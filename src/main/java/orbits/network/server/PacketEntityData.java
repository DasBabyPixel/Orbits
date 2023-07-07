package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;
import orbits.data.Vector2;

public class PacketEntityData extends Packet {
    public int entityId;
    public Vector2 motion;
    public Vector2 position;

    public PacketEntityData() {
        this("orbits_entity_data");
    }

    public PacketEntityData(String key) {
        super(key);
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeInt(entityId);
        buffer.write(motion);
        buffer.write(position);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        entityId = buffer.readInt();
        motion = buffer.read(Vector2::new);
        position = buffer.read(Vector2::new);
    }
}
