package orbits.server.network;

import gamelauncher.engine.network.Connection;
import gamelauncher.netty.standalone.packet.s2c.PacketClientDisconnected;
import orbits.server.OrbitsServer;
import orbits.server.handler.ServerHandler;
import org.jetbrains.annotations.NotNull;

public class ClientDisconnectedHandler extends ServerHandler<PacketClientDisconnected> {
    public ClientDisconnectedHandler(OrbitsServer server) {
        super(server);
    }

    @Override
    public void receivePacket0(@NotNull Connection connection, @NotNull PacketClientDisconnected packet) {
    }
}
