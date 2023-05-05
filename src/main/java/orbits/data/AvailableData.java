package orbits.data;

import orbits.data.level.Level;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AvailableData {

    public Level level = null;

    public boolean complete() {
        return level != null;
    }
}
