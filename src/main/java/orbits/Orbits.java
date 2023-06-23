package orbits;

import gamelauncher.engine.gui.GuiDistribution;
import gamelauncher.engine.gui.guis.TextGui;
import gamelauncher.engine.plugin.Plugin;
import gamelauncher.engine.plugin.Plugin.GamePlugin;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.Key;
import gamelauncher.engine.util.logging.Logger;
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
        launcher().guiManager().registerGuiCreator(GuiDistribution.DEFAULT, TextGui.class, () -> {
            return new TextGui.Simple(launcher()) {
//                @Override
//                protected void doInit() throws GameException {
//                }
//
//                @Override
//                protected void preRender(float mouseX, float mouseY, float partialTick) throws GameException {
//                }
//
//                @Override
//                protected boolean doRender(float mouseX, float mouseY, float partialTick) throws GameException {
//                    return true;
//                }
//
//                @Override
//                protected void doCleanup() throws GameException {
//                }
            };
        });
        Logger.asyncLogStream().async(false);
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
