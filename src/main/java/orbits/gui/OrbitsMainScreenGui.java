package orbits.gui;

import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.text.Component;
import orbits.OrbitsGame;

public class OrbitsMainScreenGui extends ParentableAbstractGui {

    public OrbitsMainScreenGui(OrbitsGame orbits) throws GameException {
        super(orbits.launcher());
        ButtonGui mapEditor = launcher().guiManager().createGui(ButtonGui.class);
        mapEditor.text().value(Component.text(launcher().languageManager().selectedLanguage().translate(orbits.key().withKey("map_editor"))));
        mapEditor.xProperty().bind(xProperty().add(widthProperty().divide(1.2)).subtract(mapEditor.widthProperty()));
        mapEditor.yProperty().bind(yProperty().add(heightProperty().divide(2)).subtract(mapEditor.heightProperty().divide(2)));
        mapEditor.width(250);
        mapEditor.onButtonPressed(event -> launcher().guiManager().openGui(framebuffer, new MapEditorGui(orbits)));
        GUIs.add(mapEditor);
        ButtonGui settings = launcher().guiManager().createGui(ButtonGui.class);
        settings.text().value(Component.text(launcher().languageManager().selectedLanguage().translate(orbits.key().withKey("settings"))));
        settings.xProperty().bind(xProperty().add(widthProperty().divide(1.2)).subtract(settings.widthProperty()));
        settings.yProperty().bind(mapEditor.yProperty().subtract(settings.heightProperty().multiply(1.5)));
        settings.width(250);
        GUIs.add(settings);
        ButtonGui play = launcher().guiManager().createGui(ButtonGui.class);
        play.text().value(Component.text(launcher().languageManager().selectedLanguage().translate(orbits.key().withKey("play"))));
        play.xProperty().bind(xProperty().add(widthProperty().divide(1.2)).subtract(play.widthProperty()));
        play.yProperty().bind(mapEditor.yProperty().add(play.heightProperty().multiply(1.5)));
        play.width(250);
        GUIs.add(play);
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
