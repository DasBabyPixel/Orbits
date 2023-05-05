package orbits.network;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.network.packet.Packet;
import orbits.data.Vector2;

public class BallMotionPacket extends Packet {
    private int entityId;
    private final Vector2 motion = new Vector2();

    public BallMotionPacket() {
        super("orbits_ball_motion");
    }

    @Override
    protected void write0(DataBuffer buffer) {
        buffer.writeInt(entityId);
        buffer.write(motion);
    }

    @Override
    protected void read0(DataBuffer buffer) {
        entityId = buffer.readInt();
        buffer.read(motion);
    }
}
