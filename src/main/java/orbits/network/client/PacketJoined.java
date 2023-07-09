package orbits.network.client;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketJoined extends Packet {
    public PacketJoined() {
        super("orbits_joined");
    }

    @Override
    protected void write0(DataBuffer buffer) {

    }

    @Override
    protected void read0(DataBuffer buffer) {

    }
}
