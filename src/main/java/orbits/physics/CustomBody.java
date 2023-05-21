package orbits.physics;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.Settings;
import org.dyn4j.dynamics.TimeStep;
import org.dyn4j.geometry.Vector2;

public class CustomBody extends Body {
    @Override
    public void integrateVelocity(Vector2 gravity, TimeStep timestep, Settings settings) {
        super.integrateVelocity(gravity, timestep, settings);
    }

    @Override
    public void integratePosition(TimeStep timestep, Settings settings) {
        super.integratePosition(timestep, settings);
    }
}
