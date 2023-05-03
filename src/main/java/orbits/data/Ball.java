package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataSerializable;

public class Ball implements DataSerializable {
    private final Position position = new Position(0, 0);
    private double radius;

    public void radius(double radius) {
        this.radius = radius;
    }

    public double radius() {
        return radius;
    }

    public Position position() {
        return position;
    }

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
}
