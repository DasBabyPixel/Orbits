package orbits.network;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;
import orbits.data.level.Level;

public class PacketIngame extends Packet {
    public Level level;

    public PacketIngame() {
        super("ingame");
        level = new Level();
    }

    public PacketIngame(Level level) {
        super("ingame");
        this.level = level;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.write(level);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        buffer.read(level);
    }
}
