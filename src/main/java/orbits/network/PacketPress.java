package orbits.network;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketPress extends Packet {
    public int id;

    public PacketPress() {
        super("press");
    }

    public PacketPress(int id) {
        this();
        this.id = id;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeInt(id);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        id = buffer.readInt();
    }
}
