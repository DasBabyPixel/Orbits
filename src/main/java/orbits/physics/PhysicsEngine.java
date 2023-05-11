package orbits.physics;

import org.dyn4j.dynamics.Body;
import org.dyn4j.world.World;

import java.util.ArrayList;
import java.util.List;

public class PhysicsEngine {

    private final World<Body> world = new World<>();
    private final List<Collidable> collidables = new ArrayList<>();
    private long lastTick;

    public PhysicsEngine() {
        world.setGravity(0, 0);
    }

    public void tick() {

    }

    public void add(Collidable collidable) {
        collidables.add(collidable);
        world.addBody(collidable.body());
    }

    public void remove(Collidable collidable) {
        collidables.remove(collidable);
        world.removeBody(collidable.body());
    }
}
