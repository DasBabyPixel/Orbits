package orbits.network;

import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.packet.Packet;
import gamelauncher.engine.network.packet.PacketEncoder;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.util.GameException;
import gamelauncher.netty.standalone.packet.s2c.*;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractServerWrapperConnection extends WrapperConnection {
    protected final PacketEncoder encoder;
    private final Lock handlerLock = new ReentrantLock(true);
    private final Map<Class<?>, Collection<HandlerEntry<?>>> handlers = new ConcurrentHashMap<>();
    private final Map<Integer, ReceiverConnection> clientConnections = new ConcurrentHashMap<>();

    public AbstractServerWrapperConnection(Connection connection) {
        super(connection);
        this.encoder = new PacketEncoder(connection.networkClient().packetRegistry());
    }

    protected void loadServer() {
        handle.addHandler(PacketPayloadInC2S.class, (con, packet) -> {
            ReceiverConnection ccon = clientConnections.get(packet.client);
            Packet p = ServerUtils.receivePayload(encoder, con, packet.data);
            ccon.receivePacket(packet);
            serverReceivePacket(ccon, p);
        });
        handle.addHandler(PacketClientConnected.class, (con, packet) -> {
            clientConnections.put(packet.id, createServerClientConnection(packet.id));
            serverClientConnected(clientConnections.get(packet.id));
        });
        handle.addHandler(PacketClientDisconnected.class, (con, packet) -> {
            Connection ccon = clientConnections.remove(packet.id);
            serverClientDisconnected(ccon);
        });
        cleanupFuture().thenRun(() -> {
            for (ReceiverConnection c : clientConnections.values()) {
                try {
                    c.cleanup();
                } catch (GameException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    protected ReceiverConnection createServerClientConnection(int target) {
        throw new UnsupportedOperationException();
    }

    protected void loadClient() {
        handle.addHandler(PacketPayloadInS2C.class, (connection1, packet) -> {
            Packet p = ServerUtils.receivePayload(encoder, connection1, packet.data);
            Collection<HandlerEntry<?>> cc = handlers.get(p.getClass());
            if (cc != null) {
                for (HandlerEntry<?> e : cc) {
                    e.receivePacket(this, p);
                }
            }
        });
        handle.addHandler(PacketKicked.class, (con, packet) -> {
            try {
                cleanup();
            } catch (GameException e) {
                e.printStackTrace();
            }
        });
    }

    protected void serverClientConnected(Connection ccon) {
    }

    protected void serverClientDisconnected(Connection ccon) {
        try {
            ccon.cleanup();
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
    }

    protected void serverReceivePacket(Connection ccon, Packet packet) {
        Collection<HandlerEntry<?>> cc = handlers.get(packet.getClass());
        if (cc != null) {
            for (HandlerEntry<?> e : cc) {
                e.receivePacket(ccon, packet);
            }
        }
    }

    @Override
    public <T extends Packet> void addHandler(Class<T> packetType, PacketHandler<T> handler) {
        handlerLock.lock();
        if (!handlers.containsKey(packetType)) {
            handlers.put(packetType, ConcurrentHashMap.newKeySet());
        }
        handlers.get(packetType).add(new HandlerEntry<>(packetType, handler));
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
