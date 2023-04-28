package orbits.data.level;

import gamelauncher.engine.network.packet.BufferObject;
import gamelauncher.engine.network.packet.PacketBuffer;
import orbits.data.Position;

public class StartPosition implements BufferObject {
    private final Position position = new Position(0, 0);
    private double radius;

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
