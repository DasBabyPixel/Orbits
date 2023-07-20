package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import orbits.data.level.Level;
import orbits.ingame.Game;

public class Player extends Ball {

    public static final float DODGE_SPEED = 2;
    public static final long DODGE_DURATION = 800;
    private final DoubleArrayList positions = new DoubleArrayList();
    private Orbit currentOrbit;
    private int currentOrbitsId = -1;
    private boolean orbiting = false;
    private double orbitingTheta;
    private float dodgeMultiplier = 1;
    private long dodgeMultiplierApplied = Long.MAX_VALUE;

    public DoubleArrayList positions() {
        return positions;
    }

    public void addTrail(Ball ball) {
        ball.pull(null);
        super.trailEnd().pull(ball);
        ball.ownerId(entityId());
        ball.color().set(color());
    }

    public Ball removeTrail() {
        Ball b = trailEnd();
        if (b == null) return null;
        b.prev().pull(null);
        b.ownerId(0);
        return b;
    }

    @Override
    public void write(DataBuffer buffer) {
        super.write(buffer);
        buffer.writeByte((byte) (orbiting ? 1 : 0));
        buffer.writeDouble(orbitingTheta);
        buffer.writeInt(currentOrbitsId);
        buffer.writeFloat(dodgeMultiplier);
        buffer.writeLong(dodgeMultiplierApplied);
    }

    @Override
    public void read(DataBuffer buffer) {
        super.read(buffer);
        orbiting = buffer.readByte() == 1;
        orbitingTheta = buffer.readDouble();
        currentOrbitsId = buffer.readInt();
        dodgeMultiplier = buffer.readFloat();
        dodgeMultiplierApplied = buffer.readLong();
    }

    public float dodgeMultiplier() {
        return dodgeMultiplier;
    }

    public long dodgeMultiplierApplied() {
        return dodgeMultiplierApplied;
    }

    public void dodgeMultiplier(Game game, float dodgeMultiplier) {
        this.dodgeMultiplier = dodgeMultiplier;
        if (body != null) {
            body.getLinearVelocity().normalize();
            body.getLinearVelocity().multiply(calculateSpeed(game));
            updateMotion(game);
        }
    }

    public int currentOrbitsId() {
        return currentOrbitsId;
    }

    public Orbit currentOrbit() {
        return currentOrbit;
    }

    public void currentOrbit(Level level, int id) {
        this.currentOrbitsId = id;
        this.currentOrbit = level.orbits().get(id);
    }

    public void currentOrbit(Level level, Orbit currentOrbit) {
        if (currentOrbit != null) {
            this.currentOrbitsId = level.orbits().indexOf(currentOrbit);
            this.currentOrbit = currentOrbit;
        } else {
            this.currentOrbit = null;
            this.currentOrbitsId = -1;
        }
    }

    public boolean orbiting() {
        return orbiting;
    }

    public double orbitingTheta() {
        return orbitingTheta;
    }

    public void orbiting(boolean orbiting, double theta) {
        if (orbiting) body.setLinearVelocity(0, 0);
        this.orbiting = orbiting;
        this.orbitingTheta = theta;
    }

    public Ball trailEnd() {
        Ball t = super.trailEnd();
        return t == this ? null : t;
    }

    public float calculateSpeed(Game game) {
        return game.speed() * dodgeMultiplier;
    }

    public void dodgeMultiplierApplied(long dodgeMultiplierApplied) {
        this.dodgeMultiplierApplied = dodgeMultiplierApplied;
    }
}
