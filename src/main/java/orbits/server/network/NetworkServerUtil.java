package orbits.server.network;

import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.packet.Packet;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.settings.SettingSection;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.logging.Logger;
import gamelauncher.netty.standalone.packet.c2s.PacketConnectToServer;
import java8.util.concurrent.CompletableFuture;
import orbits.network.AbstractServerWrapperConnection;
import orbits.network.ServerUtils;
import orbits.settings.OrbitsSettingSection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NetworkServerUtil {
    private static final Logger logger = Logger.logger();

    public static Connection connect(GameLauncher launcher, String serverId) throws GameException {
        SettingSection section = launcher.settings().getSubSection(OrbitsSettingSection.ORBITS);
        Connection connection;
        try {
            connection = launcher.networkClient().connect(new URI(section.<String>getSetting(OrbitsSettingSection.SERVER_URL).getValue()));
        } catch (URISyntaxException e) {
            throw GameException.wrap(e);
        }
        Connection.State state = connection.ensureState(Connection.State.CONNECTED).timeoutAfter(5, TimeUnit.SECONDS).await();
        if (state == Connection.State.CONNECTED) {
            CompletableFuture<Integer> connectFuture = new CompletableFuture<>();
            PacketHandler<PacketConnectToServer.Response> responseHandler = (con, packet) -> connectFuture.complete(packet.code);
            connection.addHandler(PacketConnectToServer.Response.class, responseHandler);
            connection.sendPacket(new PacketConnectToServer(serverId));
            try {
                int code = connectFuture.get(5, TimeUnit.SECONDS);
                if (code != PacketConnectToServer.Response.SUCCESS) {
                    connection.cleanup();
                    logger.error("Failed to connect: " + code);
                    return null;
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error(e);
                connection.cleanup();
                return null;
            }
            Connection finalConnection = connection;
            Connection con = new AbstractServerWrapperConnection(finalConnection) {
                {
                    loadClient();
                }

                @Override
                public void sendPacket(Packet packet) {
                    ServerUtils.clientSendPacket(encoder, finalConnection, packet);
                }

                @Override
                public CompletableFuture<Void> sendPacketAsync(Packet packet) {
                    return ServerUtils.clientSendPacketAsync(encoder, finalConnection, packet);
                }
            };
            return con;
        } else {
            connection.cleanup();
            return null;
        }
    }
}
