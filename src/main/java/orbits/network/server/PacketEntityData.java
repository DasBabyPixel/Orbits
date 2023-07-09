package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;
import orbits.data.Ball;
import orbits.data.LocalPlayer;
import orbits.data.Player;

public class PacketEntityData extends Packet {
    public Ball entity;

    public PacketEntityData() {
        super("orbits_entity_data");
    }

    public PacketEntityData(Ball entity) {
        this();
        this.entity = entity;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeByte((byte) (entity instanceof LocalPlayer ? 2 : entity instanceof Player ? 1 : 0));
        buffer.write(entity);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        byte b = buffer.readByte();
        entity = buffer.read(b == 2 ? LocalPlayer::new : b == 1 ? Player::new : Ball::new);
    }
}
