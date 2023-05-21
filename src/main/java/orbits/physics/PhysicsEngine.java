package orbits.physics;

import orbits.data.Ball;
import orbits.data.Entity;
import orbits.data.Player;
import orbits.lobby.Lobby;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.ContinuousDetectionMode;
import org.dyn4j.world.World;

public class PhysicsEngine {

    private final World<Body> world = new World<>();
    private final Lobby lobby;

    public PhysicsEngine(Lobby lobby) {
        this.lobby = lobby;
        world.setGravity(World.ZERO_GRAVITY);
        world.getSettings().setContinuousDetectionMode(ContinuousDetectionMode.BULLETS_ONLY);
    }

    public World<Body> world() {
        return world;
    }

    public void tick() {
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
        world.step(1);
    }
}
