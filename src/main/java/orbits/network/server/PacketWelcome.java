package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketWelcome extends Packet {

    public PacketWelcome() {
        super("orbits_welcome");
    }

    @Override
    protected void write0(DataBuffer buffer) {
    }

    @Override
    protected void read0(DataBuffer buffer) {
    }
}
