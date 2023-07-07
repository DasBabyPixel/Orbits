package orbits.server.network;

import de.dasbabypixel.api.property.Property;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.NetworkAddress;
import gamelauncher.engine.network.NetworkClient;
import gamelauncher.engine.network.packet.Packet;
import gamelauncher.engine.network.packet.PacketEncoder;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.resource.AbstractGameResource;
import gamelauncher.engine.settings.SettingSection;
import gamelauncher.engine.util.GameException;
import gamelauncher.netty.standalone.packet.c2s.PacketRequestServerId;
import gamelauncher.netty.standalone.packet.s2c.PacketClientConnected;
import gamelauncher.netty.standalone.packet.s2c.PacketClientDisconnected;
import java8.util.concurrent.CompletableFuture;
import orbits.OrbitsGame;
import orbits.data.level.Level;
import orbits.network.AbstractServerWrapperConnection;
import orbits.network.ReceiverConnection;
import orbits.network.ServerUtils;
import orbits.server.OrbitsServer;
import orbits.settings.OrbitsSettingSection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NetworkServer extends OrbitsServer {
    protected String serverId;
    protected Connection rawConnection;
    protected ClientConnectedHandler clientConnectedHandler;
    protected ClientDisconnectedHandler clientDisconnectedHandler;

    public NetworkServer(OrbitsGame orbits, Level level) {
        super(orbits, level);
    }

    @Override
    protected Connection createConnection() throws GameException {
        SettingSection section = launcher.settings().getSubSection(OrbitsSettingSection.ORBITS);
        try {
            rawConnection = launcher.networkClient().connect(new URI(section.<String>getSetting(OrbitsSettingSection.SERVER_URL).getValue()));
        } catch (URISyntaxException e) {
            throw GameException.wrap(e);
        }
        CompletableFuture<String> serverIdFuture = new CompletableFuture<>();
        PacketHandler<PacketRequestServerId.Response> requestHandler = (con, packet) -> serverIdFuture.complete(packet.id);
        rawConnection.addHandler(PacketRequestServerId.Response.class, requestHandler);
        Connection.State state = rawConnection.ensureState(Connection.State.CONNECTED).timeoutAfter(5, TimeUnit.SECONDS).await();
        if (state != Connection.State.CONNECTED) {
            stop();
            return null;
        }
        rawConnection.sendPacket(new PacketRequestServerId());
        try {
            serverId = serverIdFuture.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.errorf("Error while requesting server id: %s", e);
            stop();
            return null;
        }
        rawConnection.addHandler(PacketClientConnected.class, clientConnectedHandler = new ClientConnectedHandler(this));
        rawConnection.addHandler(PacketClientDisconnected.class, clientDisconnectedHandler = new ClientDisconnectedHandler(this));
        Connection connection = new AbstractServerWrapperConnection(rawConnection) {
            {
                loadServer();
            }

            @Override
            protected ReceiverConnection createServerClientConnection(int target) {
                return new ServerClientConnection(networkClient(), rawConnection, serverId, target, encoder);
            }

            @Override
            public void sendPacket(Packet packet) {
                ServerUtils.serverSendPacketAll(encoder, serverId, rawConnection, packet);
            }

            @Override
            public CompletableFuture<Void> sendPacketAsync(Packet packet) {
                return ServerUtils.serverSendPacketAllAsync(encoder, serverId, rawConnection, packet);
            }
        };
        return connection;
    }

    @Override
    protected void stop0() throws GameException {
        if (rawConnection != null) rawConnection.cleanup();
        super.stop0();
    }

    public String serverId() {
        return serverId;
    }

    private static class ServerClientConnection extends AbstractGameResource implements ReceiverConnection {
        private final NetworkClient client;
        private final Connection rawConnection;
        private final String serverId;
        private final int target;
        private final PacketEncoder encoder;
        private final Lock handlerLock = new ReentrantLock(true);
        private final Map<Class<?>, Collection<HandlerEntry<?>>> handlers = new ConcurrentHashMap<>();
        private final Property<State> state = Property.withValue(State.CONNECTED);

        public ServerClientConnection(NetworkClient client, Connection rawConnection, String serverId, int target, PacketEncoder encoder) {
            this.client = client;
            this.rawConnection = rawConnection;
            this.serverId = serverId;
            this.target = target;
            this.encoder = encoder;
        }

        @Override
        public NetworkAddress localAddress() {
            return rawConnection.localAddress();
        }

        @Override
        public NetworkAddress remoteAddress() {
            return rawConnection.remoteAddress();
        }

        @Override
        public Property<State> state() {
            return state;
        }

        @Override
        public NetworkClient networkClient() {
            return client;
        }

        @Override
        public <T extends Packet> void addHandler(Class<T> packetTpye, PacketHandler<T> handler) {
            handlerLock.lock();
            if (!handlers.containsKey(packetTpye)) {
                handlers.put(packetTpye, ConcurrentHashMap.newKeySet());
            }
            handlers.get(packetTpye).add(new HandlerEntry<>(packetTpye, handler));
            handlerLock.unlock();
        }

        @Override
        public <T extends Packet> void removeHandler(Class<T> packetType, PacketHandler<T> handler) {
            handlerLock.lock();
            if (handlers.containsKey(packetType)) {
                Collection<HandlerEntry<?>> col = handlers.get(packetType);
                col.removeIf(he -> he.clazz() == packetType && he.handler == handler);
                if (col.isEmpty()) handlers.remove(packetType);

            }
            handlerLock.unlock();
        }

        @Override
        public StateEnsurance ensureState(State state) throws GameException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendPacket(Packet packet) {
            ServerUtils.serverSendPacket(encoder, serverId, rawConnection, target, packet);
        }

        @Override
        public CompletableFuture<Void> sendPacketAsync(Packet packet) {
            return ServerUtils.serverSendPacketAsync(encoder, serverId, rawConnection, target, packet);
        }

        @Override
        protected CompletableFuture<Void> cleanup0() throws GameException {
            return null;
        }

        @Override
        public void receivePacket(Packet packet) {
            Collection<HandlerEntry<?>> col;
            try {
                handlerLock.lock();
                col = handlers.get(packet.getClass());
            } finally {
                handlerLock.unlock();
            }
            if (col == null) return;
            for (HandlerEntry<?> h : col) {
                h.receivePacket(this, packet);
            }
        }

        private static class HandlerEntry<T extends Packet> {
            private final Class<T> clazz;
            private final PacketHandler<T> handler;

            public HandlerEntry(Class<T> clazz, PacketHandler<T> handler) {
                this.clazz = clazz;
                this.handler = handler;
            }

            public Class<T> clazz() {
                return clazz;
            }

            public void receivePacket(Connection connection, Object packet) {
                handler.receivePacket(connection, clazz.cast(packet));
            }
        }
    }
}
