package orbits.network.server;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;

public class PacketBallTrail extends Packet {
    public int ballId;
    public int playerId;

    public PacketBallTrail() {
        super("orbits_ball_trail");
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeInt(ballId);
        buffer.writeInt(playerId);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        ballId = buffer.readInt();
        playerId = buffer.readInt();
    }
}
