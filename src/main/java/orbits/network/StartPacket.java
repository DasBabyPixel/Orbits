package orbits.network;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class StartPacket extends Packet {
    public StartPacket() {
        super("orbits_start");
    }

    @Override
    protected void write0(DataBuffer buffer) {
    }

    @Override
    protected void read0(DataBuffer buffer) {
    }
}
