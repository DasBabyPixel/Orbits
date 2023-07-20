package orbits;

import gamelauncher.engine.event.EventHandler;
import gamelauncher.engine.event.events.LauncherInitializedEvent;
import gamelauncher.engine.event.events.gui.GuiOpenEvent;
import gamelauncher.engine.gui.GuiDistribution;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.gui.guis.MainScreenGui;
import gamelauncher.engine.render.Framebuffer;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.concurrent.Threads;
import orbits.data.LevelStorage;
import orbits.gui.CustomGui;
import orbits.gui.OrbitsPressToPlay;
import orbits.gui.ServerIpGui;
import orbits.gui.TextureStorage;
import orbits.ingame.Game;
import orbits.network.PacketHandlers;

public class OrbitsGame extends gamelauncher.engine.game.Game {
    private final Orbits orbits;
    private final PacketHandlers packetHandlers = new PacketHandlers(this);
    private final TextureStorage textureStorage;
    private Game game;
    private volatile LevelStorage levelStorage;

    public OrbitsGame(Orbits orbits) throws GameException {
        super(orbits, "orbits");
        this.orbits = orbits;
        this.textureStorage = new TextureStorage(this);
        levelStorage = new LevelStorage(this);
        launcher().guiManager().registerGuiCreator(GuiDistribution.DEFAULT, ButtonGui.class, CustomGui.class);
        launcher().guiManager().registerGuiCreator(GuiDistribution.DEFAULT, ServerIpGui.class);
    }

    @Override
    protected void launch0(Framebuffer framebuffer) throws GameException {
        packetHandlers().registerHandlers();
    }

    @Override
    protected void close0() throws GameException {
        Threads.await(textureStorage.cleanup());
    }

    @EventHandler
    private void handle(GuiOpenEvent event) throws GameException {
        if (event.gui() instanceof MainScreenGui) event.gui(new OrbitsPressToPlay(this));
    }

    @EventHandler
    private void handle(LauncherInitializedEvent event) throws GameException {
        launch(event.launcher().frame().framebuffer());
    }

    public LevelStorage levelStorage() {
        return levelStorage;
    }

    public PacketHandlers packetHandlers() {
        return packetHandlers;
    }

    public Game currentLobby() {
        if (Thread.currentThread() != launcher().gameThread())
            throw new RuntimeException("May only access this from GameThread");
        return game;
    }

    public TextureStorage textureStorage() {
        return textureStorage;
    }

    public void currentLobby(Game game) {
        if (Thread.currentThread() != launcher().gameThread())
            throw new RuntimeException("May only access this from GameThread");
        this.game = game;
    }
}
