package orbits.gui;

import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.TextureGui;
import gamelauncher.engine.util.GameException;
import orbits.OrbitsGame;

public class TextureBackgroundGui extends ParentableAbstractGui {

    public TextureBackgroundGui(OrbitsGame orbits, String path) throws GameException {
        super(orbits.launcher());
        TextureGui gui = launcher().guiManager().createGui(TextureGui.class);
        gui.widthProperty().bind(widthProperty());
        gui.heightProperty().bind(heightProperty());
        gui.xProperty().bind(xProperty());
        gui.yProperty().bind(yProperty());

        gui.texture().uploadAsync(launcher().resourceLoader().resource(orbits.key().withKey(path).toPath(launcher().assets())).newResourceStream()).thenRun(() -> {
            try {
                gui.widthProperty().unbind();
                gui.heightProperty().unbind();
                gui.xProperty().unbind();
                gui.yProperty().unbind();
                float aspectRatio = gui.texture().width().floatValue() / gui.texture().height().floatValue();
                gui.widthProperty().bind(widthProperty().max(heightProperty().multiply(aspectRatio)));
                gui.heightProperty().bind(heightProperty().max(widthProperty().divide(aspectRatio)));
                gui.xProperty().bind(xProperty().add(widthProperty().subtract(gui.widthProperty()).divide(2)));
                gui.yProperty().bind(yProperty().add(heightProperty().subtract(gui.heightProperty()).divide(2)));

                redraw();
            } catch (GameException e) {
                throw new RuntimeException(e);
            }
        });
        addGUI(gui);
    }
}
