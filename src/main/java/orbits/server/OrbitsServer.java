package orbits.server;

import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.GameThread;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.Key;
import gamelauncher.engine.util.logging.Logger;
import gamelauncher.netty.standalone.packet.s2c.PacketKicked;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java8.util.concurrent.CompletableFuture;
import orbits.OrbitsGame;
import orbits.data.*;
import orbits.data.level.Level;
import orbits.ingame.Game;
import orbits.ingame.GameBroadcast;
import orbits.network.client.*;
import orbits.network.server.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class OrbitsServer {

    protected static final Logger logger = Logger.logger();
    protected static final int BASE_ID_INTERVAL = 300000;
    private static final Key KEY_IDS = new Key("orbits", "ids");
    private static final Key KEY_ID = new Key("orbits", "id");
    private static final Key KEY_READY = new Key("orbits", "ready");
    protected final Random random = new Random(0);
    protected final GameLauncher launcher;
    protected final GameThread thread;
    protected final OrbitsGame orbits;
    protected final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
    protected final CompletableFuture<Void> startFuture = new CompletableFuture<>();
    protected final Game game;
    protected final Int2ObjectMap<Vector3> playerColors = new Int2ObjectOpenHashMap<>();
    protected final Int2ObjectMap<Player> players = new Int2ObjectOpenHashMap<>();
    protected final Collection<Connection> clientConnections = new ArrayList<>();
    protected int state = 0; // 0 = Lobby, 1 = Ingame
    protected int nextBaseId = BASE_ID_INTERVAL;
    protected int stopTimer = -1;
    protected Connection connection;
    protected CountDownLatch joinedLatch;
    private boolean paused = false;

    public OrbitsServer(OrbitsGame orbits, Level level) {
        this.orbits = orbits;
        this.launcher = orbits.launcher();
        this.thread = new GameThread(launcher) {
            @Override
            protected void launcherTick() {
                if (paused) return;
                try {
                    if (state == 1) {
                        if (stopTimer == -1) {
                            if (game.players().size() < 2) {
                                stopTimer = (int) (GameLauncher.MAX_TPS * 5);
                            }
                        } else {
                            if (stopTimer > 0) stopTimer--;
                            if (stopTimer == 0) OrbitsServer.this.stop();
                        }
                        if (stopTimer != 0) game.physicsEngine().tick();
                    }
                } catch (Throwable e) {
                    logger.error(e);
                }
            }
        };
        this.game = new Game();
        this.game.availableData().level = level;
        this.game.broadcast = new GameBroadcast() {
            @Override
            public void update(Entity entity) {
                connection.sendPacket(new PacketEntityData((Ball) entity));
            }

            @Override
            public void addTrail(int entityOwner, int entityTrail) {
                connection.sendPacket(new PacketAddTrail(entityOwner, entityTrail));
            }

            @Override
            public void removed(int entityId) {
                connection.sendPacket(new PacketEntityRemove(entityId));
            }

            @Override
            public void removeTrail(int entityOwner, int entityTrail) {
                connection.sendPacket(new PacketRemoveTrail(entityOwner, entityTrail));
            }
        };
        thread.setName("OrbitsServerThread");
    }

    public void start() {
        thread.submit(this::start0);
        thread.start();
    }

    protected void start0() throws GameException {
        logger.info("Starting OrbitsServer");
        connection = createConnection();
        connection.addHandler(PacketHello.class, (con, packet) -> {
            con.sendPacket(new PacketWelcome());
            con.sendPacket(new PacketLevelChecksum(game.availableData().level.uuid(), game.availableData().level.checksum()));
            clientConnections.add(con);
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
                    nextBaseId = nextBaseId + BASE_ID_INTERVAL;
                    return id;
                });
                con.sendPacket(new PacketIdModifier(baseId));
                Set<Integer> ids = con.storedValue(KEY_IDS, HashSet::new);
                if (ids.contains(packet.id)) return;
                ids.add(packet.id);
                Vector3 color = newColor();
                int id = baseId + packet.id;
                playerColors.put(baseId + packet.id, color);

                con.sendPacket(new PacketPlayerCreated(id, packet.ch, color));
                for (Connection clientConnection : clientConnections()) {
                    if (clientConnection == con) continue;
                    clientConnection.sendPacket(new PacketPlayerCreated(id, (char) 0, color));
                }
            });
        });
        connection.addHandler(PacketDeletePlayer.class, (con, packet) -> {
            thread.submit(() -> {
                Set<Integer> ids = con.storedValue(KEY_IDS, HashSet::new);
                if (!ids.contains(packet.id)) return;
                ids.remove(packet.id);
                playerColors.remove(con.<Integer>storedValue(KEY_ID) + packet.id);

                con.sendPacket(new PacketPlayerDeleted(con.<Integer>storedValue(KEY_ID) + packet.id));
                for (Connection clientConnection : clientConnections()) {
                    if (clientConnection == con) continue;
                    clientConnection.sendPacket(new PacketPlayerDeleted(con.<Integer>storedValue(KEY_ID) + packet.id));
                }
            });
        });
        connection.addHandler(PacketReadyToPlay.class, (con, packet) -> {
            con.storeValue(KEY_READY, true);
        });
        connection.addHandler(PacketStart.class, (con, packet) -> {
            thread.submit(() -> {
                if (playerColors.size() < 2) return;
                for (Connection ccon : clientConnections)
                    if (!ccon.storedValue(KEY_READY, () -> false)) return;
                joinedLatch = new CountDownLatch(clientConnections.size());
                for (Connection ccon : clientConnections)
                    ccon.sendPacket(new PacketIngame());
                for (int id : playerColors.keySet()) {
                    Player p = new LocalPlayer(id, (char) 0);
                    p.color().set(playerColors.get(id));
                    game.players().add(p);
                    players.put(id, p);
                }
                try {
                    //noinspection ResultOfMethodCallIgnored
                    joinedLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                state = 1;
                game.start(orbits);
            });
        });
        connection.addHandler(PacketPause.class, (con, packet) -> {
            thread.submit(() -> {
                paused = !paused;
                connection.sendPacket(new PacketPaused(paused));
            });
        });
        connection.addHandler(PacketPress.class, (con, packet) -> {
            thread.submit(() -> {
                int entityId = con.<Integer>storedValue(KEY_ID) + packet.id;
                Player entity = players.get(entityId);
                if (entity != null) game.tap(entity);
            });
        });
        connection.addHandler(PacketJoined.class, (con, packet) -> {
            joinedLatch.countDown();
        });
        startFuture.complete(null);
    }

    public Collection<Connection> clientConnections() {
        return clientConnections;
    }

    /**
     * @return the connection to the host network
     */
    public Connection connection() {
        return connection;
    }

    protected Vector3 newColor() {
        return new Vector3(random.nextFloat(), random.nextFloat(), random.nextFloat());
    }

    public GameThread thread() {
        return thread;
    }

    /**
     * @return a new connection from the server to the host network (most likely <a href="https://ssh.darkcube.eu/orbits">https://ssh.darkcube.eu/orbits</a>)
     */
    protected abstract Connection createConnection() throws GameException;

    public CompletableFuture<Void> startFuture() {
        return startFuture;
    }

    public CompletableFuture<Void> shutdownFuture() {
        return shutdownFuture;
    }

    protected void stop0() throws GameException {
        logger.info("Stopping OrbitsServer");
        if (!startFuture.isDone()) startFuture.completeExceptionally(new GameException("Server stopped"));
        shutdownFuture.complete(null);
        thread.cleanup();
    }

    public void stop() {
        thread.submit(this::stop0);
    }
}
