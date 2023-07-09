package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;
import orbits.data.Vector3;

public class PacketPlayerCreated extends Packet {
    public int id;
    public char display;
    public Vector3 color;

    public PacketPlayerCreated() {
        super("player_created");
    }

    public PacketPlayerCreated(int id, char display, Vector3 color) {
        this();
        this.id = id;
        this.display = display;
        this.color = color;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeInt(id);
        buffer.writeInt(display);
        buffer.write(color);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        id = buffer.readInt();
        display = (char) buffer.readInt();
        color = buffer.read(Vector3::new);
    }

    @Override
    public String toString() {
        return "PacketPlayerCreated{" + "id=" + id + ", display=" + display + ", color=" + color + '}';
    }
}
