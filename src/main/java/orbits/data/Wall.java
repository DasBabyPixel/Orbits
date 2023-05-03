package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataSerializable;
import orbits.data.level.Level;

public class Wall implements DataSerializable {

    private final Level level;
    private int pos1Index;
    private int pos2Index;

    public Wall(Level level) {
        this.level = level;
    }

    public Level level() {
        return level;
    }

    public int pos1Index() {
        return pos1Index;
    }

    public void pos1Index(int pos1Index) {
        this.pos1Index = pos1Index;
    }

    public int pos2Index() {
        return pos2Index;
    }

    public void pos2Index(int pos2Index) {
        this.pos2Index = pos2Index;
    }

    @Override
    public void write(DataBuffer buffer) {
        buffer.writeInt(pos1Index);
        buffer.writeInt(pos2Index);
    }

    @Override
    public void read(DataBuffer buffer) {
        pos1Index = buffer.readInt();
        pos2Index = buffer.readInt();
    }
}
