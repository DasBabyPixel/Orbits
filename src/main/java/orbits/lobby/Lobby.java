package orbits.lobby;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import orbits.OrbitsGame;
import orbits.data.*;
import orbits.data.level.Level;
import orbits.data.level.StartPosition;
import orbits.physics.PhysicsEngine;
import org.dyn4j.collision.Filter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.ContactListenerAdapter;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lobby {
    private final AvailableData availableData = new AvailableData();
    private final List<Player> players = new ArrayList<>();
    private final Int2ObjectMap<Entity> entities = new Int2ObjectLinkedOpenHashMap<>();
    private final PhysicsEngine physicsEngine = new PhysicsEngine(this);
    private float scale = 1;
    private float speed = 0.65F;
    private int entityIdCounter = 1;
    private Level level;
    private float playerSize = 0.03F;
    private float spawnSpeed = 0.1F;
    private OrbitsGame orbitsGame;

    public Lobby() {
    }

    public AvailableData availableData() {
        return availableData;
    }

    public void start(OrbitsGame orbitsGame) {
        this.orbitsGame = orbitsGame;
        level = availableData.level;
        World<Body> world = physicsEngine.world();
        world.addContactListener(new ContactListenerAdapter<>() {
            @Override
            public void end(ContactCollisionData<Body> collision, Contact contact) {
                Body b1 = collision.getBody1();
                Body b2 = collision.getBody2();
                work(b1);
                work(b2);
            }

            @Override
            public void begin(ContactCollisionData<Body> collision, Contact contact) {
                Body b1 = collision.getBody1();
                Body b2 = collision.getBody2();
                if (b1.getUserData() instanceof Player && b2.getUserData() instanceof Ball)
                    begin(((Player) b1.getUserData()), ((Ball) b2.getUserData()));
                if (b2.getUserData() instanceof Player && b1.getUserData() instanceof Ball)
                    begin(((Player) b2.getUserData()), ((Ball) b1.getUserData()));
            }

            private void begin(Player player, Ball ball) {
                if (ball instanceof Player) return;
                if (ball.ownerId() != 0) {
                    if (ball.ownerId() == player.ownerId()) return;
                    kill(player, (Player) entities.get(ball.ownerId()));
                    return;
                }
                player.addTrail(ball);
            }

            private void work(Body body) {
                if (body.getUserData() instanceof Player) {
                    Ball ball = (Ball) body.getUserData();
                    body.getLinearVelocity().normalize();
                    float speed = speed();
                    if (ball instanceof Player) speed = ((Player) ball).calculateSpeed(Lobby.this);
                    body.getLinearVelocity().multiply(speed);

                    ball.motion().x(toLocalSpaceX(body.getLinearVelocity().x));
                    ball.motion().y(body.getLinearVelocity().y);
                }
            }
        });
        for (Wall wall : level.walls()) {
            Body body = new Body();
            Vector2 pos1 = new Vector2();
            pos1.x = level.wallPositions().get(wall.pos1Index()).x();
            pos1.y = level.wallPositions().get(wall.pos1Index()).y();
            pos1 = toWorldSpace(pos1);
            Vector2 pos2 = new Vector2();
            pos2.x = level.wallPositions().get(wall.pos2Index()).x();
            pos2.y = level.wallPositions().get(wall.pos2Index()).y();
            pos2 = toWorldSpace(pos2);

            BodyFixture f = body.addFixture(Geometry.createSegment(pos1, pos2), 1, 0, 1);
            f.setRestitutionVelocity(0);
            f.setFilter(new BallFilter(null, BallFilter.TYPE_WALL));
            body.setMass(MassType.INFINITE);
            world.addBody(body);
        }

        int startPosIdx = 0;
        Map<StartPosition, List<Player>> positions = new HashMap<>();
        for (Player player : players) {
            StartPosition pos = level.startPositions().get((startPosIdx++) % level.startPositions().size());
            positions.computeIfAbsent(pos, a -> new ArrayList<>()).add(player);
            newEntity(player);
        }
        for (Map.Entry<StartPosition, List<Player>> entry : positions.entrySet()) {
            double r = Math.PI * Math.random();
            int idx = 0;
            int maxIdx = entry.getValue().size();
            for (; idx < maxIdx; idx++) {
                Player player = entry.getValue().get(idx);
                double worldX = Math.cos(2 * Math.PI * idx / maxIdx + r);
                double worldY = Math.sin(2 * Math.PI * idx / maxIdx + r);
                double x = toLocalSpaceX(worldX);
                double y = worldY;
                player.position().x(entry.getKey().position().x() + x * entry.getKey().radius());
                player.position().y(entry.getKey().position().y() + y * entry.getKey().radius());
                player.motion().x(toLocalSpaceX(worldY * speed));
                player.motion().y(-worldX * speed);
            }
        }

        for (Entity entity : entities.values()) {
            if (entity instanceof Ball) {
                Ball ball = (Ball) entity;
                Body body = new Body();
                body.setUserData(ball);
                ball.body = body;
                BodyFixture f = body.addFixture(Geometry.createCircle(playerSize / 2 * scale), 1, 0, 1);
                f.setRestitutionVelocity(0);
                body.translate(toWorldSpaceX(ball.position().x()), ball.position().y());
                body.setMass(new Mass(new Vector2(), 1, 0));
                body.setBullet(true);
                body.setLinearVelocity(toWorldSpaceX(toWorldSpaceX(ball.motion().x())), ball.motion().y());

                if (entity instanceof Player) {
                    ball.ownerId(entity.entityId());
                    f.setFilter(new BallFilter(ball, BallFilter.TYPE_PLAYER));
                    body.setAtRestDetectionEnabled(false);
                } else {
                    f.setFilter(new BallFilter(ball, BallFilter.TYPE_BALL));
                }
                world.addBody(body);
            }
        }
    }

    public void kill(Player player, @Nullable Player killer) {
        players.remove(player);

        entities.remove(player.entityId());
        player.entityId(0);
        physicsEngine.remove().add(player);

        Ball b = player.pull();
        if (b != null) b.prev(null);
        while (b != null) {
            System.out.println(b);
            Ball n = b.pull();
            if (killer == null) {
                Ball finalB = b;
                orbitsGame.launcher().frame().renderThread().submit(() -> finalB.model.cleanup());
                entities.remove(b.entityId());
                physicsEngine.remove().add(b);
                if (b.prev() != null) b.prev().pull(null);
                if (b.pull() != null) b.pull().prev(null);
            } else {
                killer.addTrail(b);
            }
            b = n;
        }
    }

    public Ball newBall(double x, double y) {
        Ball ball = new Ball();
        ball.position().x(x);
        ball.position().y(y);
        ball.color().x(1);
        ball.color().y(1);
        ball.color().z(1);
        newEntity(ball);
        ball.body = new Body();
        ball.body.setLinearDamping(0.43);
        ball.body.setUserData(ball);
        BodyFixture f = ball.body.addFixture(Geometry.createCircle(playerSize / 2 * scale), 4, 0, 0);
        f.setFilter(new BallFilter(ball, BallFilter.TYPE_BALL));
        ball.body.translate(toWorldSpaceX(x), y);
        ball.body.setBullet(true);
        ball.body.setMass(new Mass(new Vector2(), 1, 0));
        physicsEngine.world().addBody(ball.body);
        return ball;
    }

    public float speed() {
        return speed;
    }

    public float scale() {
        return scale;
    }

    public PhysicsEngine physicsEngine() {
        return physicsEngine;
    }

    public Vector2 toWorldSpace(Vector2 vector) {
        vector.x = toWorldSpaceX(vector.x);
        return vector;
    }

    public Vector2 toLocalSpace(Vector2 vector) {
        vector.x = toLocalSpaceX(vector.x);
        return vector;
    }

    public double toWorldSpaceX(double worldSpace) {
        return worldSpace * level.aspectRatioWpH();
    }

    public double toLocalSpaceX(double worldSpace) {
        return worldSpace / level.aspectRatioWpH();
    }

    private void newEntity(Entity entity) {
        entities.put(entityIdCounter, entity);
        entity.entityId(entityIdCounter++);
    }

    public Int2ObjectMap<Entity> entities() {
        return entities;
    }

    public List<Player> players() {
        return players;
    }

    public float playerSize() {
        return playerSize;
    }

    public Level level() {
        return level;
    }

    public float spawnSpeed() {
        return spawnSpeed;
    }

    private static class BallFilter implements Filter {
        private static final int TYPE_PLAYER = 1;
        private static final int TYPE_BALL = 2;
        private static final int TYPE_WALL = 3;

        private final Ball ball;
        private final int type;

        public BallFilter(Ball ball, int type) {
            this.ball = ball;
            this.type = type;
        }

        private static boolean compare(BallFilter f1, BallFilter f2, int type1, int type2) {
            if (f1.type == type1 && f2.type == type2) return true;
            if (f2.type == type1 && f1.type == type2) return true;
            if (f1.ball == null || f2.ball == null) return true;
            if (f1.ball.ownerId() == f2.ball.ownerId()) return false;
            return false;
        }

        @Override
        public boolean isAllowed(Filter filter) {
            if (filter instanceof BallFilter) {
                BallFilter bf = (BallFilter) filter;
                if ((type == TYPE_PLAYER && bf.type == TYPE_BALL) || (type == TYPE_BALL && bf.type == TYPE_PLAYER)) {
                    if (bf.ball.ownerId() == ball.ownerId()) return false;
                    return true;
                }
                if (compare(this, bf, TYPE_BALL, TYPE_WALL)) return true;
            }
            return false;
        }
    }
}
