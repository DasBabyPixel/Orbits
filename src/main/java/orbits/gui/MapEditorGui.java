package orbits.gui;

import de.dasbabypixel.api.property.NumberValue;
import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.util.GameException;
import orbits.OrbitsGame;

public class MapEditorGui extends ParentableAbstractGui {
    private final OrbitsGame game;

    public MapEditorGui(OrbitsGame game) throws GameException {
        super(game.launcher());
        this.game = game;
        float inset = 0.05F;
        float spacingF = 0.02F;
        int verticalCount = 7;
        ButtonGui saveAndExit = createButton();
        saveAndExit.widthProperty().bind(saveAndExit.heightProperty());
        NumberValue guiHeight = heightProperty().multiply(1 - inset * 2);
        NumberValue spacing = guiHeight.multiply(spacingF);
        NumberValue rowHeight = heightProperty().subtract(spacing.multiply(verticalCount - 1));

        saveAndExit.heightProperty().bind(guiHeight.divide(7));
    }

    private ButtonGui createButton() throws GameException {
        return new ButtonGui(game.launcher());
    }
}
