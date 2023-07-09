package orbits.gui;

import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.concurrent.Threads;
import gamelauncher.engine.util.text.Component;
import orbits.OrbitsGame;
import orbits.server.network.NetworkServer;
import orbits.server.network.NetworkServerUtil;

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
                Connection con = NetworkServerUtil.connect(launcher(), server.serverId());
                if (con == null) {
                    server.stop();
                    return;
                }
                launcher().guiManager().openGui(new StartIngameGui.Simple(orbits, con, server));
            });
            launcher().guiManager().openGui(levelSelectGui);
        });
        ((ButtonGui.Simple.TextForeground) buttonGui.foreground().value()).textGui().text().value(Component.text("Create"));
        addGUI(buttonGui);

        ButtonGui back = launcher().guiManager().createGui(ButtonGui.class);
        back.xProperty().bind(xProperty().add(10));
        back.yProperty().bind(yProperty().add(heightProperty()).subtract(10).subtract(back.heightProperty()));
        back.heightProperty().bind(heightProperty().divide(14));
        back.widthProperty().bind(back.heightProperty().multiply(3));
        ((ButtonGui.Simple.TextForeground) back.foreground().value()).textGui().text().value(Component.text("Back"));
        back.onButtonPressed(event -> launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits)));
        addGUI(back);

        buttonGui = launcher().guiManager().createGui(ButtonGui.class);
        buttonGui.xProperty().bind(xProperty().add(widthProperty().subtract(buttonGui.widthProperty()).divide(2)));
        buttonGui.yProperty().bind(yProperty().add(heightProperty().subtract(buttonGui.heightProperty()).divide(2)).add(buttonGui.heightProperty()));
        buttonGui.widthProperty().bind(widthProperty().divide(2));
        buttonGui.heightProperty().bind(heightProperty().divide(4));
        buttonGui.onButtonPressed(event -> launcher().guiManager().openGuiByClass(ServerIpGui.class));
        ((ButtonGui.Simple.TextForeground) buttonGui.foreground().value()).textGui().text().value(Component.text("Join"));
        addGUI(buttonGui);
    }
}
