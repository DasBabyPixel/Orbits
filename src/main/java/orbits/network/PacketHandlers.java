package orbits.network;

import gamelauncher.engine.network.NetworkClient;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.network.packet.PacketNotRegisteredException;
import gamelauncher.engine.util.GameException;
import orbits.OrbitsGame;
import orbits.data.level.Level;

import java.util.UUID;

public class PacketHandlers {
    private OrbitsGame orbits;
    private NetworkClient client;

    private final PacketHandler<LevelChecksumPacket> levelChecksumPacket = packet -> {
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
    private final PacketHandler<LevelPacket> levelPacket = packet -> {
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
    private final PacketHandler<ReadyToPlayPacket> readyToPlayPacket = packet -> {
        orbits.launcher().gameThread().runLater(() -> {

        });
    };
    private final PacketHandler<RequestLevelPacket> requestLevelPacket = packet -> {
        orbits.launcher().gameThread().runLater(() -> {

        });
    };
    private final PacketHandler<StartPacket> startPacket = packet -> orbits.launcher().gameThread().runLater(() -> orbits.currentLobby().start(orbits));

    public PacketHandlers(OrbitsGame orbits) {
        this.orbits = orbits;
        this.client = orbits.launcher().networkClient();
    }

    public void registerHandlers() {
        client.getPacketRegistry().register(LevelChecksumPacket.class, LevelChecksumPacket::new);
        client.addHandler(LevelChecksumPacket.class, levelChecksumPacket);
        client.getPacketRegistry().register(LevelPacket.class, LevelPacket::new);
        client.addHandler(LevelPacket.class, levelPacket);
        client.getPacketRegistry().register(StartPacket.class, StartPacket::new);
        client.addHandler(StartPacket.class, startPacket);
        client.getPacketRegistry().register(ReadyToPlayPacket.class, ReadyToPlayPacket::new);
        client.addHandler(ReadyToPlayPacket.class, readyToPlayPacket);
        client.getPacketRegistry().register(RequestLevelPacket.class, RequestLevelPacket::new);
        client.addHandler(RequestLevelPacket.class, requestLevelPacket);
    }

    public void unregisterHandlers() throws PacketNotRegisteredException {
        client.getPacketRegistry().unregister(LevelChecksumPacket.class);
        client.removeHandler(LevelChecksumPacket.class, levelChecksumPacket);
        client.getPacketRegistry().unregister(LevelPacket.class);
        client.removeHandler(LevelPacket.class, levelPacket);
        client.getPacketRegistry().unregister(StartPacket.class);
        client.removeHandler(StartPacket.class, startPacket);
        client.getPacketRegistry().unregister(ReadyToPlayPacket.class);
        client.removeHandler(ReadyToPlayPacket.class, readyToPlayPacket);
        client.getPacketRegistry().unregister(RequestLevelPacket.class);
        client.removeHandler(RequestLevelPacket.class, requestLevelPacket);
    }

    private void checkComplete() {
        if (orbits.currentLobby().availableData().complete()) {
            // TODO send ReadToPlayPacket
        }
    }

}
