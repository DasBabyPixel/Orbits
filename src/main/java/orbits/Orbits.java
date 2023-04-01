package orbits;

import gamelauncher.engine.plugin.Plugin;
import gamelauncher.engine.plugin.Plugin.GamePlugin;
import gamelauncher.engine.util.GameException;

@GamePlugin
public class Orbits extends Plugin {
	private OrbitsGame game;

	public Orbits() {
		super("orbits");
	}

	@Override
	public void onEnable() throws GameException {
		game = new OrbitsGame(this);
		launcher().gameRegistry().register(game);
		launcher().eventManager().registerListener(game);
	}

	@Override
	public void onDisable() {
		launcher().gameRegistry().unregister(game.key());
		launcher().eventManager().unregisterListener(game);
	}
}
