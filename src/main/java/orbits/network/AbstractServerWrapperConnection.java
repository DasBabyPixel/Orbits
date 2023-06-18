package orbits.network;

import de.dasbabypixel.api.property.Property;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.NetworkAddress;
import gamelauncher.engine.network.NetworkClient;
import gamelauncher.engine.network.packet.Packet;
import gamelauncher.engine.network.packet.PacketEncoder;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.Key;
import gamelauncher.engine.util.function.GameSupplier;
import gamelauncher.netty.standalone.PacketPayloadInC2S;
import gamelauncher.netty.standalone.PacketPayloadInS2C;
import java8.util.concurrent.CompletableFuture;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractServerWrapperConnection implements Connection {
    protected final PacketEncoder encoder;
    private final Lock handlerLock = new ReentrantLock(true);
    private final Map<Class<?>, Collection<HandlerEntry<?>>> handlers = new ConcurrentHashMap<>();
    protected final Connection connection;

    public AbstractServerWrapperConnection(Connection connection) {
        this.connection = connection;
        this.encoder = new PacketEncoder(connection.networkClient().packetRegistry());
        connection.addHandler(PacketPayloadInS2C.class, (connection1, packet) -> {
            System.out.println(packet);
            Packet p = ServerUtils.receivePayload(encoder, connection1, packet.data);
            Collection<HandlerEntry<?>> cc = handlers.get(p.getClass());
            if (cc != null) {
                for (HandlerEntry<?> e : cc) {
                    e.receivePacket(this, p);
                }
            }
        });
        connection.addHandler(PacketPayloadInC2S.class, (connection1, packet) -> {
            System.out.println(packet);
            System.out.println(Arrays.toString(packet.data));
            Packet p = ServerUtils.receivePayload(encoder, connection1, packet.data);
            Collection<HandlerEntry<?>> cc = handlers.get(p.getClass());
            if (cc != null) {
                for (HandlerEntry<?> e : cc) {
                    e.receivePacket(this, p);
                }
            }
        });
    }

    @Override
    public NetworkAddress localAddress() {
        return connection.localAddress();
    }

    @Override
    public NetworkAddress remoteAddress() {
        return connection.remoteAddress();
    }

    @Override
    public Property<State> state() {
        return connection.state();
    }

    @Override
    public NetworkClient networkClient() {
        return connection.networkClient();
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
        return connection.ensureState(state);
    }

    @Override
    public void startTracking() {
        connection.startTracking();
    }

    @Override
    public void stopTracking() {
        connection.stopTracking();
    }

    @Override
    public void storeValue(Key key, Object value) {
        connection.storeValue(key, value);
    }

    @Override
    public <T> T storedValue(Key key) {
        return connection.storedValue(key);
    }

    @Override
    public <T> T storedValue(Key key, GameSupplier<T> defaultSupplier) {
        return connection.storedValue(key, defaultSupplier);
    }

    @Override
    public CompletableFuture<Void> cleanup() throws GameException {
        return connection.cleanup();
    }

    @Override
    public boolean cleanedUp() {
        return connection.cleanedUp();
    }

    @Override
    public CompletableFuture<Void> cleanupFuture() {
        return connection.cleanupFuture();
    }

    static class HandlerEntry<T extends Packet> {
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
