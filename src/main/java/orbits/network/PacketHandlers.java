package orbits.network;

import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.network.NetworkClient;
import gamelauncher.engine.network.packet.PacketHandler;
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
        orbits.launcher().threads().cached.submit(() -> {
            Level level = orbits.levelStorage().findLevel(levelId, checksum);
            if (level == null) {
                // TODO: send packet to request level
            } else {

            }
        });
    };

    public PacketHandlers(OrbitsGame orbits) {
        this.orbits = orbits;
        this.client = orbits.launcher().networkClient();
    }

    public void registerHandlers() {
        client.addHandler(LevelChecksumPacket.class, levelChecksumPacket);
    }

    public void unregisterHandlers() {
        client.removeHandler(LevelChecksumPacket.class, levelChecksumPacket);
    }

}
