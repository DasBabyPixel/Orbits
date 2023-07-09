package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketPaused extends Packet {
    public boolean paused;

    public PacketPaused() {
        super("orbits_paused");
    }

    public PacketPaused(boolean paused) {
        this();
        this.paused = paused;
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeByte((byte) (paused ? 1 : 0));
    }

    @Override
    protected void read0(DataBuffer buffer) {
        paused = buffer.readByte() == 1;
    }
}
