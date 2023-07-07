package orbits.network.client;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketHello extends Packet {
    public PacketHello() {
        super("orbits_hello");
    }

    @Override
    protected void write0(DataBuffer buffer) {

    }

    @Override
    protected void read0(DataBuffer buffer) {

    }
}
