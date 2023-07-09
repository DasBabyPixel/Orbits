package orbits.ingame;

import gamelauncher.engine.GameLauncher;
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
import org.dyn4j.dynamics.ContinuousDetectionMode;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {
    final PhysicsEngine physicsEngine = new PhysicsEngine(this);
    final AvailableData availableData = new AvailableData();
    final List<Player> players = new ArrayList<>();
    final Int2ObjectMap<Entity> entities = new Int2ObjectLinkedOpenHashMap<>();
    public GameBroadcast broadcast;
    float scale = 1;
    float speed = 0.25F;
    int entityIdCounter = 1;
    Level level;
    float playerSize = 0.03F;
    float spawnSpeed = 0.5F * 7;
    OrbitsGame orbitsGame;
    boolean owner = true;

    public Game() {
    }

    public boolean owner() {
        return owner;
    }

    public void owner(boolean owner) {
        this.owner = owner;
    }

    public AvailableData availableData() {
        return availableData;
    }

    public void start(OrbitsGame orbitsGame) {
        this.orbitsGame = orbitsGame;
        level = availableData.level;
        World<Body> world = physicsEngine.world();
        world.getSettings().setContinuousDetectionMode(ContinuousDetectionMode.BULLETS_ONLY);
        world.addContactListener(new OrbitsContactListener(this));
        addWalls();
        addOrbits();

        if (owner) loadPlayers();

        loadEntities();

        if (broadcast != null) {
            for (Entity entity : players()) {
                broadcast.update(entity);
            }
        }
    }

    void loadEntities() {
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
                ball.updateMotion(this);
                physicsEngine.world().addBody(body);
            }
        }
    }

    void loadPlayers() {
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
                player.motion().x(y * speed);
                player.motion().y(-x * speed);
            }
        }
    }

    void addOrbits() {
        for (Orbit orbit : level.orbits()) {
            Body body = new Body();
            body.setUserData(orbit);
            BodyFixture f = body.addFixture(Geometry.createCircle(orbit.radius() - (playerSize / 2 * scale)), 1, 0, 0);
            f.setFilter(new BallFilter(null, BallFilter.TYPE_ORBIT));
            body.setMass(MassType.INFINITE);
            body.translate(toWorldSpaceX(orbit.position().x()), orbit.position().y());
            physicsEngine.world().addBody(body);
        }
    }

    void addWalls() {
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
            physicsEngine.world().addBody(body);
        }
    }

    void reset(Ball ball) {
        ball.projectile = false;
        ball.body.setBullet(false);
        ball.ownerId(0);
        ball.color().x(1);
        ball.color().y(1);
        ball.color().z(1);
        if (broadcast != null) broadcast.update(ball);
    }

    public void tap(Player p) {
        if (p.entityId() == 0) return;
        if (p.dodgeMultiplier() > 1) return;
        if (p.currentOrbit() != null) {
            if (!p.orbiting()) {
                double ox = p.currentOrbit().position().x();
                double oy = p.currentOrbit().position().y();
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

                double circumference = dist * Math.PI * 2;
                double speed = p.calculateSpeed(this);
                double percentSpeed = speed / circumference;

                double w1 = -xv * (woy - (yp + yv));
                double w2 = -yv * (wox - (xp + xv));

                double sign = Math.signum(w2 - w1);

                p.orbiting(true, sign * Math.PI * 2 * percentSpeed / GameLauncher.MAX_TPS);
                if (broadcast != null) broadcast.update(p);
            } else {
                stopOrbit(p);
            }
            return;
        }
        dodge(p);
    }

    void dodge(Player p) {
        p.dodgeMultiplier(this, Player.DODGE_SPEED);
        p.dodgeMultiplierApplied(System.currentTimeMillis());
        Ball b = p.removeTrail();
        if (broadcast != null) {
            broadcast.update(p);
        }
        if (b != null) {
            setupProjectile(p, b);
            if (broadcast != null) {
                broadcast.update(b);
                broadcast.removeTrail(p.entityId(), b.entityId());
            }
        }
    }

    public void setupProjectile(Player owner, Ball b) {
        b.ownerId(owner.entityId());
        b.body.translateToOrigin();
        b.body.translate(owner.body.getWorldCenter());
        b.body.getLinearVelocity().set(owner.body.getLinearVelocity());
        b.body.getLinearVelocity().multiply(Player.DODGE_SPEED * 1.3);
        b.body.setAtRest(false);
        b.body.setBullet(true);
        b.projectile = true;
        b.updateMotion(this);
    }

    void stopOrbit(Player player) {
        if (!player.orbiting()) return;
        double ox = toWorldSpaceX(player.currentOrbit().position().x());
        double oy = player.currentOrbit().position().y();
        double px = player.body.getWorldCenter().x;
        double py = player.body.getWorldCenter().y;
        double tx = oy - py;
        double ty = px - ox;
        tx *= Math.signum(player.orbitingTheta());
        ty *= Math.signum(player.orbitingTheta());
        player.body.setLinearVelocity(tx, ty);
        player.body.getLinearVelocity().normalize();
        player.body.getLinearVelocity().multiply(player.calculateSpeed(this));
        player.updateMotion(this);
        player.orbiting(false, 0);
        if (broadcast != null) broadcast.update(player);
    }

    public void kill(Player player, @Nullable Player killer) {
        if (player.entityId() == 0) return;
        players.remove(player);
        int pid = player.entityId();
        entities.remove(player.entityId());
        player.entityId(0);
        physicsEngine.remove().add(player);
        if (broadcast != null) broadcast.removed(pid);

        Ball b = player.pull();
        if (b != null) {
            player.pull(null);
        }
        while (b != null) {
            Ball n = b.pull();
            if (killer == null || killer.entityId() == 0) {
                if (broadcast != null) broadcast.removed(b.entityId());
                entities.remove(b.entityId());
                physicsEngine.remove().add(b);
                if (b.prev() != null) b.prev().pull(b.pull());
                b.pull(null);
            } else {
                killer.addTrail(b);
                if (broadcast != null) {
                    broadcast.addTrail(killer.entityId(), b.entityId());
                }
            }
            b = n;
        }
    }

    public void newBall(double x, double y) {
        Ball ball = new Ball();
        ball.color().x(1);
        ball.color().y(1);
        ball.color().z(1);
        ball.position().x(x);
        ball.position().y(y);
        newEntity(ball);
        setupBody(ball);
        physicsEngine.world().addBody(ball.body);
        if (broadcast != null) broadcast.update(ball);
    }

    public Body setupBody(Ball entity) {
        double x = entity.position().x();
        double y = entity.position().y();
        Body body = new Body();
        body.setLinearDamping(0.73);
        body.setUserData(entity);
        entity.body = body;
        BodyFixture f = entity.body.addFixture(Geometry.createCircle(playerSize / 2 * scale), 4, 0, 0);
        f.setFilter(new BallFilter(entity, BallFilter.TYPE_BALL));
        entity.body.translate(toWorldSpaceX(x), y);
        entity.body.setMass(new Mass(new Vector2(), 1, 0));
        if (entity instanceof Player) {
            players.add((Player) entity);
        }
        return body;
    }

    public void setPositionToWorld(Ball entity) {
        double x = entity.position().x();
        double y = entity.position().y();
        entity.body.translateToOrigin();
        entity.body.translate(toWorldSpaceX(x), y);
    }

    public void setMotionToWorld(Ball b) {
        b.body.getLinearVelocity().set(toWorldSpaceX(b.motion().x()), b.motion().y());
        b.body.setAtRest(false);
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

    void newEntity(Entity entity) {
        if (!owner) throw new IllegalStateException("Not Owner");
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
