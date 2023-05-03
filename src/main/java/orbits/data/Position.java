package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataSerializable;

public class Position implements DataSerializable {

    private double x, y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Position() {
        this(0, 0);
    }

    public double x() {
        return x;
    }

    public void x(double x) {
        this.x = x;
    }

    public double y() {
        return y;
    }

    public void y(double y) {
        this.y = y;
    }

    @Override
    public void write(DataBuffer buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
    }

    @Override
    public void read(DataBuffer buffer) {
        x = buffer.readDouble();
        y = buffer.readDouble();
    }
}
