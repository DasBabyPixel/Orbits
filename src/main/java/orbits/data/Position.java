package orbits.data;

import gamelauncher.engine.network.packet.BufferObject;
import gamelauncher.engine.network.packet.PacketBuffer;

public class Position implements BufferObject {

    private double x, y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
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
    public void write(PacketBuffer buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
    }

    @Override
    public void read(PacketBuffer buffer) {
        x = buffer.readDouble();
        y = buffer.readDouble();
    }
}
