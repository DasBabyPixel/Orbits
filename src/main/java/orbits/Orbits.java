package orbits;

import gamelauncher.engine.plugin.Plugin;
import gamelauncher.engine.plugin.Plugin.GamePlugin;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.Key;
import gamelauncher.engine.util.logging.Logger;

import java.util.Locale;

@GamePlugin
public class Orbits extends Plugin {
    private OrbitsGame game;

    public Orbits() {
        super("orbits");
    }

    @Override
    public void onEnable() throws GameException {
        Logger.asyncLogStream().async(false);
        game = new OrbitsGame(this);
        launcher().languageManager().language(Locale.ENGLISH).load(new Key(this, "languages/en.json"));
        launcher().gameRegistry().register(game);
        launcher().eventManager().registerListener(game);
    }

    @Override
    public void onDisable() {
        launcher().gameRegistry().unregister(game.key());
        launcher().eventManager().unregisterListener(game);
    }

    public OrbitsGame game() {
        return game;
    }
}
