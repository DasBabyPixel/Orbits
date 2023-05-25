package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import orbits.lobby.Lobby;
import org.dyn4j.geometry.Vector2;

public class Player extends Ball {

    public static final float DODGE_SPEED = 4;
    public static final long DODGE_DURATION = 400;
    private final DoubleList positions = new DoubleArrayList();
    private float dodgeMultiplier = 1;
    private long dodgeMultiplierApplied = Long.MAX_VALUE;
    private int trail;

    public DoubleList positions() {
        return positions;
    }

    public int trail() {
        return trail;
    }

    public void trail(int trail) {
        this.trail = trail;
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
            motion().x(lobby.toLocalSpaceX(body.getLinearVelocity().x));
            motion().y(body.getLinearVelocity().y);
        }
    }

    public float calculateSpeed(Lobby lobby) {
        return lobby.speed() * dodgeMultiplier;
    }

    public void dodgeMultiplierApplied(long dodgeMultiplierApplied) {
        this.dodgeMultiplierApplied = dodgeMultiplierApplied;
    }
}
