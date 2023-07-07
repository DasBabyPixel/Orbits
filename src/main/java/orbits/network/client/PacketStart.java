package orbits.network.client;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketStart extends Packet {
    public PacketStart() {
        super("orbits_start");
    }

    @Override
    protected void write0(DataBuffer buffer) {
    }

    @Override
    protected void read0(DataBuffer buffer) {
    }
}
