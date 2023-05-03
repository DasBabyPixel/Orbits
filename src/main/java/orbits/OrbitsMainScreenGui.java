package orbits;

import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.TextureGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.MouseButtonKeybindEvent;
import gamelauncher.engine.util.keybind.MouseButtonKeybindEvent.Type;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;

public class OrbitsMainScreenGui extends ParentableAbstractGui {

    public OrbitsMainScreenGui(GameLauncher launcher) throws GameException {
        super(launcher);
        TextureGui gui = launcher().guiManager().createGui(TextureGui.class);
        gui.widthProperty().bind(widthProperty());
        gui.heightProperty().bind(heightProperty());
        gui.xProperty().bind(xProperty());
        gui.yProperty().bind(yProperty());
        
        gui.texture().uploadAsync(launcher().resourceLoader()
                        .resource(launcher.assets().resolve("12orbits.png")).newResourceStream())
                .thenRun(() -> {
                    gui.widthProperty().unbind();
                    gui.heightProperty().unbind();
                    gui.xProperty().unbind();
                    gui.yProperty().unbind();
                    float aspectRatio = gui.texture().width().floatValue() / gui.texture().height()
                            .floatValue();
                    gui.widthProperty()
                            .bind(widthProperty().min(heightProperty().multiply(aspectRatio)));
                    gui.heightProperty()
                            .bind(heightProperty().min(widthProperty().divide(aspectRatio)));
                    gui.xProperty().bind(xProperty().add(
                            widthProperty().subtract(gui.widthProperty()).divide(2)));
                    gui.yProperty().bind(yProperty().add(
                            heightProperty().subtract(gui.heightProperty()).divide(2)));

                    redraw();
                });
        GUIs.add(gui);
    }

    @Override
    protected boolean doHandle(KeybindEvent event) throws GameException {
        if (event instanceof MouseButtonKeybindEvent) {
            MouseButtonKeybindEvent mbke = ((MouseButtonKeybindEvent) event);
            if (mbke.type() == Type.PRESS)
                launcher().keyboardVisible(!launcher().keyboardVisible());
        }
        return super.doHandle(event);
    }
}
