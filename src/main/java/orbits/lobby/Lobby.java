package orbits.lobby;

import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.util.GameException;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import orbits.OrbitsGame;
import orbits.data.*;
import orbits.data.level.Level;
import orbits.data.level.StartPosition;
import orbits.gui.OrbitsMainScreenGui;
import orbits.physics.PhysicsEngine;
import org.dyn4j.collision.Filter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.ContinuousDetectionMode;
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
    private float speed = 0.25F;
    private int entityIdCounter = 1;
    private Level level;
    private float playerSize = 0.03F;
    private float spawnSpeed = 0.5F * 7;
    private int stopTimer = -1;
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
        world.getSettings().setContinuousDetectionMode(ContinuousDetectionMode.BULLETS_ONLY);
        world.addContactListener(new ContactListenerAdapter<>() {
            @Override
            public void end(ContactCollisionData<Body> collision, Contact contact) {
                Body b1 = collision.getBody1();
                Body b2 = collision.getBody2();
                end(b1);
                end(b2);
                if (b1.getUserData() instanceof Player && b2.getUserData() instanceof Orbit)
                    ((Player) b1.getUserData()).currentOrbit = null;
                if (b2.getUserData() instanceof Player && b1.getUserData() instanceof Orbit)
                    ((Player) b2.getUserData()).currentOrbit = null;
            }

            @Override
            public void begin(ContactCollisionData<Body> collision, Contact contact) {
                Body b1 = collision.getBody1();
                Body b2 = collision.getBody2();
                begin(collision, b1, b2);
                begin(collision, b2, b1);
            }

            private void begin(ContactCollisionData<Body> collision, Body b1, Body b2) {
                if (b1.getUserData() instanceof Player && b2.getUserData() instanceof Ball) {
                    begin(collision, ((Player) b1.getUserData()), ((Ball) b2.getUserData()));
                } else if (b1.getUserData() instanceof Player && b2.getUserData() instanceof Orbit) {
                    begin(collision, ((Player) b1.getUserData()), ((Orbit) b2.getUserData()));
                } else if (b1.getUserData() instanceof Ball) {
                    Ball b = (Ball) b1.getUserData();
                    if (b.projectile) {
                        if (b2.getUserData() instanceof Wall) {
                            reset(b);
                        } else if (b2.getUserData() instanceof Player) {
                            Player target = (Player) b2.getUserData();
                            Player killer = (Player) entities.get(b.ownerId());
                            kill(target, killer);
                        }
                    }
                }
            }

            private void begin(ContactCollisionData<Body> collision, Player player, Orbit orbit) {
                collision.getContactConstraint().setEnabled(false);
                player.currentOrbit = orbit;
            }

            private void begin(ContactCollisionData<Body> collision, Player player, Ball ball) {
                if (player.dodgeMultiplier() > 1 || player.entityId() == 0) {
                    collision.getContactConstraint().setEnabled(false);
                    return;
                }
                if (ball instanceof Player) {
                    if (((Player) ball).dodgeMultiplier() > 1 || ball.entityId() == 0) {
                        collision.getContactConstraint().setEnabled(false);
                        return;
                    }
                    stopOrbit(player);
                    stopOrbit((Player) ball);
                    return;
                }
                if (ball.ownerId() != 0) {
                    if (ball.ownerId() == player.entityId()) return;
                    kill(player, (Player) entities.get(ball.ownerId()));
                    return;
                }
                collision.getContactConstraint().setEnabled(false);
                player.addTrail(ball);
            }

            private void end(Body body) {
                if (body.getUserData() instanceof Player) {
                    Player player = (Player) body.getUserData();
                    body.getLinearVelocity().normalize();
                    float speed = player.calculateSpeed(Lobby.this);
                    body.getLinearVelocity().multiply(speed);

                    player.updateMotion(Lobby.this);
                }
            }
        });
        for (Wall wall : level.walls()) {
            Body body = new Body();
            body.setUserData(wall);
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
        for (Orbit orbit : level.orbits()) {
            Body body = new Body();
            body.setUserData(orbit);
            BodyFixture f = body.addFixture(Geometry.createCircle(orbit.radius() - (playerSize / 2 * scale)), 1, 0, 0);
            f.setFilter(new BallFilter(null, BallFilter.TYPE_ORBIT));
            body.setMass(MassType.INFINITE);
            body.translate(toWorldSpaceX(orbit.position().x()), orbit.position().y());
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
                if (speed > 0.3) body.setBullet(true);
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

    private void reset(Ball ball) {
        ball.projectile = false;
        ball.body.setBullet(false);
        ball.ownerId(0);
        ball.color().x(1);
        ball.color().y(1);
        ball.color().z(1);
    }

    public void tap(Player p) {
        if (p.dodgeMultiplier() > 1) return;
        if (p.currentOrbit != null) {
            if (!p.orbiting) {
                p.orbiting = true;
                double ox = p.currentOrbit.position().x();
                double oy = p.currentOrbit.position().y();
                double wox = toWorldSpaceX(ox);
                double woy = oy;
                double xp = p.body.getWorldCenter().x;
                double yp = p.body.getWorldCenter().y;
                if (xp == wox && yp == woy) xp = xp + 0.001; // Sollte man genau im Orbitmittelpunkt in den Orbit gehen
                double xv = p.body.getLinearVelocity().x;
                double yv = p.body.getLinearVelocity().y;
                double dx = xp - wox;
                double dy = yp - woy;
                double dist = Math.sqrt(dx * dx + dy * dy);
                p.body.setLinearVelocity(0, 0);
                double circumference = dist * Math.PI * 2;
                double speed = p.calculateSpeed(this);
                double percentSpeed = speed / circumference;

                double w1 = -xv * (woy - (yp + yv));
                double w2 = -yv * (wox - (xp + xv));

                double sign = Math.signum(w2 - w1);

                p.orbitingTheta = sign * Math.PI * 2 * percentSpeed / GameLauncher.MAX_TPS;
            } else {
                stopOrbit(p);
            }
            return;
        }
        dodge(p);
    }

    private void dodge(Player p) {
        p.dodgeMultiplier(this, Player.DODGE_SPEED);
        p.dodgeMultiplierApplied(System.currentTimeMillis());
        Ball b = p.removeTrail();
        if (b != null) {
            b.ownerId(p.entityId());
            b.body.translateToOrigin();
            b.body.translate(p.body.getWorldCenter());
            b.body.getLinearVelocity().set(p.body.getLinearVelocity());
            b.body.getLinearVelocity().multiply(Player.DODGE_SPEED * 1.3);
            b.body.setAtRest(false);
            b.body.setBullet(true);
            b.projectile = true;
        }
    }

    private void stopOrbit(Player player) {
        if (!player.orbiting) return;
        double ox = toWorldSpaceX(player.currentOrbit.position().x());
        double oy = player.currentOrbit.position().y();
        double px = player.body.getWorldCenter().x;
        double py = player.body.getWorldCenter().y;
        double tx = oy - py;
        double ty = px - ox;
        tx *= Math.signum(player.orbitingTheta);
        ty *= Math.signum(player.orbitingTheta);
        player.body.setLinearVelocity(tx, ty);
        player.body.getLinearVelocity().normalize();
        player.body.getLinearVelocity().multiply(player.calculateSpeed(this));
        player.orbiting = false;
    }

    public void stop() throws GameException {
        orbitsGame.launcher().guiManager().openGui(new OrbitsMainScreenGui(orbitsGame));
    }

    public void kill(Player player, @Nullable Player killer) {
        if (player.entityId() == 0) return;
        players.remove(player);

        entities.remove(player.entityId());
        player.entityId(0);
        physicsEngine.remove().add(player);
        orbitsGame.launcher().frame().renderThread().submit(() -> player.model.cleanup());

        if (players.size() == 1) {
            stopTimer = (int) (GameLauncher.MAX_TPS * 5);
        }

        Ball b = player.pull();
        if (b != null) b.prev(null);
        while (b != null) {
            Ball n = b.pull();
            if (killer == null || killer.entityId() == 0) {
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

    public void newBall(double x, double y) {
        Ball ball = new Ball();
        ball.position().x(x);
        ball.position().y(y);
        newEntity(ball);
        ball.body = new Body();
        ball.body.setLinearDamping(0.73);
        ball.body.setUserData(ball);
        reset(ball);
        BodyFixture f = ball.body.addFixture(Geometry.createCircle(playerSize / 2 * scale), 4, 0, 0);
        f.setFilter(new BallFilter(ball, BallFilter.TYPE_BALL));
        ball.body.translate(toWorldSpaceX(x), y);
        ball.body.setMass(new Mass(new Vector2(), 1, 0));
        physicsEngine.world().addBody(ball.body);
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

    public int stopTimer() {
        return stopTimer;
    }

    public void stopTimer(int stopTimer) {
        this.stopTimer = stopTimer;
    }

    public float spawnSpeed() {
        return spawnSpeed;
    }

    private static class BallFilter implements Filter {
        private static final int TYPE_PLAYER = 1;
        private static final int TYPE_BALL = 2;
        private static final int TYPE_WALL = 3;
        private static final int TYPE_ORBIT = 4;

        private final Ball ball;
        private final int type;

        public BallFilter(Ball ball, int type) {
            this.ball = ball;
            this.type = type;
        }

        private static boolean compare(BallFilter f1, BallFilter f2, int type1, int type2) {
            if (f1.type == type1 && f2.type == type2) return true;
            if (f2.type == type1 && f1.type == type2) return true;
            return false;
        }

        @Override
        public boolean isAllowed(Filter filter) {
            if (filter instanceof BallFilter) {
                BallFilter f = (BallFilter) filter;
                if (compare(this, f, TYPE_PLAYER, TYPE_BALL)) return f.ball.ownerId() != ball.ownerId();
                if (compare(this, f, TYPE_PLAYER, TYPE_ORBIT)) return true;
                if (compare(this, f, TYPE_PLAYER, TYPE_PLAYER)) return true;
                if (compare(this, f, TYPE_PLAYER, TYPE_WALL)) return true;
                if (compare(this, f, TYPE_BALL, TYPE_WALL)) return true;
            }
            return false;
        }
    }
}
