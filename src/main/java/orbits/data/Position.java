package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataSerializable;
import org.dyn4j.geometry.Vector2;

import java.util.Objects;

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

    public Vector2 physicsVector() {
        return new Vector2(x, y);
    }

    public double distanceSquared(Position other) {
        double x1 = x - other.x;
        double y1 = y - other.y;
        return x1 * x1 + y1 * y1;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return Double.compare(position.x, x) == 0 && Double.compare(position.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Position{" + "x=" + x + ", y=" + y + '}';
    }
}
