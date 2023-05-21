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
    private final int dataVersion = 4;
    private final AspectRatio aspectRatio = new AspectRatio(16, 9);
    private UUID uuid;
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
        return aspectRatio.WpH();
    }

    public AspectRatio aspectRatio() {
        return aspectRatio;
    }

    public List<Wall> walls() {
        return walls;
    }

    @Override
    public void write(DataBuffer buffer) {
        buffer.writeInt(dataVersion);
        buffer.write(aspectRatio);
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
            buffer.readList(walls, Wall::new);
        } else if (dataVersion == 2) {
            buffer.readFloat(); // aspect ratio
            uuid = new UUID(buffer.readLong(), buffer.readLong());
            buffer.readList(orbits, Orbit::new);
            buffer.readList(startPositions, StartPosition::new);
            buffer.readList(wallPositions, Position::new);
            buffer.readList(walls, Wall::new);
        } else if (dataVersion == 3) {
            buffer.read(aspectRatio);
            uuid = new UUID(buffer.readLong(), buffer.readLong());
            buffer.readList(orbits, Orbit::new);
            buffer.readList(startPositions, StartPosition::new);
            for (Orbit orbit : orbits) orbit.radius(orbit.radius() / 2);
            buffer.readList(wallPositions, Position::new);
            buffer.readList(walls, Wall::new);
        } else if (dataVersion == 4) {
            buffer.read(aspectRatio);
            uuid = new UUID(buffer.readLong(), buffer.readLong());
            buffer.readList(orbits, Orbit::new);
            buffer.readList(startPositions, StartPosition::new);
            buffer.readList(wallPositions, Position::new);
            buffer.readList(walls, Wall::new);
        } else {
            throw new UnsupportedOperationException("Invalid DataVersion: " + dataVersion);
        }
    }

    public long checksum() {
        return checksum;
    }

    public void checksum(long checksum) {
        this.checksum = checksum;
    }

    public static class AspectRatio implements DataSerializable {
        private int width;
        private int height;
        private float wph;

        public AspectRatio(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public AspectRatio() {
        }

        @Override
        public void write(DataBuffer buffer) {
            buffer.writeInt(width);
            buffer.writeInt(height);
        }

        @Override
        public void read(DataBuffer buffer) {
            width = buffer.readInt();
            height = buffer.readInt();
            wph = (float)width/(float)height;
        }

        public int width() {
            return width;
        }

        public void width(int width) {
            this.width = width;
            wph = (float) width / (float) height;
        }

        public int height() {
            return height;
        }

        public void height(int height) {
            this.height = height;
            wph = (float) width / (float) height;
        }

        public float WpH() {
            return wph;
        }
    }
}
