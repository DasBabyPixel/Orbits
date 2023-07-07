package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;
import org.joml.Vector4f;

public class PacketPlayerCreated extends Packet {
    public int id;
    public char display;
    public Vector4f color;

    public PacketPlayerCreated() {
        super("player_created");
    }

    public PacketPlayerCreated(int id, char display, Vector4f color) {
        this();
        this.id = id;
        this.display = display;
        this.color = color;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeInt(id);
        buffer.writeInt(display);
        buffer.writeFloat(color.x);
        buffer.writeFloat(color.y);
        buffer.writeFloat(color.z);
        buffer.writeFloat(color.w);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        id = buffer.readInt();
        display= (char) buffer.readInt();
        color = new Vector4f();
        color.x = buffer.readFloat();
        color.y = buffer.readFloat();
        color.z = buffer.readFloat();
        color.w = buffer.readFloat();
    }

    @Override
    public String toString() {
        return "PacketPlayerCreated{" + "id=" + id + ", display=" + display + ", color=" + color + '}';
    }
}
