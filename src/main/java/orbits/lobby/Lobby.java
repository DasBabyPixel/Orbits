package orbits.lobby;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import orbits.data.AvailableData;
import orbits.data.Ball;
import orbits.data.Player;
import orbits.data.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lobby {
    private final AvailableData availableData = new AvailableData();
    private Level level;
    private final List<Player> players = new ArrayList<>();
    private final Int2ObjectMap<Ball> balls = new Int2ObjectLinkedOpenHashMap<>();

    public AvailableData availableData() {
        return availableData;
    }

    public void start() {
        level = availableData.level;
    }

    public Int2ObjectMap<Ball> balls() {
        return balls;
    }

    public List<Player> players() {
        return players;
    }

    public Level level() {
        return level;
    }
}
