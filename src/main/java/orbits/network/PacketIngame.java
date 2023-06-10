package orbits.network;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketIngame extends Packet {
    public PacketIngame() {
        super("ingame");
    }

    @Override
    protected void write0(DataBuffer buffer) {

    }

    @Override
    protected void read0(DataBuffer buffer) {

    }
}
