package orbits.network;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

import java.util.UUID;

/**
 * This packet is sent by the game owner to all players when they connect. The clients should validate that they have the level, and if they do NOT have the level they should send a {@link RequestLevelPacket} to the game owner.
 */
public class LevelChecksumPacket extends Packet {
    public UUID levelId;
    public long checksum;

    public LevelChecksumPacket(UUID levelId, long checksum) {
        super("orbits_level_checksum");
        this.levelId = levelId;
        this.checksum = checksum;
    }

    public LevelChecksumPacket() {
        this(null, 0);
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeLong(levelId.getMostSignificantBits());
        buffer.writeLong(levelId.getLeastSignificantBits());
        buffer.writeLong(checksum);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        levelId = new UUID(buffer.readLong(), buffer.readLong());
        checksum = buffer.readLong();
    }
}
