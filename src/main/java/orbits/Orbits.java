package orbits;

import gamelauncher.engine.gui.GuiConstructorTemplate;
import gamelauncher.engine.gui.GuiConstructorTemplates;
import gamelauncher.engine.plugin.Plugin;
import gamelauncher.engine.plugin.Plugin.GamePlugin;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.Key;
import orbits.settings.OrbitsSettingSectionInsertion;

import java.util.Locale;

@GamePlugin
public class Orbits extends Plugin {
    private OrbitsGame game;

    public Orbits() {
        super("orbits");
    }

    @Override
    public void onEnable() throws GameException {
        GuiConstructorTemplates.addDefault(new GuiConstructorTemplate(OrbitsGame.class) {
            @Override
            public Object[] arguments() {
                return new Object[]{game};
            }
        });
        launcher().frame().icon().value(launcher().imageDecoder().decodeIcon(launcher().resourceLoader().resource(launcher().assets().resolve("orbits").resolve("textures").resolve("orbits.ico")).newResourceStream()));
        new OrbitsSettingSectionInsertion(launcher().eventManager()).register(launcher());
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
