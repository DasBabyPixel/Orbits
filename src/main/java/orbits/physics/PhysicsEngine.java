package orbits.physics;

import gamelauncher.engine.util.GameException;
import orbits.data.*;
import orbits.lobby.Lobby;
import org.dyn4j.dynamics.Body;
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
    private final Lobby lobby;
    private final List<Ball> remove = new ArrayList<>();

    public PhysicsEngine(Lobby lobby) {
        this.lobby = lobby;
        world.setGravity(World.ZERO_GRAVITY);
        world.getSettings().setContinuousDetectionMode(ContinuousDetectionMode.NONE);
        world.getSettings().setVelocityConstraintSolverIterations(2);
        world.getSettings().setAtRestDetectionEnabled(true);
        world.getSettings().setMaximumAtRestLinearVelocity(0.005);
        world.getSettings().setBaumgarte(Settings.DEFAULT_BAUMGARTE / 20);
        world.getSettings().setLinearTolerance(Settings.DEFAULT_LINEAR_TOLERANCE / 100);
    }

    public World<Body> world() {
        return world;
    }

    public void tick() throws GameException {
        if (lobby.stopTimer() != -1) {
            lobby.stopTimer(lobby.stopTimer() - 1);
            if (lobby.stopTimer() == 0) lobby.stop();
        }
        if (lobby.stopTimer() == 0) return;

        for (Player player : lobby.players()) {

            int cur = 0;
            Ball l = player;
            int size = player.positions().size() / 2;
            int max = size - 1;
            int space = (int) (20 / lobby.speed());
            while (l.pull() != null) {
                cur++;
                l = l.pull();
                int idx = max - Math.min(max, cur * space / 10) + 1;
                idx = Math.clamp(0, max, idx);
                l.position().x(player.positions().getDouble(idx * 2));
                l.position().y(player.positions().getDouble(idx * 2 + 1));
                if (l.body != null) {
                    l.body.translateToOrigin();
                    l.body.translate(lobby.toWorldSpaceX(l.position().x()), l.position().y());
                }
            }
            if (player.orbiting) {
                if (player.currentOrbit != null)
                    player.body.rotate(player.orbitingTheta, lobby.toWorldSpaceX(player.currentOrbit.position().x()), player.currentOrbit.position().y());
                else System.out.println("bug");
            }
        }
        for (Entity entity : lobby.entities().values()) {
            if (entity.body != null) {
                if (entity instanceof Ball) {
                    Ball ball = (Ball) entity;
                    ball.position().x(lobby.toLocalSpaceX(ball.body.getWorldCenter().x));
                    ball.position().y(ball.body.getWorldCenter().y);
                    if (entity instanceof Player) {
                        Player p = (Player) entity;
                        p.positions().add(ball.position().x());
                        p.positions().add(ball.position().y());
                    }
                }
            }
        }
        if (lobby.entities().size() < 500) { // Nur neue bälle erschaffen, wenn weniger als 500 existieren
            int count = 0;
            float spawn = lobby.spawnSpeed();
            while (spawn > 0) {
                float f = r.nextFloat();
                spawn = spawn - f;
                if (f < 0.005) count++;
            }
            float px = (float) lobby.level().startPositions().get(0).position().x();
            float py = (float) lobby.level().startPositions().get(0).position().y();
            for (int i = 0; i < count; i++) {
                float tx = r.nextFloat();
                float ty = r.nextFloat();
                float rx = tx - px;
                float ry = ty - py;
                int intersectCount = 0;

                for (int i2 = 0; i2 < lobby.level().walls().size(); i2++) {
                    Wall wall = lobby.level().walls().get(i2);
                    Position pos = lobby.level().wallPositions().get(wall.pos1Index());
                    float qx = (float) pos.x();
                    float qy = (float) pos.y();
                    Position pos2 = lobby.level().wallPositions().get(wall.pos2Index());
                    float sx = (float) (pos2.x() - qx);
                    float sy = (float) (pos2.y() - qy);
                    if (intersect(px, py, qx, qy, rx, ry, sx, sy)) intersectCount++;
                }
                if (intersectCount % 2 == 0) {
                    lobby.newBall(tx, ty);
                }
            }
        }
        int size = remove.size();
        for (int i = 0; i < size; i++) {
            world.removeBody(remove.get(i).body);
        }
        remove.clear();

        world.step(1);
        for (Player player : lobby.players()) {
            long diff = -System.currentTimeMillis() + Player.DODGE_DURATION + player.dodgeMultiplierApplied();
            if (diff <= 0 || diff > Player.DODGE_DURATION) {
                player.dodgeMultiplierApplied(Long.MAX_VALUE);
                player.dodgeMultiplier(lobby, 1);
            } else {
                float percent = (float) diff / Player.DODGE_DURATION;
                percent = (float) java.lang.Math.pow(percent, 0.7);
                if (percent < 0.01) percent = 0;
                player.dodgeMultiplier(lobby, (Player.DODGE_SPEED - 1) * percent + 1);
            }
        }
    }

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
