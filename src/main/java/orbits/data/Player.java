package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import orbits.lobby.Lobby;

public class Player extends Ball {

    public static final float DODGE_SPEED = 2;
    public static final long DODGE_DURATION = 800;
    private final DoubleList positions = new DoubleArrayList();
    public Orbit currentOrbit;
    public boolean orbiting = false;
    public double orbitingTheta;
    private float dodgeMultiplier = 1;
    private long dodgeMultiplierApplied = Long.MAX_VALUE;
    private Ball trailEnd;
    private int trail;

    public DoubleList positions() {
        return positions;
    }

    public int trail() {
        return trail;
    }

    public void addTrail(Ball ball) {
        trail++;
        ball.pull(null);
        if (trailEnd != null) {
            trailEnd.pull(ball);
            ball.prev(trailEnd);
        } else {
            pull(ball);
            ball.prev(this);
        }
        trailEnd = ball;
        ball.ownerId(entityId());
        ball.color().set(color());
    }

    public Ball removeTrail() {
        Ball b = trailEnd;
        if (b == null) return null;
        trailEnd = trailEnd.prev();
        b.prev(null);
        trailEnd.pull(null);
        if (trailEnd == this) trailEnd = null;
        b.ownerId(0);
        return b;
    }

    @Override
    public void write(DataBuffer buffer) {
        super.write(buffer);
        buffer.writeInt(trail);
        buffer.writeFloat(dodgeMultiplier);
        buffer.writeLong(dodgeMultiplierApplied);
    }

    @Override
    public void read(DataBuffer buffer) {
        super.read(buffer);
        trail = buffer.readInt();
        dodgeMultiplier = buffer.readFloat();
        dodgeMultiplierApplied = buffer.readLong();
    }

    public float dodgeMultiplier() {
        return dodgeMultiplier;
    }

    public long dodgeMultiplierApplied() {
        return dodgeMultiplierApplied;
    }

    public void dodgeMultiplier(Lobby lobby, float dodgeMultiplier) {
        this.dodgeMultiplier = dodgeMultiplier;
        if (body != null) {
            body.getLinearVelocity().normalize();
            body.getLinearVelocity().multiply(calculateSpeed(lobby));
            updateMotion(lobby);
        }
    }

    public float calculateSpeed(Lobby lobby) {
        return lobby.speed() * dodgeMultiplier;
    }

    public void dodgeMultiplierApplied(long dodgeMultiplierApplied) {
        this.dodgeMultiplierApplied = dodgeMultiplierApplied;
    }
}
