package orbits.gui;

import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.concurrent.Threads;
import gamelauncher.engine.util.text.Component;
import orbits.OrbitsGame;
import orbits.server.network.NetworkServer;

public class MultiplayerSelectGui extends ParentableAbstractGui {

    public MultiplayerSelectGui(OrbitsGame orbits) throws GameException {
        super(orbits.launcher());
        ButtonGui buttonGui = launcher().guiManager().createGui(ButtonGui.class);
        buttonGui.xProperty().bind(xProperty().add(widthProperty().subtract(buttonGui.widthProperty()).divide(2)));
        buttonGui.yProperty().bind(yProperty().add(heightProperty().subtract(buttonGui.heightProperty()).divide(2)).subtract(buttonGui.heightProperty()));
        buttonGui.widthProperty().bind(widthProperty().divide(2));
        buttonGui.heightProperty().bind(heightProperty().divide(4));
        buttonGui.onButtonPressed(event -> {
            LevelSelectGui levelSelectGui = new LevelSelectGui(orbits, false, l -> {
                if (l.startPositions().isEmpty()) return false;
                return true;
            });
            levelSelectGui.exit().onButtonPressed(e1 -> launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits)));
            levelSelectGui.levelSelector().value(level -> {
                NetworkServer server = new NetworkServer(orbits, level);
                server.start();
                Threads.await(server.startFuture());
                launcher().guiManager().openGui(new StartIngameGui(orbits, server.connection(), server));
            });
            launcher().guiManager().openGui(levelSelectGui);
        });
        ((ButtonGui.Simple.TextForeground) buttonGui.foreground().value()).textGui().text().value(Component.text("Create"));
        addGUI(buttonGui);

        buttonGui = launcher().guiManager().createGui(ButtonGui.class);
        buttonGui.xProperty().bind(xProperty().add(widthProperty().subtract(buttonGui.widthProperty()).divide(2)));
        buttonGui.yProperty().bind(yProperty().add(heightProperty().subtract(buttonGui.heightProperty()).divide(2)).add(buttonGui.heightProperty()));
        buttonGui.widthProperty().bind(widthProperty().divide(2));
        buttonGui.heightProperty().bind(heightProperty().divide(4));
        buttonGui.onButtonPressed(event -> launcher().guiManager().openGui(new ServerIpGui(orbits)));
        ((ButtonGui.Simple.TextForeground) buttonGui.foreground().value()).textGui().text().value(Component.text("Join"));
        addGUI(buttonGui);
    }
}
