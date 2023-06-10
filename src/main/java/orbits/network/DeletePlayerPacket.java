package orbits.network;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class DeletePlayerPacket extends Packet {
    public int id;

    public DeletePlayerPacket() {
        super("delete_player");
    }

    public DeletePlayerPacket(int id) {
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
