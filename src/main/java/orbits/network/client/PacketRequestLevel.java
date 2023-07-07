package orbits.network.client;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;
import orbits.network.server.PacketLevel;

import java.util.UUID;

/**
 * This packet is sent by a player to request a {@link PacketLevel} from the game owner. The game owner MUST respond.
 */
public class PacketRequestLevel extends Packet {
    public UUID levelId;

    public PacketRequestLevel(UUID levelId) {
        super("orbits_request_level");
        this.levelId = levelId;
    }

    public PacketRequestLevel() {
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
