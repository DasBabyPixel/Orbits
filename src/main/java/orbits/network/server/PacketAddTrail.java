package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketAddTrail extends Packet {
    public int entityId;
    public int trailId;

    public PacketAddTrail() {
        super("orbits_add_trail");
    }

    public PacketAddTrail(int entityId, int trailId) {
        this();
        this.entityId = entityId;
        this.trailId = trailId;
    }

    public PacketAddTrail(String key, int entityId, int trailId) {
        super(key);
        this.entityId = entityId;
        this.trailId = trailId;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeInt(entityId);
        buffer.writeInt(trailId);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        entityId = buffer.readInt();
        trailId = buffer.readInt();
    }
}
