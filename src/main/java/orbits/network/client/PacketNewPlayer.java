package orbits.network.client;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketNewPlayer extends Packet {
    public int id;
    public char ch;

    public PacketNewPlayer() {
        super("new_player");
    }

    public PacketNewPlayer(int id, char ch) {
        this();
        this.id = id;
        this.ch = ch;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeInt(id);
        buffer.writeInt(ch);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        id = buffer.readInt();
        ch = (char) buffer.readInt();
    }

    @Override
    public String toString() {
        return "NewPlayerPacket{" + "id=" + id + ", ch=" + ch + '}';
    }
}
