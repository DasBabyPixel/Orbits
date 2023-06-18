package orbits.gui;

import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.TextGui;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.NetworkAddress;
import gamelauncher.engine.network.packet.Packet;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.concurrent.Threads;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.KeyboardKeybindEvent;
import gamelauncher.engine.util.text.Component;
import gamelauncher.netty.standalone.PacketConnectToServer;
import java8.util.concurrent.CompletableFuture;
import orbits.OrbitsGame;
import orbits.network.AbstractServerWrapperConnection;
import orbits.network.ServerUtils;

import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ServerIpGui extends ParentableAbstractGui {
    private final OrbitsGame orbits;
    private String ip = "?????";
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
        Connection connection = launcher().networkClient().connect(NetworkAddress.byName("localhost", 19452));
        Connection.State state = connection.ensureState(Connection.State.CONNECTED).timeoutAfter(5, TimeUnit.SECONDS).await();
        if (state == Connection.State.CONNECTED) {
            Threads.await(connection.sendPacketAsync(new PacketConnectToServer(ip)));
            Threads.sleep(500);
            if (connection.cleanedUp()) return;

            launcher().guiManager().openGui(new StartIngameGuiMultiplayerClient(orbits, new AbstractServerWrapperConnection(connection) {
                @Override
                public void sendPacket(Packet packet) {
                    ServerUtils.clientSendPacket(encoder, connection, packet);
                }

                @Override
                public CompletableFuture<Void> sendPacketAsync(Packet packet) {
                    return ServerUtils.clientSendPacketAsync(encoder, connection, packet);
                }
            }));
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
                if (ip.length() != 5) return true;
                try {
                    connect();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                return true;
            } else {
                ip = ip + c;
                ip = ip.toUpperCase(Locale.ENGLISH);
                if (ip.length() > 5) ip = ip.substring(ip.length() - 5);
            }
            textGui.text().value(Component.text(ip));
        }
        return super.doHandle(entry);
    }

}
