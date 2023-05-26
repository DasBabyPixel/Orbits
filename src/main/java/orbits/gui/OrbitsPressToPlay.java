package orbits.gui;

import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.MouseButtonKeybindEvent;
import orbits.OrbitsGame;

public class OrbitsPressToPlay extends ParentableAbstractGui {
    private final OrbitsGame orbits;
    public OrbitsPressToPlay(OrbitsGame orbitsGame) throws GameException {
        super(orbitsGame.launcher());
        this.orbits = orbitsGame;
        TextureBackgroundGui gui = new TextureBackgroundGui(orbits, "pressToPlay.png");
        gui.widthProperty().bind(widthProperty());
        gui.heightProperty().bind(heightProperty());
        gui.xProperty().bind(xProperty());
        gui.yProperty().bind(yProperty());
        addGUI(gui);
    }

    @Override
    protected boolean doHandle(KeybindEvent entry) throws GameException {
        if (entry instanceof MouseButtonKeybindEvent) {
            launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits));
        }
        return super.doHandle(entry);
    }
}
