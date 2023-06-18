package orbits.network;

import gamelauncher.engine.network.NetworkClient;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.network.packet.PacketNotRegisteredException;
import gamelauncher.engine.util.GameException;
import gamelauncher.netty.standalone.StandaloneServer;
import orbits.OrbitsGame;
import orbits.data.level.Level;

import java.util.UUID;

/**
 * TODO: Multiplayer
 */
public class PacketHandlers {
    private OrbitsGame orbits;
    private final PacketHandler<LevelChecksumPacket> levelChecksumPacket = (connection, packet) -> {
        final UUID levelId = packet.levelId;
        final long checksum = packet.checksum;
        Level level;
        try {
            level = orbits.levelStorage().findLevel(levelId, checksum);
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
        orbits.launcher().gameThread().runLater(() -> {
            if (level == null) {
                // TODO: send packet to request level
            } else {
                orbits.currentLobby().availableData().level = level;
                checkComplete();
            }
        });
    };
    private final PacketHandler<LevelPacket> levelPacket = (connection, packet) -> {
        Level level = packet.level;
        try {
            orbits.levelStorage().saveLevel(level);
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
        orbits.launcher().gameThread().runLater(() -> {
            orbits.currentLobby().availableData().level = level;
            checkComplete();
        });
    };
    private final PacketHandler<ReadyToPlayPacket> readyToPlayPacket = (connection, packet) -> {
        orbits.launcher().gameThread().runLater(() -> {

        });
    };
    private final PacketHandler<RequestLevelPacket> requestLevelPacket = (connection, packet) -> {
        orbits.launcher().gameThread().runLater(() -> {

        });
    };
    private final PacketHandler<StartPacket> startPacket = (connection, packet) -> orbits.launcher().gameThread().runLater(() -> orbits.currentLobby().start(orbits));
    private NetworkClient client;

    public PacketHandlers(OrbitsGame orbits) {
        this.orbits = orbits;
        this.client = orbits.launcher().networkClient();
    }

    public void registerHandlers() {
        client.packetRegistry().register(LevelChecksumPacket.class, LevelChecksumPacket::new);
        client.addHandler(LevelChecksumPacket.class, levelChecksumPacket);
        client.packetRegistry().register(LevelPacket.class, LevelPacket::new);
        client.addHandler(LevelPacket.class, levelPacket);
        client.packetRegistry().register(StartPacket.class, StartPacket::new);
        client.addHandler(StartPacket.class, startPacket);
        client.packetRegistry().register(ReadyToPlayPacket.class, ReadyToPlayPacket::new);
        client.addHandler(ReadyToPlayPacket.class, readyToPlayPacket);
        client.packetRegistry().register(RequestLevelPacket.class, RequestLevelPacket::new);
        client.addHandler(RequestLevelPacket.class, requestLevelPacket);

        client.packetRegistry().register(PacketPlayerCreated.class, PacketPlayerCreated::new);
        client.packetRegistry().register(PacketPlayerDeleted.class, PacketPlayerDeleted::new);
        client.packetRegistry().register(NewPlayerPacket.class, NewPlayerPacket::new);
        client.packetRegistry().register(DeletePlayerPacket.class, DeletePlayerPacket::new);
        client.packetRegistry().register(PacketIngame.class, PacketIngame::new);
        client.packetRegistry().register(PacketPress.class, PacketPress::new);
        client.packetRegistry().register(PacketDone.class, PacketDone::new);

        StandaloneServer.registerPackets(client.packetRegistry());

    }

    public void unregisterHandlers() throws PacketNotRegisteredException {
        client.packetRegistry().unregister(LevelChecksumPacket.class);
        client.removeHandler(LevelChecksumPacket.class, levelChecksumPacket);
        client.packetRegistry().unregister(LevelPacket.class);
        client.removeHandler(LevelPacket.class, levelPacket);
        client.packetRegistry().unregister(StartPacket.class);
        client.removeHandler(StartPacket.class, startPacket);
        client.packetRegistry().unregister(ReadyToPlayPacket.class);
        client.removeHandler(ReadyToPlayPacket.class, readyToPlayPacket);
        client.packetRegistry().unregister(RequestLevelPacket.class);
        client.removeHandler(RequestLevelPacket.class, requestLevelPacket);

        client.packetRegistry().unregister(PacketPlayerCreated.class);
        client.packetRegistry().unregister(PacketPlayerDeleted.class);
        client.packetRegistry().unregister(NewPlayerPacket.class);
        client.packetRegistry().unregister(DeletePlayerPacket.class);
        client.packetRegistry().unregister(PacketIngame.class);
        client.packetRegistry().unregister(PacketPress.class);
        client.packetRegistry().unregister(PacketDone.class);

    }

    private void checkComplete() {
        if (orbits.currentLobby().availableData().complete()) {
            // TODO send ReadToPlayPacket

        }
    }

}
