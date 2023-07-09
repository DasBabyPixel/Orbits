package orbits.ingame;

import orbits.data.*;
import org.dyn4j.collision.CollisionItem;
import org.dyn4j.collision.broadphase.CollisionItemBroadphaseDetector;
import org.dyn4j.collision.broadphase.CollisionItemBroadphaseDetectorAdapter;
import org.dyn4j.collision.broadphase.DynamicAABBTree;
import org.dyn4j.collision.broadphase.NullAABBExpansionMethod;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.ContinuousDetectionMode;
import org.dyn4j.dynamics.Settings;
import org.dyn4j.world.World;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PhysicsEngine {

    private final World<Body> world = new World<>();
    private final Random r = new Random();
    private final Game game;
    private final List<Ball> remove = new ArrayList<>();
    private final double spawnChance = 0.005; // 0.005 default

    public PhysicsEngine(Game game) {
        this.game = game;
        world.setGravity(World.ZERO_GRAVITY);
        world.getSettings().setContinuousDetectionMode(ContinuousDetectionMode.BULLETS_ONLY);
        world.getSettings().setVelocityConstraintSolverIterations(7);
        world.getSettings().setAtRestDetectionEnabled(true);
        world.getSettings().setMaximumAtRestLinearVelocity(0.005);
        world.getSettings().setBaumgarte(Settings.DEFAULT_BAUMGARTE);
        world.getSettings().setLinearTolerance(Settings.DEFAULT_LINEAR_TOLERANCE);
        CollisionItemBroadphaseDetector<Body, BodyFixture> bd = world.getBroadphaseDetector();
        DynamicAABBTree<CollisionItem<Body, BodyFixture>> tree = new DynamicAABBTree<>(bd.getBroadphaseFilter(), bd.getAABBProducer(), new NullAABBExpansionMethod<>());
        world.setBroadphaseDetector(new CollisionItemBroadphaseDetectorAdapter<>(tree));
    }

    public World<Body> world() {
        return world;
    }

    public void tick() {
        if (game.owner()) { // Nur wenn wir der server sind
            if (game.entities().size() < 500) { // Nur neue bälle erschaffen, wenn weniger als 500 existieren
                spawnBalls();
            }
        }

        removeOldEntities();

        simulationStep();

        tickEntities();

        tickDodgeTimes();
    }

    private void tickDodgeTimes() {
        for (Player player : game.players()) {
            tickDodgeTime(player);
        }
    }

    private void tickDodgeTime(Player player) {
        long diff = -System.currentTimeMillis() + Player.DODGE_DURATION + player.dodgeMultiplierApplied();
        if (diff <= 0 || diff > Player.DODGE_DURATION) {
            player.dodgeMultiplierApplied(Long.MAX_VALUE);
            player.dodgeMultiplier(game, 1);
        } else {
            float percent = (float) diff / Player.DODGE_DURATION;
            percent = (float) java.lang.Math.pow(percent, 0.7);
            if (percent < 0.01) percent = 0;
            player.dodgeMultiplier(game, (Player.DODGE_SPEED - 1) * percent + 1);
        }
    }

    private void simulationStep() {
        for (Player player : game.players()) {
            preTickPlayer(player);
        }
        world.step(1);
        for (Player player : game.players()) {
            postTickPlayer(player);
        }
    }

    private void preTickPlayer(Player player) {
    }

    private void postTickPlayer(Player player) {
        int cur = 0;
        Ball l = player;
        int size = player.positions().size() / 2;
        int max = size - 1;
        int space = (int) (20 / game.speed());
        while (l.pull() != null) {
            cur++;
            l = l.pull();
            int idx = max - Math.min(max, cur * space / 10) + 1;
            idx = Math.clamp(0, max, idx);
            if (player.positions().size() > 0) {
                l.position().x(player.positions().getDouble(idx * 2));
                l.position().y(player.positions().getDouble(idx * 2 + 1));
            }
            if (l.body != null) {
                l.body.translateToOrigin();
                l.body.translate(game.toWorldSpaceX(l.position().x()), l.position().y());
            }
        }
        if (player.orbiting()) {
            player.body.rotate(player.orbitingTheta(), game.toWorldSpaceX(player.currentOrbit().position().x()), player.currentOrbit().position().y());
        }
    }

    private void spawnBalls() {
        int count = 0;
        float spawn = game.spawnSpeed();
        while (spawn > 0) {
            float f = r.nextFloat();
            spawn = spawn - f;
            if (f < spawnChance) count++;
        }
        float px = (float) game.level().startPositions().get(0).position().x();
        float py = (float) game.level().startPositions().get(0).position().y();
        for (int i = 0; i < count; i++) {
            float tx = r.nextFloat();
            float ty = r.nextFloat();
            float rx = tx - px;
            float ry = ty - py;

            int intersectCount = calculateIntersections(rx, ry, px, py);

            if (intersectCount % 2 == 0) {
                game.newBall(tx, ty);
            }
        }
    }

    private int calculateIntersections(float rx, float ry, float px, float py) {
        int intersectCount = 0;
        for (int i2 = 0; i2 < game.level().walls().size(); i2++) {
            Wall wall = game.level().walls().get(i2);
            Position pos = game.level().wallPositions().get(wall.pos1Index());
            float qx = (float) pos.x();
            float qy = (float) pos.y();
            Position pos2 = game.level().wallPositions().get(wall.pos2Index());
            float sx = (float) (pos2.x() - qx);
            float sy = (float) (pos2.y() - qy);
            if (intersect(px, py, qx, qy, rx, ry, sx, sy)) intersectCount++;
        }
        return intersectCount;
    }

    private void tickEntities() {
        for (Entity entity : game.entities().values()) {
            tickEntity(entity);
        }
    }

    private void tickEntity(Entity entity) {
        if (entity.body != null) {
            if (entity instanceof Ball) {
                Ball ball = (Ball) entity;
                tickBall(ball);
            }
        }
    }

    private void tickBall(Ball ball) {
        if (ball instanceof Player) {
            Player p = (Player) ball;
            p.positions().add(ball.position().x());
            p.positions().add(ball.position().y());
        }
        ball.updateMotion(game);
        ball.position().x(game.toLocalSpaceX(ball.body.getWorldCenter().x));
        ball.position().y(ball.body.getWorldCenter().y);
    }

    private void removeOldEntities() {
        int size = remove.size();
        for (int i = 0; i < size; i++) {
            world.removeBody(remove.get(i).body);
        }
        remove.clear();
    }

    /**
     * @return the list of balls to remove
     */
    public List<Ball> remove() {
        return remove;
    }

    private boolean intersect(float px, float py, float qx, float qy, float rx, float ry, float sx, float sy) {
        float rs = cross(rx, ry, sx, sy);
        float qpx = qx - px;
        float qpy = qy - py;
        float t = cross(qpx, qpy, sx / rs, sy / rs);
        float u = cross(qpx, qpy, rx / rs, ry / rs);
        return rs != 0 && 0 <= t && t <= 1 && 0 <= u && u <= 1;
    }

    private float cross(float vx1, float vy1, float vx2, float vy2) {
        return vx1 * vy2 - vy1 * vx2;
    }
}
