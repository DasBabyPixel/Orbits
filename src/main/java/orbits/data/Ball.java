package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.render.GameItem;

public class Ball extends Entity {
    private final Position position = new Position(0, 0);
    private final Vector2 motion = new Vector2();
    private final Vector3 color = new Vector3();
    private Ball pull;
    public GameItem ballItem;
    private int ownerId;

    public Vector2 motion() {
        return motion;
    }

    public Position position() {
        return position;
    }

    public Vector3 color() {
        return color;
    }

    @Override
    public void write(DataBuffer buffer) {
        super.write(buffer);
        buffer.write(position);
        buffer.write(motion);
        buffer.write(color);
        buffer.writeInt(ownerId);
    }

    @Override
    public void read(DataBuffer buffer) {
        super.read(buffer);
        buffer.read(position);
        buffer.read(motion);
        buffer.read(color);
        ownerId = buffer.readInt();
    }

    public Ball pull() {
        return pull;
    }

    public void pull(Ball pull) {
        this.pull = pull;
    }

    public int ownerId() {
        return ownerId;
    }

    public void ownerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
