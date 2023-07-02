package orbits.gui;

import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.util.GameException;
import orbits.OrbitsGame;
import orbits.data.level.Level;
import orbits.network.*;

public class StartIngameGuiMultiplayerClient extends StartIngameGui {
    private final Connection connection;
    private final PacketHandler<PacketPlayerCreated> playerCreated = (connection1, packet) -> launcher().gameThread().runLater(() -> created(packet));
    private final PacketHandler<PacketPlayerDeleted> playerDeleted = (connection1, packet) -> launcher().gameThread().runLater(() -> deleted(packet));
    private final PacketHandler<PacketIngame> ingame = (connection1, packet) -> launcher().gameThread().runLater(() -> ingame(packet.level));

    public StartIngameGuiMultiplayerClient(OrbitsGame orbits, Connection connection) throws GameException {
        super(null, orbits);
        this.connection = connection;
        this.connection.addHandler(PacketPlayerCreated.class, playerCreated);
        this.connection.addHandler(PacketPlayerDeleted.class, playerDeleted);
        this.connection.addHandler(PacketIngame.class, ingame);
        this.connection.cleanupFuture().thenRun(() -> {
            if (!initialized()) return;
            try {
                launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits));
            } catch (GameException e) {
                throw new RuntimeException(e);
            }
        });
        lobby.serverConnection(connection);
    }

    @Override
    public void onClose() {
        connection.removeHandler(PacketPlayerCreated.class, playerCreated);
        connection.removeHandler(PacketPlayerDeleted.class, playerDeleted);
        connection.removeHandler(PacketIngame.class, ingame);
    }

    @Override
    protected void addStart() {
    }

    private void ingame(Level level) throws GameException {
        lobby.availableData().level = level;
        forceStart();
    }

    @Override
    protected IngameGui startCreateGui() throws GameException {
        return new IngameGuiClient(orbits);
    }

    private void created(PacketPlayerCreated packet) throws GameException {
        newPlayer(packet.id, packet.display, packet.color);
    }

    private void deleted(PacketPlayerDeleted packet) {
        super.removePlayer(packet.id);
    }

    @Override
    protected void removePlayer(int id) {
        this.connection.sendPacket(new DeletePlayerPacket(id));
    }

    @Override
    protected PlayerGui newPlayer(int id, char display) {
        this.connection.sendPacket(new NewPlayerPacket(id, display));
        return null;
    }
}
