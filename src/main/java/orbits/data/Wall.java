package orbits.data;

import gamelauncher.engine.network.packet.BufferObject;
import gamelauncher.engine.network.packet.PacketBuffer;

public class Wall implements BufferObject {

    private final Position pos1 = new Position(0, 0);
    private final Position pos2 = new Position(0, 0);

    public Position pos1() {
        return pos1;
    }

    public Position pos2() {
        return pos2;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.write(pos1);
        buffer.write(pos2);
    }

    @Override
    public void read(PacketBuffer buffer) {
        buffer.read(pos1);
        buffer.read(pos2);
    }
}
