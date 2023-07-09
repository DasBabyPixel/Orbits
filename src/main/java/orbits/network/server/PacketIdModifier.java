package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketIdModifier extends Packet {
    public int modifier;

    public PacketIdModifier() {
        super("orbits_id_modifier");
    }

    public PacketIdModifier(int modifier) {
        this();
        this.modifier = modifier;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeInt(modifier);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        modifier = buffer.readInt();
    }
}
