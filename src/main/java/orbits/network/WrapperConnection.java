package orbits.network;

import de.dasbabypixel.api.property.Property;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.NetworkAddress;
import gamelauncher.engine.network.NetworkClient;
import gamelauncher.engine.network.packet.Packet;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.Key;
import gamelauncher.engine.util.function.GameSupplier;
import java8.util.concurrent.CompletableFuture;

public class WrapperConnection implements Connection {
    protected final Connection handle;

    public WrapperConnection(Connection handle) {
        this.handle = handle;
    }

    public Connection handle() {
        return handle;
    }

    @Override
    public void startTracking() {
        handle.startTracking();
    }

    @Override
    public void stopTracking() {
        handle.stopTracking();
    }

    @Override
    public NetworkAddress localAddress() {
        return handle.localAddress();
    }

    @Override
    public NetworkAddress remoteAddress() {
        return handle.remoteAddress();
    }

    @Override
    public Property<State> state() {
        return handle.state();
    }

    @Override
    public NetworkClient networkClient() {
        return handle.networkClient();
    }

    @Override
    public <T extends Packet> void addHandler(Class<T> packetType, PacketHandler<T> handler) {
        handle.addHandler(packetType, handler);
    }

    @Override
    public <T extends Packet> void removeHandler(Class<T> packetType, PacketHandler<T> handler) {
        handle.removeHandler(packetType, handler);
    }

    @Override
    public StateEnsurance ensureState(State state) throws GameException {
        return handle.ensureState(state);
    }

    @Override
    public void sendPacket(Packet packet) {
        handle.sendPacket(packet);
    }

    @Override
    public CompletableFuture<Void> sendPacketAsync(Packet packet) {
        return handle.sendPacketAsync(packet);
    }

    @Override
    public void storeValue(Key key, Object value) {
        handle.storeValue(key, value);
    }

    @Override
    public <T> T storedValue(Key key) {
        return handle.storedValue(key);
    }

    @Override
    public <T> T storedValue(Key key, GameSupplier<T> defaultSupplier) {
        return handle.storedValue(key, defaultSupplier);
    }

    @Override
    public CompletableFuture<Void> cleanup() throws GameException {
        return handle.cleanup();
    }

    @Override
    public boolean cleanedUp() {
        return handle.cleanedUp();
    }

    @Override
    public CompletableFuture<Void> cleanupFuture() {
        return handle.cleanupFuture();
    }
}
