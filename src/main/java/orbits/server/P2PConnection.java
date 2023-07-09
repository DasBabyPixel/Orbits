package orbits.server;

import de.dasbabypixel.api.property.Property;
import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataMemory;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.NetworkAddress;
import gamelauncher.engine.network.NetworkClient;
import gamelauncher.engine.network.packet.*;
import gamelauncher.engine.resource.AbstractGameResource;
import java8.util.concurrent.CompletableFuture;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class P2PConnection extends AbstractGameResource implements Connection {
    private final Property<State> state = Property.withValue(State.CONNECTED);
    private final NetworkAddress localAddress = new P2PNetworkAddress();
    private final NetworkAddress remoteAddress;
    private final Lock handlerLock = new ReentrantLock(true);
    private final Map<Class<?>, Collection<P2PConnection.HandlerEntry<?>>> handlers = new ConcurrentHashMap<>();
    private final P2PConnection p;
    private final PacketEncoder encoder;
    private final PacketRegistry registry;

    public P2PConnection(PacketRegistry registry) {
        this.registry = registry;
        this.encoder = new PacketEncoder(registry);
        p = new P2PConnection(registry, this);
        remoteAddress = p.localAddress;
    }

    private P2PConnection(PacketRegistry registry, P2PConnection p) {
        this.registry = registry;
        this.encoder = p.encoder;
        this.p = p;
        remoteAddress = p.localAddress;
    }

    @Override
    public NetworkAddress localAddress() {
        return null;
    }

    @Override
    public NetworkAddress remoteAddress() {
        return remoteAddress;
    }

    @Override
    public Property<State> state() {
        return state;
    }

    @Override
    public NetworkClient networkClient() {
        return null;
    }

    @Override
    public <T extends Packet> void addHandler(Class<T> packetTpye, PacketHandler<T> handler) {
        handlerLock.lock();
        if (!handlers.containsKey(packetTpye)) {
            handlers.put(packetTpye, ConcurrentHashMap.newKeySet());
        }
        handlers.get(packetTpye).add(new P2PConnection.HandlerEntry<>(packetTpye, handler));
        handlerLock.unlock();
    }

    @Override
    public <T extends Packet> void removeHandler(Class<T> packetType, PacketHandler<T> handler) {
        handlerLock.lock();
        if (handlers.containsKey(packetType)) {
            Collection<P2PConnection.HandlerEntry<?>> col = handlers.get(packetType);
            col.removeIf(he -> he.clazz() == packetType && he.handler == handler);
            if (col.isEmpty()) handlers.remove(packetType);

        }
        handlerLock.unlock();
    }

    @Override
    public StateEnsurance ensureState(State state) {
        State s = this.state.value();
        return new StateEnsurance() {

            @Override
            public StateEnsurance timeoutAfter(long time, TimeUnit unit) {
                return this;
            }

            @Override
            public StateEnsurance timeoutHandler(TimeoutHandler timeoutHandler) {
                return this;
            }

            @Override
            public State await() {
                return s;
            }

            @Override
            public CompletableFuture<State> future() {
                return CompletableFuture.completedFuture(s);
            }
        };
    }

    @Override
    public void sendPacket(Packet packet) {
        try {
            registry.ensureRegistered(packet.getClass());
        } catch (PacketNotRegisteredException e) {
            throw new RuntimeException(e);
        }
        DataMemory memory = new DataMemory();
        DataBuffer db = new DataBuffer(memory);
        try {
            encoder.write(db, packet);
            Packet p = encoder.read(db);
            this.p.receivePacket(p);
        } catch (PacketNotRegisteredException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<Void> sendPacketAsync(Packet packet) {
        sendPacket(packet);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected CompletableFuture<Void> cleanup0() {
        return null;
    }

    private void receivePacket(Packet packet) {
        Collection<P2PConnection.HandlerEntry<?>> col;
        try {
            handlerLock.lock();
            col = handlers.get(packet.getClass());
        } finally {
            handlerLock.unlock();
        }
        if (col == null) return;
        for (P2PConnection.HandlerEntry<?> h : col) {
            h.receivePacket(this, packet);
        }
    }

    public Connection point() {
        return p;
    }

    public static class P2PNetworkAddress implements NetworkAddress {
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
