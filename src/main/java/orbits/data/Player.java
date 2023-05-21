package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class Player extends Ball {

    private final DoubleList positions = new DoubleArrayList();
    private int trail;

    public DoubleList positions() {
        return positions;
    }

    public int trail() {
        return trail;
    }

    @Override
    public void write(DataBuffer buffer) {
        super.write(buffer);
        buffer.writeInt(trail);
    }

    @Override
    public void read(DataBuffer buffer) {
        super.read(buffer);
        trail = buffer.readInt();
    }
}
