package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataSerializable;

public class Entity implements DataSerializable {

    private int entityId;

    public int entityId() {
        return entityId;
    }

    public void entityId(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void write(DataBuffer buffer) {
        buffer.writeInt(entityId);
    }

    @Override
    public void read(DataBuffer buffer) {
        entityId = buffer.readInt();
    }
}
