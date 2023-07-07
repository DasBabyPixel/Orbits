package orbits.network.client;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketDeletePlayer extends Packet {
    public int id;

    public PacketDeletePlayer() {
        super("delete_player");
    }

    public PacketDeletePlayer(int id) {
        this();
        this.id = id;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeInt(id);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        this.id = buffer.readInt();
    }

    @Override
    public String toString() {
        return "DeletePlayerPacket{" + "id=" + id + '}';
    }
}
