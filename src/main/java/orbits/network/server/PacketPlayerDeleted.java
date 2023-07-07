package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketPlayerDeleted extends Packet {
    public int id;

    public PacketPlayerDeleted() {
        super("player_deleted");
    }

    public PacketPlayerDeleted(int id) {
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
        return "PacketPlayerDeleted{" + "id=" + id + '}';
    }
}
