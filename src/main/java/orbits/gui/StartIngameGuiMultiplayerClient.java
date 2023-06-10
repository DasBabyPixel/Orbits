package orbits.gui;

import gamelauncher.engine.network.Connection;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.concurrent.Threads;
import gamelauncher.engine.util.keybind.KeyboardKeybindEvent;
import orbits.OrbitsGame;
import orbits.data.level.Level;
import orbits.network.*;

public class StartIngameGuiMultiplayerClient extends StartIngameGui {
    private final Connection connection;
    private boolean ingame = false;

    public StartIngameGuiMultiplayerClient(OrbitsGame orbits, Connection connection) throws GameException {
        super(null, orbits);
        this.connection = connection;
        this.connection.addHandler(PacketPlayerCreated.class, (connection1, packet) -> launcher().gameThread().runLater(() -> created(packet)));
        this.connection.addHandler(PacketPlayerDeleted.class, (connection1, packet) -> launcher().gameThread().runLater(() -> deleted(packet)));
        this.connection.addHandler(PacketIngame.class, (connection1, packet) -> launcher().gameThread().runLater(this::ingame));
        this.connection.cleanupFuture().thenRun(() -> {
            try {
                launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits));
            } catch (GameException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected void addStart() {
    }

    private void ingame() {
        ingame = true;
    }

    @Override
    protected void handleCharacter(KeyboardKeybindEvent.CharacterKeybindEvent event) throws GameException {
        if (!ingame) {
            super.handleCharacter(event);
        } else {
            connection.sendPacket(new PacketPress(event.keybind().uniqueId()));
        }
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

    @Override
    public void onClose() throws GameException {
        if (!connection.cleanedUp()) Threads.await(connection.cleanup());
    }
}
