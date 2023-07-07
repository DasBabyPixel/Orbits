package orbits.server.network;

import gamelauncher.engine.network.Connection;
import gamelauncher.netty.standalone.packet.s2c.PacketClientConnected;
import orbits.server.OrbitsServer;
import orbits.server.handler.ServerHandler;
import org.jetbrains.annotations.NotNull;

public class ClientConnectedHandler extends ServerHandler<PacketClientConnected> {
    public ClientConnectedHandler(OrbitsServer server) {
        super(server);
    }

    @Override
    public void receivePacket0(@NotNull Connection connection, @NotNull PacketClientConnected packet) {

    }
}
