package orbits.gui;

import de.dasbabypixel.api.property.NumberValue;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.concurrent.Threads;
import gamelauncher.engine.util.text.Component;
import orbits.OrbitsGame;
import orbits.server.InternalServer;

public class OrbitsMainScreenGui extends ParentableAbstractGui {

    public OrbitsMainScreenGui(OrbitsGame orbits) throws GameException {
        super(orbits.launcher());

        TextureBackgroundGui gui = new TextureBackgroundGui(orbits, "background1.png");
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
        multiplayer.onButtonPressed(event -> launcher().guiManager().openGui(new MultiplayerSelectGui(orbits)));
        addGUI(multiplayer);

        ButtonGui singleplayer = launcher().guiManager().createGui(ButtonGui.class);
        ((ButtonGui.Simple.TextForeground) singleplayer.foreground().value()).textGui().text().value(Component.text(launcher().languageManager().selectedLanguage().translate(orbits.key().withKey("singleplayer"))));
        singleplayer.widthProperty().bind(width);
        singleplayer.heightProperty().bind(height);
        singleplayer.xProperty().bind(xProperty().add(widthProperty().divide(1.2)).subtract(singleplayer.widthProperty()));
        singleplayer.yProperty().bind(multiplayer.yProperty().add(singleplayer.heightProperty().multiply(1.5)));
        singleplayer.onButtonPressed(event -> {
            LevelSelectGui levelSelectGui = new LevelSelectGui(orbits, false, l -> {
                if (l.startPositions().isEmpty()) return false;
                return true;
            });
            levelSelectGui.exit().onButtonPressed(e1 -> launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits)));
            levelSelectGui.levelSelector().value(level -> {
                InternalServer server = new InternalServer(orbits, level);
                server.start();
                Threads.await(server.startFuture());
                launcher().guiManager().openGui(new StartIngameGui.Simple(orbits, server.clientConnection(), server));
            });
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

    }
}
