package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataSerializable;

public class Vector3 implements DataSerializable {
    private float x, y, z;

    public Vector3() {
    }

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float x() {
        return x;
    }

    public void x(float x) {
        this.x = x;
    }

    public float y() {
        return y;
    }

    public void y(float y) {
        this.y = y;
    }

    public float z() {
        return z;
    }

    public void z(float z) {
        this.z = z;
    }

    public void set(Vector3 other) {
        x(other.x);
        y(other.y);
        z(other.z);
    }

    @Override
    public void write(DataBuffer buffer) {
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
    }

    @Override
    public void read(DataBuffer buffer) {
        x = buffer.readFloat();
        y = buffer.readFloat();
        z = buffer.readFloat();
    }

    @Override
    public String toString() {
        return "(" + x + " | " + y + " | " + z + ")";
    }
}
