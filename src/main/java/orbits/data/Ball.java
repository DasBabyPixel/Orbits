package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataSerializable;

public class Ball extends Entity {
    private final Position position = new Position(0, 0);
    private final Vector2 motion = new Vector2();
    private double radius;

    public void radius(double radius) {
        this.radius = radius;
    }

    public double radius() {
        return radius;
    }

    public Vector2 motion() {
        return motion;
    }

    public Position position() {
        return position;
    }

    @Override
    public void write(DataBuffer buffer) {
        super.write(buffer);
        buffer.write(position);
        buffer.write(motion);
        buffer.writeDouble(radius);
    }

    @Override
    public void read(DataBuffer buffer) {
        super.read(buffer);
        buffer.read(position);
        buffer.read(motion);
        radius = buffer.readDouble();
    }
}
