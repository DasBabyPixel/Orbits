package orbits.gui;

import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.MouseButtonKeybindEvent;

public class CustomGui extends ButtonGui.Simple {
    public CustomGui(GameLauncher launcher) throws GameException {
        super(launcher);
        ((ColorBackground) this.background().value()).defaultColor().set(0, 0, 0, 0);
    }

    @Override
    protected boolean doHandle(KeybindEvent entry) throws GameException {
        return super.doHandle(entry);
    }
}
