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
        NumberValue height = heightProperty().divide(7);
        NumberValue width = height.multiply(5);
        ButtonGui mapEditor = launcher().guiManager().createGui(ButtonGui.class);
        ((ButtonGui.Simple.TextForeground) mapEditor.foreground().value()).textGui().text().value(Component.text(launcher().languageManager().selectedLanguage().translate(orbits.key().withKey("map_editor"))));
        mapEditor.xProperty().bind(xProperty().add(widthProperty().divide(1.2)).subtract(mapEditor.widthProperty()));
        mapEditor.yProperty().bind(yProperty().add(heightProperty().divide(2)).subtract(mapEditor.heightProperty().divide(2)));
        mapEditor.widthProperty().bind(width);
        mapEditor.heightProperty().bind(height);
        mapEditor.onButtonPressed(event -> {
            LevelSelectGui levelSelectGui = new LevelSelectGui(orbits);
            levelSelectGui.exit().onButtonPressed(e -> launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits)));
            levelSelectGui.levelSelector().value(level -> launcher().guiManager().openGui(new MapEditorGui(orbits, level)));
            launcher().guiManager().openGui(levelSelectGui);
        });
        addGUI(mapEditor);
        ButtonGui settings = launcher().guiManager().createGui(ButtonGui.class);

        ((ButtonGui.Simple.TextForeground) settings.foreground().value()).textGui().text().value(Component.text(launcher().languageManager().selectedLanguage().translate(orbits.key().withKey("settings"))));
        settings.xProperty().bind(xProperty().add(widthProperty().divide(1.2)).subtract(settings.widthProperty()));
        settings.yProperty().bind(mapEditor.yProperty().subtract(settings.heightProperty().multiply(1.5)));
        settings.widthProperty().bind(width);
        settings.heightProperty().bind(height);
        addGUI(settings);
        ButtonGui play = launcher().guiManager().createGui(ButtonGui.class);
        ((ButtonGui.Simple.TextForeground) play.foreground().value()).textGui().text().value(Component.text(launcher().languageManager().selectedLanguage().translate(orbits.key().withKey("play"))));
        play.xProperty().bind(xProperty().add(widthProperty().divide(1.2)).subtract(play.widthProperty()));
        play.yProperty().bind(mapEditor.yProperty().add(play.heightProperty().multiply(1.5)));
        play.widthProperty().bind(width);
        play.heightProperty().bind(height);
        addGUI(play);

//        TextureGui gui = launcher().guiManager().createGui(TextureGui.class);
//        gui.widthProperty().bind(widthProperty());
//        gui.heightProperty().bind(heightProperty());
//        gui.xProperty().bind(xProperty());
//        gui.yProperty().bind(yProperty());
//
//        gui.texture().uploadAsync(launcher().resourceLoader()
//                        .resource(launcher.assets().resolve("12orbits.png")).newResourceStream())
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
