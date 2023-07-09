package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketEntityRemove extends Packet {
    public int entityId;

    public PacketEntityRemove() {
        super("orbits_entity_remove");
    }

    public PacketEntityRemove(int entityId) {
        this();
        this.entityId = entityId;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeInt(entityId);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        entityId = buffer.readInt();
    }
}
