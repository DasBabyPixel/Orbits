package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.render.GameItem;
import orbits.lobby.Lobby;

public class Ball extends Entity {
    private final Position position = new Position(0, 0);
    private final Vector2 motion = new Vector2();
    private final Vector3 color = new Vector3();
    public GameItem ballItem;
    public boolean projectile = false;
    private Ball prev;
    private Ball pull;
    private int ownerId;

    public Vector2 motion() {
        return motion;
    }

    public void updateMotion(Lobby lobby) {
        motion.x(lobby.toLocalSpaceX(body.getLinearVelocity().x));
        motion.y(body.getLinearVelocity().y);
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

    public Ball prev() {
        return prev;
    }

    public void prev(Ball prev) {
        this.prev = prev;
    }

    public int ownerId() {
        return ownerId;
    }

    public void ownerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
