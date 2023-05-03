package orbits;

import gamelauncher.engine.event.EventHandler;
import gamelauncher.engine.event.events.gui.GuiOpenEvent;
import gamelauncher.engine.game.Game;
import gamelauncher.engine.gui.guis.MainScreenGui;
import gamelauncher.engine.render.Framebuffer;
import gamelauncher.engine.util.GameException;
import orbits.data.LevelStorage;
import orbits.network.PacketHandlers;

public class OrbitsGame extends Game {
    private final Orbits orbits;
    private final PacketHandlers packetHandlers = new PacketHandlers(this);
    private LevelStorage levelStorage;

    public OrbitsGame(Orbits orbits) {
        super(orbits, "orbits");
        this.orbits = orbits;
    }

    @Override
    protected void launch0(Framebuffer framebuffer) throws GameException {
        levelStorage = new LevelStorage(orbits);
        launcher().guiManager().openGui(framebuffer, null);
    }

    @Override
    protected void close0() {
    }

    @EventHandler
    private void handle(GuiOpenEvent event) throws GameException {
        if (event.gui() instanceof MainScreenGui) {
            event.gui(new OrbitsMainScreenGui(launcher()));
        }
    }

    public LevelStorage levelStorage() {
        return levelStorage;
    }

    public PacketHandlers packetHandlers() {
        return packetHandlers;
    }
}
