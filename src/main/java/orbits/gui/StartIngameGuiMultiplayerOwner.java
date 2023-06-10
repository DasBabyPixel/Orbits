package orbits.gui;

import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.network.server.NetworkServer;
import gamelauncher.engine.network.server.ServerListener;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.Key;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import orbits.OrbitsGame;
import orbits.data.level.Level;
import orbits.network.*;

import java.util.*;

public class StartIngameGuiMultiplayerOwner extends StartIngameGui {
    public static final Key mapped_ids = new Key("mapped_ids");
    private final Collection<Connection> connections = Collections.synchronizedCollection(new HashSet<>());
    private NetworkServer server;
    private final PacketHandler<NewPlayerPacket> h1 = (connection, packet) -> launcher().gameThread().runLater(() -> newPlayer(connection, packet));
    private final PacketHandler<DeletePlayerPacket> h2 = (connection, packet) -> launcher().gameThread().runLater(() -> deletePlayer(connection, packet));

    public StartIngameGuiMultiplayerOwner(Level level, OrbitsGame orbits) throws GameException {
        super(level, orbits);
        server = orbits.launcher().networkClient().newServer();
        server.serverListener(new ServerListener() {
            @Override
            public void connected(Connection connection) {
                connections.add(connection);
            }

            @Override
            public void disconnected(Connection connection) {
                connections.remove(connection);
            }
        });
        launcher().networkClient().addHandler(NewPlayerPacket.class, h1);
        launcher().networkClient().addHandler(DeletePlayerPacket.class, h2);
    }

    private void newPlayer(Connection connection, NewPlayerPacket packet) throws GameException {
        Int2IntMap map = connection.storedValue(mapped_ids, Int2IntOpenHashMap::new);
        if (map.containsKey(packet.id)) return;
        map.put(packet.id, new Random().nextInt(100000000) + 1000000);
        int oid = map.get(packet.id);
        PlayerGui pg = newPlayer(oid, packet.ch);
        connection.sendPacket(new PacketPlayerCreated(packet.id, packet.ch, pg.color));
    }

    private void deletePlayer(Connection connection, DeletePlayerPacket packet) {
        Int2IntMap map = connection.storedValue(mapped_ids, Int2IntOpenHashMap::new);
        if (!map.containsKey(packet.id)) return;
        int oid = map.remove(packet.id);
        removePlayer(oid);
        connection.sendPacket(new PacketPlayerDeleted(packet.id));
    }

    @Override
    protected void doInit() {
        server.start();
    }

    @Override
    protected void preStart() {
        for (Connection connection : connections) {
            connection.sendPacket(new PacketIngame());
        }
        launcher().networkClient().removeHandler(NewPlayerPacket.class, h1);
        launcher().networkClient().removeHandler(DeletePlayerPacket.class, h2);
        lobby.server(server);
        server = null;
    }

    @Override
    public void onClose() throws GameException {
        if (server != null) {
            launcher().networkClient().removeHandler(NewPlayerPacket.class, h1);
            launcher().networkClient().removeHandler(DeletePlayerPacket.class, h2);
            server.cleanup();
            server = null;
        }
    }
}
