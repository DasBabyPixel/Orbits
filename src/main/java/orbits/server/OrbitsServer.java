package orbits.server;

import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.GameThread;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.Key;
import gamelauncher.engine.util.logging.Logger;
import gamelauncher.netty.standalone.packet.s2c.PacketKicked;
import java8.util.concurrent.CompletableFuture;
import orbits.OrbitsGame;
import orbits.data.level.Level;
import orbits.lobby.Lobby;
import orbits.network.client.PacketDeletePlayer;
import orbits.network.client.PacketHello;
import orbits.network.client.PacketNewPlayer;
import orbits.network.client.PacketRequestLevel;
import orbits.network.server.*;
import org.joml.Vector4f;

import java.util.*;

public abstract class OrbitsServer {

    private static final Key KEY_IDS = new Key("orbits", "ids");
    private static final Key KEY_ID = new Key("orbits", "id");
    protected static final Logger logger = Logger.logger();
    protected final Random random = new Random(0);
    protected final GameLauncher launcher;
    protected final GameThread thread;
    protected final OrbitsGame orbits;
    protected final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
    protected final CompletableFuture<Void> startFuture = new CompletableFuture<>();
    protected final Lobby lobby;
    protected int state = 0; // 0 = Lobby, 1 = Ingame
    protected final Collection<Integer> players = new ArrayList<>();
    protected int nextBaseId = 1;
    protected Connection connection;

    public OrbitsServer(OrbitsGame orbits, Level level) {
        this.orbits = orbits;
        this.launcher = orbits.launcher();
        this.thread = new GameThread(launcher) {
            @Override
            protected void launcherTick() {
            }
        };
        this.lobby = new Lobby();
        this.lobby.availableData().level = level;
        thread.setName("OrbitsServerThread");
    }

    public void start() {
        thread.submit(this::start0);
        thread.start();
    }

    protected void start0() throws GameException {
        connection = createConnection();
        connection.addHandler(PacketHello.class, (con, packet) -> {
            con.sendPacket(new PacketWelcome());
            con.sendPacket(new PacketLevelChecksum(lobby.availableData().level.uuid(), lobby.availableData().level.checksum()));
        });
        connection.addHandler(PacketRequestLevel.class, (con, packet) -> {
            try {
                Level level = orbits.levelStorage().findLevel(packet.levelId, -1);
                if (level != null) con.sendPacket(new PacketLevel(level));
                else con.sendPacket(new PacketKicked("Kicked: Invalid levelId"));
            } catch (GameException e) {
                throw new RuntimeException(e);
            }
        });
        connection.addHandler(PacketNewPlayer.class, (con, packet) -> {
            thread.submit(() -> {
                int baseId = con.storedValue(KEY_ID, () -> {
                    int id = nextBaseId;
                    nextBaseId = nextBaseId + Character.MAX_VALUE;
                    return id;
                });
                Set<Integer> ids = con.storedValue(KEY_IDS, HashSet::new);
                if (ids.contains(packet.id)) return;
                ids.add(packet.id);
                players.add(baseId + packet.id);

                con.sendPacket(new PacketPlayerCreated(packet.id, packet.ch, newColor()));
            });
        });
        connection.addHandler(PacketDeletePlayer.class, (con, packet) -> {
            thread.submit(() -> {
                Set<Integer> ids = con.storedValue(KEY_IDS, HashSet::new);
                if (!ids.contains(packet.id)) return;
                ids.remove(packet.id);
                players.remove(con.<Integer>storedValue(KEY_ID) + packet.id);

                con.sendPacket(new PacketPlayerDeleted(packet.id));
            });
        });
        startFuture.complete(null);
    }

    public Connection connection() {
        return connection;
    }

    protected Vector4f newColor() {
        return new Vector4f(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0F);
    }

    public GameThread thread() {
        return thread;
    }

    protected abstract Connection createConnection() throws GameException;

    public CompletableFuture<Void> startFuture() {
        return startFuture;
    }

    public CompletableFuture<Void> shutdownFuture() {
        return shutdownFuture;
    }

    protected void stop0() throws GameException {
        if (!startFuture.isDone()) startFuture.completeExceptionally(new GameException("Server stopped"));
        shutdownFuture.complete(null);
    }

    public void stop() {
        thread.submit(this::stop0);
    }
}
