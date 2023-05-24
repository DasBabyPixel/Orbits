package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataSerializable;

public class Vector2 implements DataSerializable {
    private double x, y;

    public Vector2() {
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

    public Vector2 set(Vector2 vector) {
        x(vector.x());
        y(vector.y);
        return this;
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

    public void multiply(double i) {
        this.x *= i;
        this.y *= i;
    }
}
