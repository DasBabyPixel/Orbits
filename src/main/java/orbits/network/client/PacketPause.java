package orbits.network.client;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketPause extends Packet {
    public PacketPause() {
        super("orbits_pause");
    }

    @Override
    protected void write0(DataBuffer buffer) {

    }

    @Override
    protected void read0(DataBuffer buffer) {

    }
}
