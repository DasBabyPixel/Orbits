package orbits.physics;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.ContinuousDetectionMode;
import org.dyn4j.world.World;

import java.util.ArrayList;
import java.util.List;

public class PhysicsEngine {

    private final World<Body> world = new World<>();
    private long lastTick;

    public PhysicsEngine() {
        world.setGravity(World.ZERO_GRAVITY);
        world.getSettings().setContinuousDetectionMode(ContinuousDetectionMode.BULLETS_ONLY);
    }

    public World<Body> world() {
        return world;
    }

    public void tick() {
        world.step(1);
    }

    public void add(Body body) {
        world.addBody(body);
    }

    public void remove(Body body) {
        world.removeBody(body);
    }
}
