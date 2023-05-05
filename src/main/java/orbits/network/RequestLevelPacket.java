package orbits.network;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

import java.util.UUID;

/**
 * This packet is sent by a player to request a {@link LevelPacket} from the game owner. The game owner MUST respond.
 */
public class RequestLevelPacket extends Packet {
    public UUID levelId;

    public RequestLevelPacket(UUID levelId) {
        super("orbits_request_level");
        this.levelId = levelId;
    }

    public RequestLevelPacket() {
        this(null);
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeLong(levelId.getMostSignificantBits());
        buffer.writeLong(levelId.getLeastSignificantBits());
    }

    @Override
    protected void read0(DataBuffer buffer) {
        levelId = new UUID(buffer.readLong(), buffer.readLong());
    }
}
