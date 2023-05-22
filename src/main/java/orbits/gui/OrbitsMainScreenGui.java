package orbits.gui;

import de.dasbabypixel.api.property.NumberValue;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.text.Component;
import orbits.OrbitsGame;

public class OrbitsMainScreenGui extends ParentableAbstractGui {

    public OrbitsMainScreenGui(OrbitsGame orbits) throws GameException {
        super(orbits.launcher());

        TextureBackgroundGui gui = new TextureBackgroundGui(orbits, "textures/background1.png");
        gui.widthProperty().bind(widthProperty());
        gui.heightProperty().bind(heightProperty());
        gui.xProperty().bind(xProperty());
        gui.yProperty().bind(yProperty());
        addGUI(gui);

        NumberValue height = heightProperty().divide(7);
        NumberValue width = height.multiply(5);
        ButtonGui multiplayer = launcher().guiManager().createGui(ButtonGui.class);
        ((ButtonGui.Simple.TextForeground) multiplayer.foreground().value()).textGui().text().value(Component.text(launcher().languageManager().selectedLanguage().translate(orbits.key().withKey("multiplayer"))));
        multiplayer.xProperty().bind(xProperty().add(widthProperty().divide(1.2)).subtract(multiplayer.widthProperty()));
        multiplayer.yProperty().bind(yProperty().add(heightProperty().divide(2)).subtract(multiplayer.heightProperty().divide(2)));
        multiplayer.widthProperty().bind(width);
        multiplayer.heightProperty().bind(height);
        addGUI(multiplayer);

        ButtonGui singleplayer = launcher().guiManager().createGui(ButtonGui.class);
        ((ButtonGui.Simple.TextForeground) singleplayer.foreground().value()).textGui().text().value(Component.text(launcher().languageManager().selectedLanguage().translate(orbits.key().withKey("singleplayer"))));
        singleplayer.widthProperty().bind(width);
        singleplayer.heightProperty().bind(height);
        singleplayer.xProperty().bind(xProperty().add(widthProperty().divide(1.2)).subtract(singleplayer.widthProperty()));
        singleplayer.yProperty().bind(multiplayer.yProperty().add(singleplayer.heightProperty().multiply(1.5)));
        singleplayer.onButtonPressed(event -> {
            LevelSelectGui levelSelectGui = new LevelSelectGui(orbits, false);
            levelSelectGui.exit().onButtonPressed(e1 -> launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits)));
            levelSelectGui.levelSelector().value(level -> launcher().guiManager().openGui(new StartSingleplayerGui(level, orbits)));
            launcher().guiManager().openGui(levelSelectGui);
        });
        addGUI(singleplayer);

        ButtonGui mapEditor = launcher().guiManager().createGui(ButtonGui.class);
        ((ButtonGui.Simple.TextForeground) mapEditor.foreground().value()).textGui().text().value(Component.text(launcher().languageManager().selectedLanguage().translate(orbits.key().withKey("map_editor"))));
        mapEditor.xProperty().bind(xProperty().add(widthProperty().divide(1.2)).subtract(mapEditor.widthProperty()));
        mapEditor.yProperty().bind(multiplayer.yProperty().subtract(mapEditor.heightProperty().multiply(1.5)));
        mapEditor.widthProperty().bind(width);
        mapEditor.heightProperty().bind(height);
        mapEditor.onButtonPressed(event -> {
            LevelSelectGui levelSelectGui = new LevelSelectGui(orbits, true);
            levelSelectGui.exit().onButtonPressed(e -> launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits)));
            levelSelectGui.levelSelector().value(level -> launcher().guiManager().openGui(new MapEditorGui(orbits, level)));
            launcher().guiManager().openGui(levelSelectGui);
        });
        addGUI(mapEditor);

//        TextureGui gui = launcher().guiManager().createGui(TextureGui.class);
//        gui.widthProperty().bind(widthProperty());
//        gui.heightProperty().bind(heightProperty());
//        gui.xProperty().bind(xProperty());
//        gui.yProperty().bind(yProperty());
//
//        gui.texture().uploadAsync(launcher().resourceLoader()
//                        .resource(launcher.assets().resolve("pressToPlay.png")).newResourceStream())
//                .thenRun(() -> {
//                    gui.widthProperty().unbind();
//                    gui.heightProperty().unbind();
//                    gui.xProperty().unbind();
//                    gui.yProperty().unbind();
//                    float aspectRatio = gui.texture().width().floatValue() / gui.texture().height()
//                            .floatValue();
//                    gui.widthProperty()
//                            .bind(widthProperty().min(heightProperty().multiply(aspectRatio)));
//                    gui.heightProperty()
//                            .bind(heightProperty().min(widthProperty().divide(aspectRatio)));
//                    gui.xProperty().bind(xProperty().add(
//                            widthProperty().subtract(gui.widthProperty()).divide(2)));
//                    gui.yProperty().bind(yProperty().add(
//                            heightProperty().subtract(gui.heightProperty()).divide(2)));
//
//                    redraw();
//                });
//        GUIs.add(gui);
    }

//    @Override
//    protected boolean doHandle(KeybindEvent event) throws GameException {
//        if (event instanceof MouseButtonKeybindEvent) {
//            MouseButtonKeybindEvent mbke = ((MouseButtonKeybindEvent) event);
//            if (mbke.type() == Type.PRESS)
//                launcher().keyboardVisible(!launcher().keyboardVisible());
//        }
//        return super.doHandle(event);
//    }
}
