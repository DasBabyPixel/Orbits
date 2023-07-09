package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.render.GameItem;
import orbits.ingame.Game;

public class Ball extends Entity {
    private final Position position = new Position(0, 0);
    private final Vector2 motion = new Vector2();
    private final Vector3 color = new Vector3();
    public GameItem ballItem;
    public boolean projectile = false;
    private Ball trailEnd = this;
    private Ball prev;
    private Ball pull;
    private int ownerId;

    public Vector2 motion() {
        return motion;
    }

    public void updateMotion(Game game) {
        motion.x(game.toLocalSpaceX(body.getLinearVelocity().x));
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
        if (pull != null && pull.prev != this) System.out.println("OUT OF SYNC " + entityId());
        return pull;
    }

    public void pull(Ball pull) {
        if (this.pull != null && this.pull.prev != null) {
            this.pull.prev = null;
        }
        this.pull = pull;
        if (pull != null) {
            if (pull.prev != null) {
                pull.prev.pull = null;
            }
            pull.prev = this;
            trailEnd = pull.trailEnd;
        } else {
            trailEnd = this;
        }
        if (pull != null && pull.prev != this) System.out.println("OUT OF SYNC " + entityId());
        if (prev != null && prev.pull != this) System.out.println("OUT OF SYNC " + entityId());
        Ball b = prev;
        while (b != null) {
            b.trailEnd = trailEnd;
            b = b.prev;
        }
    }

    public Ball trailEnd() {
        return trailEnd;
    }

    public Ball prev() {
        if (prev != null && prev.pull != this) System.out.println("OUT OF SYNC " + entityId());
        return prev;
    }

    public int ownerId() {
        return ownerId;
    }

    public void ownerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
