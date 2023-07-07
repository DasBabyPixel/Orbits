package orbits.network.client;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

/**
 * Whenever a client sends this packet it means he is ready to play, everything is set up on his side (Like {@link orbits.data.level.Level} downloaded, perhaps custom player skins, etc.
 */
public class PacketReadyToPlay extends Packet {
    public PacketReadyToPlay() {
        super("orbits_ready_to_play");
    }

    @Override
    protected void write0(DataBuffer buffer) {

    }

    @Override
    protected void read0(DataBuffer buffer) {

    }
}
