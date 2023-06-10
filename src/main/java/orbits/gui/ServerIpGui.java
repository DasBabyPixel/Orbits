package orbits.gui;

import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.TextGui;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.NetworkAddress;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.KeyboardKeybindEvent;
import gamelauncher.engine.util.text.Component;
import gamelauncher.gles.GLES;
import java8.util.concurrent.ForkJoinPool;
import orbits.Orbits;
import orbits.OrbitsGame;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ServerIpGui extends ParentableAbstractGui {
    private final OrbitsGame orbits;
    private String ip = "127.0.0.1";
    private TextGui textGui;

    public ServerIpGui(OrbitsGame orbits) throws GameException {
        super(orbits.launcher());
        this.orbits = orbits;
        textGui = launcher().guiManager().createGui(TextGui.class);
        textGui.text().value(Component.text(ip));
        textGui.heightProperty().bind(heightProperty().divide(5));
        textGui.xProperty().bind(xProperty().add(widthProperty().subtract(textGui.widthProperty()).divide(2)));
        textGui.yProperty().bind(yProperty().add(heightProperty().subtract(textGui.heightProperty()).divide(2)));
        addGUI(textGui);
    }

    private void connect() throws UnknownHostException, GameException {
        String host = ip;
        int port = 15684;
        if (host.contains(":")) {
            try {
                port = Integer.parseInt(host.split(":")[1]);
            } catch (NumberFormatException ignored) {
            }
            host = host.split(":")[0];
        }
        Connection connection = launcher().networkClient().connect(NetworkAddress.byName(host, port));
        Connection.State state = connection.ensureState(Connection.State.CONNECTED).timeoutAfter(5, TimeUnit.SECONDS).await();
        if (state == Connection.State.CONNECTED) {
            launcher().guiManager().openGui(new StartIngameGuiMultiplayerClient(orbits, connection));
        } else {
            connection.cleanup();
            launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits));
        }
    }

    @Override
    protected boolean doHandle(KeybindEvent entry) throws GameException {
        if (entry instanceof KeyboardKeybindEvent.CharacterKeybindEvent) {
            entry.consume();
            char c = ((KeyboardKeybindEvent.CharacterKeybindEvent) entry).character();
            if (c == '\b') {
                ip = ip.substring(0, Math.max(0, ip.length() - 1));
            } else if (c == '\n') {
                try {
                    connect();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                return true;
            } else {
                ip = ip += c;
            }
            textGui.text().value(Component.text(ip));
        }
        return super.doHandle(entry);
    }
}
