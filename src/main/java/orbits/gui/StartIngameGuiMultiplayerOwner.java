package orbits.gui;

import gamelauncher.engine.gui.guis.TextGui;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.NetworkAddress;
import gamelauncher.engine.network.packet.Packet;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.settings.SettingSection;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.Key;
import gamelauncher.engine.util.concurrent.Threads;
import gamelauncher.engine.util.text.Component;
import gamelauncher.netty.standalone.PacketClientDisconnected;
import gamelauncher.netty.standalone.PacketRequestServerId;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java8.util.concurrent.CompletableFuture;
import orbits.OrbitsGame;
import orbits.data.level.Level;
import orbits.network.*;
import orbits.settings.OrbitsSettingSection;

import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class StartIngameGuiMultiplayerOwner extends StartIngameGui {
    public static final Key mapped_ids = new Key("mapped_ids");
    private final PacketHandler<NewPlayerPacket> h1 = (connection, packet) -> launcher().gameThread().runLater(() -> newPlayer(connection, packet));
    private final PacketHandler<DeletePlayerPacket> h2 = (connection, packet) -> launcher().gameThread().runLater(() -> deletePlayer(connection, packet));
    private final PacketHandler<PacketClientDisconnected> clientDisconnected = (connection, packet) -> {
        Int2IntMap map = connection.storedValue(mapped_ids);
        if (map == null) return;
        IntIterator it = map.values().intIterator();
        while (it.hasNext()) {
            int id = it.nextInt();
            it.remove();
            removePlayer(id);
        }
    };
    private final TextGui idText;
    private Connection rawConnection;
    private Connection connection;

    public StartIngameGuiMultiplayerOwner(Level level, OrbitsGame orbits) throws GameException {
        super(level, orbits);
        idText = launcher().guiManager().createGui(TextGui.class);
        idText.heightProperty().bind(back.heightProperty());
        idText.xProperty().bind(xProperty().add(widthProperty().subtract(idText.widthProperty()).divide(2)));
        idText.yProperty().bind(back.yProperty());
        idText.color().set(1, 1, 1, 1);
        idText.text().value(Component.text("Loading..."));
        addGUI(idText);
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
        try {
            SettingSection section = launcher().settings().getSubSection(OrbitsSettingSection.ORBITS);
            rawConnection = launcher().networkClient().connect(NetworkAddress.byName(section.<String>getSetting(OrbitsSettingSection.SERVER_HOST).getValue(), section.<Integer>getSetting(OrbitsSettingSection.SERVER_PORT).getValue()));
            CompletableFuture<String> f = new CompletableFuture<>();
            rawConnection.addHandler(PacketRequestServerId.Response.class, (connection, packet) -> f.complete(packet.id));
            rawConnection.addHandler(PacketClientDisconnected.class, clientDisconnected);
            rawConnection.ensureState(Connection.State.CONNECTED).timeoutAfter(5, TimeUnit.SECONDS).await();
            rawConnection.sendPacket(new PacketRequestServerId());
            String serverId = Threads.await(f);
            idText.text().value(Component.text(serverId));

            connection = new AbstractServerWrapperConnection(rawConnection) {
                @Override
                public void sendPacket(Packet packet) {
                    ServerUtils.serverSendPacketAll(encoder, serverId, rawConnection, packet);
                }

                @Override
                public CompletableFuture<Void> sendPacketAsync(Packet packet) {
                    return ServerUtils.serverSendPacketAllAsync(encoder, serverId, rawConnection, packet);
                }
            };

            connection.addHandler(NewPlayerPacket.class, h1);
            connection.addHandler(DeletePlayerPacket.class, h2);
            lobby.serverConnection(connection);
        } catch (UnknownHostException | GameException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void preStart() {
        connection.sendPacket(new PacketIngame());
        connection.removeHandler(NewPlayerPacket.class, h1);
        connection.removeHandler(DeletePlayerPacket.class, h2);
        connection = null;
    }

    @Override
    public void onClose() throws GameException {
        if (rawConnection != null) {
            rawConnection.removeHandler(PacketClientDisconnected.class, clientDisconnected);
        }
        if (connection != null) {
            connection.removeHandler(NewPlayerPacket.class, h1);
            connection.removeHandler(DeletePlayerPacket.class, h2);
        }
    }
}
