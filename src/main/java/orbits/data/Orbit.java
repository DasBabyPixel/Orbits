package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataSerializable;

public class Orbit implements DataSerializable {
    private final Position position = new Position(0, 0);
    private double radius;

    @Override
    public void write(DataBuffer buffer) {
        buffer.write(position);
        buffer.writeDouble(radius);
    }

    @Override
    public void read(DataBuffer buffer) {
        buffer.read(position);
        radius = buffer.readDouble();
    }

    public Position position() {
        return position;
    }

    public void radius(double radius) {
        this.radius = radius;
    }

    public double radius() {
        return radius;
    }

    public void recalcBody() {
    }
}
