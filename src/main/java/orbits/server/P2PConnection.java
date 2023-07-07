package orbits.server;

import de.dasbabypixel.api.property.Property;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.NetworkAddress;
import gamelauncher.engine.network.NetworkClient;
import gamelauncher.engine.network.packet.Packet;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.resource.AbstractGameResource;
import gamelauncher.engine.util.GameException;
import java8.util.concurrent.CompletableFuture;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class P2PConnection extends AbstractGameResource implements Connection {
    private final Property<State> state = Property.withValue(State.CONNECTED);
    private final NetworkAddress localAddress = new P2PNetworkAddress();
    private final NetworkAddress remoteAddress;
    private final Lock handlerLock = new ReentrantLock(true);
    private final Map<Class<?>, Collection<P2PConnection.HandlerEntry<?>>> handlers = new ConcurrentHashMap<>();
    private final P2PConnection p;

    public P2PConnection() {
        p = new P2PConnection(this);
        remoteAddress = p.localAddress;
    }

    private P2PConnection(P2PConnection p) {
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
    public StateEnsurance ensureState(State state) throws GameException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendPacket(Packet packet) {
        p.receivePacket(packet);
    }

    @Override
    public CompletableFuture<Void> sendPacketAsync(Packet packet) {
        sendPacket(packet);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected CompletableFuture<Void> cleanup0() throws GameException {
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
