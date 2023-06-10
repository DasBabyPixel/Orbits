package orbits.network;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketDone extends Packet {
    public PacketDone() {
        super("done");
    }

    @Override
    protected void write0(DataBuffer buffer) {

    }

    @Override
    protected void read0(DataBuffer buffer) {

    }
}
