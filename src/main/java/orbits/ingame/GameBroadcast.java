package orbits.ingame;

import orbits.data.Entity;

public interface GameBroadcast {

    void update(Entity entity);

    void addTrail(int entityOwner, int entityTrail);

    void removed(int entityId);

    void removeTrail(int entityOwner, int entityTrail);
}
