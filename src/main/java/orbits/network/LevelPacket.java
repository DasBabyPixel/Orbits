package orbits.network;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;
import orbits.data.level.Level;

/**
 * This client is sent by the game owner to every client that requests it
 */
public class LevelPacket extends Packet {
    public Level level;

    public LevelPacket() {
        this(null);
    }

    public LevelPacket(Level level) {
        super("orbits_level");
        this.level = level;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.write(level);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        buffer.read(level = new Level());
    }
}
