package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataSerializable;
import gamelauncher.engine.render.GameItem;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.MassType;

public class Entity implements DataSerializable {

    public GameItem gameItem;
    public GameItem.GameItemModel model;

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
