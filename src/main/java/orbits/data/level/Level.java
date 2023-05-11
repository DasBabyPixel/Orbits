package orbits.data.level;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataSerializable;
import orbits.data.Orbit;
import orbits.data.Position;
import orbits.data.Wall;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Level implements DataSerializable {

    private final List<Orbit> orbits = new ArrayList<>();
    private final List<StartPosition> startPositions = new ArrayList<>();
    private final List<Position> wallPositions = new ArrayList<>();
    private final List<Wall> walls = new ArrayList<>();
    private final int dataVersion = 2;
    private UUID uuid;
    private float aspectRatioWpH = 16F / 9F;
    private long checksum = -1L; // Not serialized, set and read by code

    public int dataVersion() {
        return dataVersion;
    }

    public UUID uuid() {
        return uuid;
    }

    public void uuid(UUID uuid) {
        this.uuid = uuid;
    }

    public List<Orbit> orbits() {
        return orbits;
    }

    public List<StartPosition> startPositions() {
        return startPositions;
    }

    public List<Position> wallPositions() {
        return wallPositions;
    }

    /**
     * @return width/height ratio
     */
    public float aspectRatioWpH() {
        return aspectRatioWpH;
    }

    public void aspectRatioWpH(float aspectRatioWpH) {
        this.aspectRatioWpH = aspectRatioWpH;
    }

    public List<Wall> walls() {
        return walls;
    }

    @Override
    public void write(DataBuffer buffer) {
        buffer.writeInt(dataVersion);
        buffer.writeFloat(aspectRatioWpH);
        buffer.writeLong(uuid.getMostSignificantBits());
        buffer.writeLong(uuid.getLeastSignificantBits());
        buffer.writeList(orbits);
        buffer.writeList(startPositions);
        buffer.writeList(wallPositions);
        buffer.writeList(walls);
    }

    @Override
    public void read(DataBuffer buffer) {
        int dataVersion = buffer.readInt();
        if (dataVersion == 1) {
            uuid = new UUID(buffer.readLong(), buffer.readLong());
            buffer.readList(orbits, Orbit::new);
            buffer.readList(startPositions, StartPosition::new);
            buffer.readList(wallPositions, Position::new);
            buffer.readList(walls, () -> new Wall(this));
        } else if (dataVersion == 2) {
            aspectRatioWpH = buffer.readFloat();
            uuid = new UUID(buffer.readLong(), buffer.readLong());
            buffer.readList(orbits, Orbit::new);
            buffer.readList(startPositions, StartPosition::new);
            buffer.readList(wallPositions, Position::new);
            buffer.readList(walls, () -> new Wall(this));
        } else {
            throw new UnsupportedOperationException("Invalid DataVersion");
        }
    }

    public long checksum() {
        return checksum;
    }

    public void checksum(long checksum) {
        this.checksum = checksum;
    }
}
