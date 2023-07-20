package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketDodge extends Packet {
    public int playerId;

    public PacketDodge(int playerId) {
        this();
        this.playerId = playerId;
    }

    public PacketDodge() {
        super("orbits_dodge");
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeInt(playerId);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        playerId = buffer.readInt();
    }
}
