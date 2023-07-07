package orbits.network;

import gamelauncher.engine.network.NetworkClient;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.network.packet.PacketNotRegisteredException;
import gamelauncher.engine.util.GameException;
import gamelauncher.netty.standalone.StandaloneServer;
import orbits.OrbitsGame;
import orbits.data.level.Level;
import orbits.network.client.*;
import orbits.network.server.*;

import java.util.UUID;

/**
 * TODO: Multiplayer
 */
public class PacketHandlers {
    private OrbitsGame orbits;
    private final PacketHandler<PacketReadyToPlay> readyToPlayPacket = (connection, packet) -> {
        orbits.launcher().gameThread().submit(() -> {

        });
    };
    private final PacketHandler<PacketStart> startPacket = (connection, packet) -> orbits.launcher().gameThread().submit(() -> orbits.currentLobby().start(orbits));
    private NetworkClient client;

    public PacketHandlers(OrbitsGame orbits) {
        this.orbits = orbits;
        this.client = orbits.launcher().networkClient();
    }

    public void registerHandlers() {
        client.packetRegistry().register(PacketLevelChecksum.class, PacketLevelChecksum::new);
        client.packetRegistry().register(PacketLevel.class, PacketLevel::new);
        client.packetRegistry().register(PacketStart.class, PacketStart::new);
        client.addHandler(PacketStart.class, startPacket);
        client.packetRegistry().register(PacketReadyToPlay.class, PacketReadyToPlay::new);
        client.addHandler(PacketReadyToPlay.class, readyToPlayPacket);
        client.packetRegistry().register(PacketRequestLevel.class, PacketRequestLevel::new);

        client.packetRegistry().register(PacketPlayerCreated.class, PacketPlayerCreated::new);
        client.packetRegistry().register(PacketPlayerDeleted.class, PacketPlayerDeleted::new);
        client.packetRegistry().register(PacketNewPlayer.class, PacketNewPlayer::new);
        client.packetRegistry().register(PacketDeletePlayer.class, PacketDeletePlayer::new);
        client.packetRegistry().register(PacketIngame.class, PacketIngame::new);
        client.packetRegistry().register(PacketPress.class, PacketPress::new);

        StandaloneServer.registerPackets(client.packetRegistry());

    }

    public void unregisterHandlers() throws PacketNotRegisteredException {
        client.packetRegistry().unregister(PacketLevelChecksum.class);
        client.removeHandler(PacketLevelChecksum.class, levelChecksumPacket);
        client.packetRegistry().unregister(PacketLevel.class);
        client.removeHandler(PacketLevel.class, levelPacket);
        client.packetRegistry().unregister(PacketStart.class);
        client.removeHandler(PacketStart.class, startPacket);
        client.packetRegistry().unregister(PacketReadyToPlay.class);
        client.removeHandler(PacketReadyToPlay.class, readyToPlayPacket);
        client.packetRegistry().unregister(PacketRequestLevel.class);
        client.removeHandler(PacketRequestLevel.class, requestLevelPacket);

        client.packetRegistry().unregister(PacketPlayerCreated.class);
        client.packetRegistry().unregister(PacketPlayerDeleted.class);
        client.packetRegistry().unregister(PacketNewPlayer.class);
        client.packetRegistry().unregister(PacketDeletePlayer.class);
        client.packetRegistry().unregister(PacketIngame.class);
        client.packetRegistry().unregister(PacketPress.class);

    }
}
