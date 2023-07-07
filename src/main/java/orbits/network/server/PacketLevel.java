package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;
import orbits.data.level.Level;

/**
 * This client is sent by the game owner to every client that requests it
 */
public class PacketLevel extends Packet {
    public Level level;

    public PacketLevel() {
        super("orbits_level");
    }

    public PacketLevel(Level level) {
        this();
        this.level = level;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.write(level);
        buffer.writeLong(level.checksum());
    }

    @Override
    protected void read0(DataBuffer buffer) {
        level = buffer.read(Level::new);
        long checksum = buffer.readLong();
        level.checksum(checksum);
    }
}
