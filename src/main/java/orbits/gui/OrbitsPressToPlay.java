package orbits.gui;

import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.TextureGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.MouseButtonKeybindEvent;
import orbits.OrbitsGame;

public class OrbitsPressToPlay extends ParentableAbstractGui {
    private final OrbitsGame orbits;
    public OrbitsPressToPlay(OrbitsGame orbitsGame) throws GameException {
        super(orbitsGame.launcher());
        this.orbits = orbitsGame;
        TextureGui gui = launcher().guiManager().createGui(TextureGui.class);
        gui.widthProperty().bind(widthProperty());
        gui.heightProperty().bind(heightProperty());
        gui.xProperty().bind(xProperty());
        gui.yProperty().bind(yProperty());

        gui.texture().uploadAsync(launcher().resourceLoader().resource(orbitsGame.key().withKey("textures/pressToPlay.png").toPath(launcher().assets())).newResourceStream()).thenRun(() -> {
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

    @Override
    protected boolean doHandle(KeybindEvent entry) throws GameException {
        if (entry instanceof MouseButtonKeybindEvent) {
            launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits));
        }
        return super.doHandle(entry);
    }
}
