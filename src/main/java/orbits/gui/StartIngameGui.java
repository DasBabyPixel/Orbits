package orbits.gui;

import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.gui.guis.ColorGui;
import gamelauncher.engine.gui.guis.TextGui;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.KeyboardKeybindEvent;
import gamelauncher.engine.util.text.Component;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java8.util.concurrent.CompletableFuture;
import orbits.OrbitsGame;
import orbits.data.LocalPlayer;
import orbits.data.Vector3;
import orbits.data.level.Level;
import orbits.ingame.Game;
import orbits.network.client.*;
import orbits.network.server.*;
import orbits.server.OrbitsServer;
import orbits.server.network.NetworkServer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StartIngameGui extends ParentableAbstractGui {
    protected final OrbitsGame orbits;
    protected final Random r = new Random(0);
    protected final IntList players = new IntArrayList();
    protected final List<PlayerGui> playerGuis = new ArrayList<>();
    protected final Int2ObjectMap<Vector3> remotePlayers = new Int2ObjectOpenHashMap<>();
    protected final Game game;
    protected final ButtonGui back;
    protected final CompletableFuture<Void> handshakeFuture = new CompletableFuture<>();
    protected Connection connection;
    protected @Nullable OrbitsServer server;
    protected int idModifier = 0;

    public StartIngameGui(OrbitsGame orbits, Connection connection, @Nullable OrbitsServer server) throws GameException {
        super(orbits.launcher());
        this.server = server;
        this.orbits = orbits;
        this.connection = connection;
        game = new Game();
        game.owner(false);
        orbits.currentLobby(game);

        performHandshake(connection);

        connection.addHandler(PacketIdModifier.class, (con, packet) -> {
            launcher().gameThread().submit(() -> {
                idModifier = packet.modifier;
            });
        });
        connection.cleanupFuture().thenRun(() -> {
            if (initialized()) {
                launcher().gameThread().submit(() -> {
                    this.connection = null;
                    launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits));
                });
            }
        });
        connection.addHandler(PacketPlayerCreated.class, (con, packet) -> launcher().gameThread().submit(() -> addPlayer(packet.id, packet.display, packet.color)));
        connection.addHandler(PacketPlayerDeleted.class, (con, packet) -> launcher().gameThread().submit(() -> removePlayer(packet.id)));
        connection.addHandler(PacketIngame.class, (con, packet) -> {
            launcher().gameThread().submit(this::start);
        });

        back = launcher().guiManager().createGui(ButtonGui.class);
        back.xProperty().bind(xProperty().add(10));
        back.yProperty().bind(yProperty().add(heightProperty()).subtract(10).subtract(back.heightProperty()));
        back.heightProperty().bind(heightProperty().divide(14));
        back.widthProperty().bind(back.heightProperty().multiply(3));
        ((ButtonGui.Simple.TextForeground) back.foreground().value()).textGui().text().value(Component.text("Back"));
        back.onButtonPressed(event -> launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits)));
        addGUI(back);

        if (server != null) {
            ButtonGui start = launcher().guiManager().createGui(ButtonGui.class);
            start.xProperty().bind(xProperty().add(widthProperty()).subtract(10).subtract(start.widthProperty()));
            start.yProperty().bind(back.yProperty());
            start.heightProperty().bind(back.heightProperty());
            start.widthProperty().bind(back.widthProperty());
            ((ButtonGui.Simple.TextForeground) start.foreground().value()).textGui().text().value(Component.text("Start"));
            start.onButtonPressed(event -> connection.sendPacket(new PacketStart()));
            addGUI(start);
            if (server instanceof NetworkServer) {
                server.startFuture().thenRun(() -> {
                    String id = ((NetworkServer) server).serverId();
                    try {
                        TextGui idText = launcher().guiManager().createGui(TextGui.class);
                        idText.heightProperty().bind(back.heightProperty());
                        idText.xProperty().bind(xProperty().add(widthProperty().subtract(idText.widthProperty()).divide(2)));
                        idText.yProperty().bind(back.yProperty());
                        idText.color().set(1, 1, 1, 1);
                        idText.text().value(Component.text(id));
                        addGUI(idText);
                    } catch (GameException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    private void performHandshake(Connection connection) throws GameException {
        Connection.State state = connection.ensureState(Connection.State.CONNECTED).timeoutAfter(5, TimeUnit.SECONDS).await();
        if (state != Connection.State.CONNECTED) throw new GameException();

        connection.addHandler(PacketLevel.class, (con, packet) -> {
            orbits.launcher().gameThread().submit(() -> {
                Level level = packet.level;
                try {
                    orbits.levelStorage().saveLevel(level);
                } catch (GameException e) {
                    throw new RuntimeException(e);
                }
                orbits.currentLobby().availableData().level = level;
                checkComplete(con);
            });
        });
        connection.addHandler(PacketLevelChecksum.class, (con, packet) -> {
            final UUID levelId = packet.levelId;
            final long checksum = packet.checksum;
            orbits.launcher().gameThread().submit(() -> {
                Level level = orbits.levelStorage().findLevel(levelId, checksum);
                if (level == null) {
                    con.sendPacket(new PacketRequestLevel(packet.levelId));
                } else {
                    game.availableData().level = level;
                    checkComplete(con);
                }
            });
        });

        connection.sendPacket(new PacketHello());
    }

    @Override
    public void onClose() throws GameException {
        if (connection != null) {
            connection.cleanup();
        }
        if (server != null) {
            server.stop();
        }
    }

    private void checkComplete(Connection connection) {
        if (game.availableData().complete()) {
            connection.sendPacket(new PacketReadyToPlay());
            handshakeFuture.complete(null);
        }
    }

    protected void start() throws GameException {
        System.out.println(1);
        Game l = orbits.currentLobby();
        for (int i = 0; i < players.size(); i++) {
            int id = players.getInt(i);
            PlayerGui pg = playerGuis.get(i);
            LocalPlayer player = new LocalPlayer(id, pg.display.charAt(0));
            player.color().set(pg.color);
            l.players().add(player);
        }
        System.out.println(1);
        l.start(orbits);
        IngameGui ingameGui = new IngameGui(orbits, connection, server, idModifier);
        connection = null;
        server = null;
        System.out.println(1);
        launcher().guiManager().openGui(ingameGui);
        System.out.println(1);
    }

    @Override
    protected boolean doHandle(KeybindEvent entry) throws GameException {
        if (entry instanceof KeyboardKeybindEvent.CharacterKeybindEvent) {
            KeyboardKeybindEvent.CharacterKeybindEvent ke = ((KeyboardKeybindEvent.CharacterKeybindEvent) entry);
            handleCharacter(ke);
        }
        return super.doHandle(entry);
    }

    protected void handleCharacter(KeyboardKeybindEvent.CharacterKeybindEvent event) throws GameException {
        char c = event.character();
        if (players.contains(event.keybind().uniqueId())) {
            connection.sendPacket(new PacketDeletePlayer(event.keybind().uniqueId()));
        } else if (c != ' ' && c != '\n' && c != '\r' && c != '\b' && c != '\t' && c != '\f') {
            connection.sendPacket(new PacketNewPlayer(event.keybind().uniqueId(), c));
        }
    }

    protected void removePlayer(int id) {
        id = id - idModifier;
        int index = players.indexOf(id);
        if (index == -1) return;
        players.removeInt(index);
        PlayerGui pg = playerGuis.remove(index);
        PlayerGui prev = index == 0 ? null : playerGuis.get(index - 1);
        PlayerGui next = index == playerGuis.size() ? null : playerGuis.get(index);
        if (next != null) {
            next.yProperty().unbind();
            bind(next, prev);
        }
        pg.yProperty().unbind();
        removeGUI(pg);
    }

    protected PlayerGui addPlayer(int id, char display, Vector3 color) throws GameException {
        id = id - idModifier;
        if (display == 0) {
            remotePlayers.put(id, color);
            return null;
        }
        players.add(id);
        PlayerGui pg = new PlayerGui(orbits, Character.toString(display), color);
        pg.xProperty().bind(xProperty().add(10));
        pg.widthProperty().number(50);
        pg.heightProperty().number(50);
        PlayerGui prev = playerGuis.isEmpty() ? null : playerGuis.get(playerGuis.size() - 1);
        bind(pg, prev);
        playerGuis.add(pg);
        addGUI(pg);
        return pg;
    }

    protected void bind(PlayerGui pg, PlayerGui prev) {
        if (prev != null) {
            pg.yProperty().bind(prev.yProperty().subtract(pg.heightProperty()).subtract(10));
        } else {
            pg.yProperty().bind(back.yProperty().subtract(pg.heightProperty()).subtract(10));
        }
    }

    protected static class PlayerGui extends ParentableAbstractGui {
        protected final Vector3 color;
        protected final String display;

        public PlayerGui(OrbitsGame orbits, String display, Vector3 color) throws GameException {
            super(orbits.launcher());
            this.color = color;
            this.display = display;
            ColorGui colorGui = launcher().guiManager().createGui(ColorGui.class);
            colorGui.xProperty().bind(xProperty());
            colorGui.yProperty().bind(yProperty());
            colorGui.widthProperty().bind(widthProperty());
            colorGui.heightProperty().bind(heightProperty());
            colorGui.color().set(color.x(), color.y(), color.z(), 1);
            addGUI(colorGui);
            TextGui textGui = launcher().guiManager().createGui(TextGui.class);
            textGui.xProperty().bind(xProperty());
            textGui.yProperty().bind(yProperty());
            textGui.heightProperty().bind(heightProperty());
            textGui.text().value(Component.text(this.display));
            addGUI(textGui);
        }
    }
}
