package orbits.server.handler;

import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.packet.Packet;
import gamelauncher.engine.network.packet.PacketHandler;
import orbits.server.OrbitsServer;
import org.jetbrains.annotations.NotNull;

public abstract class ServerHandler<T extends Packet> implements PacketHandler<T> {
    public final OrbitsServer server;
    public final boolean serverThread;

    public ServerHandler(OrbitsServer server, boolean serverThread) {
        this.server = server;
        this.serverThread = serverThread;
    }

    public ServerHandler(OrbitsServer server) {
        this(server, true);
    }

    @Override
    public final void receivePacket(@NotNull Connection connection, @NotNull T packet) {
        if (serverThread) server.thread().submit(() -> receivePacket0(connection, packet));
        else receivePacket0(connection, packet);
    }

    protected abstract void receivePacket0(@NotNull Connection connection, @NotNull T packet);
}
