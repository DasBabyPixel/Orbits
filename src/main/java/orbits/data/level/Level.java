package orbits.data.level;

import gamelauncher.engine.network.packet.BufferObject;
import gamelauncher.engine.network.packet.PacketBuffer;
import orbits.data.Orbit;
import orbits.data.Position;
import orbits.data.Wall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Level implements BufferObject {

    private int dataVersion = 0;
    private final List<Orbit> orbits = new ArrayList<>();
    private final List<StartPosition> startPositions = new ArrayList<>();
    private final List<Position> wallPositions = new ArrayList<>();

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeInt(dataVersion);
        buffer.writeList(orbits);
        buffer.writeList(startPositions);
    }

    @Override
    public void read(PacketBuffer buffer) {
        dataVersion = buffer.readInt();
        buffer.readList(orbits, Orbit::new);
        buffer.readList(startPositions, StartPosition::new);
    }
}
