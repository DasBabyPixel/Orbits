package orbits.data;

import gamelauncher.engine.network.packet.BufferObject;
import gamelauncher.engine.network.packet.PacketBuffer;

public class Ball implements BufferObject {
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
    public void write(PacketBuffer buffer) {
        buffer.write(position);
        buffer.writeDouble(radius);
    }

    @Override
    public void read(PacketBuffer buffer) {
        buffer.read(position);
        radius = buffer.readDouble();
    }
}
