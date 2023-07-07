package orbits.gui;

import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.gui.guis.TextGui;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.packet.Packet;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.settings.SettingSection;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.KeyboardKeybindEvent;
import gamelauncher.engine.util.logging.Logger;
import gamelauncher.engine.util.text.Component;
import gamelauncher.netty.standalone.packet.c2s.PacketConnectToServer;
import java8.util.concurrent.CompletableFuture;
import orbits.OrbitsGame;
import orbits.network.AbstractServerWrapperConnection;
import orbits.network.ServerUtils;
import orbits.settings.OrbitsSettingSection;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ServerIpGui extends ParentableAbstractGui {
    private static final Logger logger = Logger.logger();
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
        ButtonGui back = launcher().guiManager().createGui(ButtonGui.class);
        back.xProperty().bind(xProperty().add(10));
        back.yProperty().bind(yProperty().add(heightProperty()).subtract(10).subtract(back.heightProperty()));
        back.heightProperty().bind(heightProperty().divide(14));
        back.widthProperty().bind(back.heightProperty().multiply(3));
        ((ButtonGui.Simple.TextForeground) back.foreground().value()).textGui().text().value(Component.text("Back"));
        back.onButtonPressed(event -> launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits)));
        addGUI(back);
    }

    private void connect() throws UnknownHostException, GameException, URISyntaxException {
        SettingSection section = launcher().settings().getSubSection(OrbitsSettingSection.ORBITS);
        Connection connection = launcher().networkClient().connect(new URI(section.<String>getSetting(OrbitsSettingSection.SERVER_URL).getValue()));
        Connection.State state = connection.ensureState(Connection.State.CONNECTED).timeoutAfter(5, TimeUnit.SECONDS).await();
        if (state == Connection.State.CONNECTED) {
            CompletableFuture<Integer> connectFuture = new CompletableFuture<>();
            PacketHandler<PacketConnectToServer.Response> responseHandler = (con, packet) -> connectFuture.complete(packet.code);
            connection.addHandler(PacketConnectToServer.Response.class, responseHandler);
            connection.sendPacket(new PacketConnectToServer(ip));
            try {
                int code = connectFuture.get(5, TimeUnit.SECONDS);
                if (code != 1) {
                    connection.cleanup();
                    logger.error("Failed to connect: " + code);
                    return;
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error(e);
                connection.cleanup();
                return;
            }

            launcher().guiManager().openGui(new StartIngameGui(orbits, new AbstractServerWrapperConnection(connection) {
                {
                    loadClient();
                }

                @Override
                public void sendPacket(Packet packet) {
                    ServerUtils.clientSendPacket(encoder, connection, packet);
                }

                @Override
                public CompletableFuture<Void> sendPacketAsync(Packet packet) {
                    return ServerUtils.clientSendPacketAsync(encoder, connection, packet);
                }
            }, null));
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
                } catch (UnknownHostException | URISyntaxException e) {
                    throw GameException.wrap(e);
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
