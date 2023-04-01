package orbits;

import gamelauncher.engine.event.EventHandler;
import gamelauncher.engine.event.events.gui.GuiOpenEvent;
import gamelauncher.engine.game.Game;
import gamelauncher.engine.gui.launcher.MainScreenGui;
import gamelauncher.engine.render.Framebuffer;
import gamelauncher.engine.util.GameException;

public class OrbitsGame extends Game {
	public OrbitsGame(Orbits orbits) {
		super(orbits, "orbits");
	}

	@Override
	protected void launch0(Framebuffer framebuffer) throws GameException {
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
}
