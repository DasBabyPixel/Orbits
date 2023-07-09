package orbits.network;

import gamelauncher.engine.network.NetworkClient;
import gamelauncher.netty.standalone.StandaloneServer;
import orbits.OrbitsGame;
import orbits.network.client.*;
import orbits.network.server.*;

/**
 * TODO: Multiplayer
 */
public class PacketHandlers {
    private OrbitsGame orbits;
    private NetworkClient client;

    public PacketHandlers(OrbitsGame orbits) {
        this.orbits = orbits;
        this.client = orbits.launcher().networkClient();
    }

    public void registerHandlers() {
        client.packetRegistry().register(PacketLevelChecksum.class, PacketLevelChecksum::new);
        client.packetRegistry().register(PacketLevel.class, PacketLevel::new);
        client.packetRegistry().register(PacketStart.class, PacketStart::new);
        client.packetRegistry().register(PacketReadyToPlay.class, PacketReadyToPlay::new);
        client.packetRegistry().register(PacketRequestLevel.class, PacketRequestLevel::new);
        client.packetRegistry().register(PacketEntityData.class, PacketEntityData::new);
        client.packetRegistry().register(PacketEntityRemove.class, PacketEntityRemove::new);
        client.packetRegistry().register(PacketPause.class, PacketPause::new);
        client.packetRegistry().register(PacketPaused.class, PacketPaused::new);
        client.packetRegistry().register(PacketIdModifier.class, PacketIdModifier::new);
        client.packetRegistry().register(PacketAddTrail.class, PacketAddTrail::new);
        client.packetRegistry().register(PacketRemoveTrail.class, PacketRemoveTrail::new);
        client.packetRegistry().register(PacketJoined.class, PacketJoined::new);

        client.packetRegistry().register(PacketPlayerCreated.class, PacketPlayerCreated::new);
        client.packetRegistry().register(PacketPlayerDeleted.class, PacketPlayerDeleted::new);
        client.packetRegistry().register(PacketNewPlayer.class, PacketNewPlayer::new);
        client.packetRegistry().register(PacketDeletePlayer.class, PacketDeletePlayer::new);
        client.packetRegistry().register(PacketIngame.class, PacketIngame::new);
        client.packetRegistry().register(PacketPress.class, PacketPress::new);
        client.packetRegistry().register(PacketHello.class, PacketHello::new);
        client.packetRegistry().register(PacketWelcome.class, PacketWelcome::new);

        StandaloneServer.registerPackets(client.packetRegistry());

    }
}
