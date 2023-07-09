package orbits.gui;

import de.dasbabypixel.api.property.NumberValue;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ColorGui;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.render.ContextProvider;
import gamelauncher.engine.render.DrawContext;
import gamelauncher.engine.render.GameItem;
import gamelauncher.engine.render.model.GlyphStaticModel;
import gamelauncher.engine.render.model.Model;
import gamelauncher.engine.render.texture.Texture;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.KeyboardKeybindEvent;
import gamelauncher.engine.util.text.Component;
import gamelauncher.gles.model.GLESCombinedModelsModel;
import gamelauncher.gles.model.Texture2DModel;
import gamelauncher.gles.texture.GLESTexture;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import orbits.OrbitsGame;
import orbits.data.Ball;
import orbits.data.Entity;
import orbits.data.LocalPlayer;
import orbits.data.Player;
import orbits.ingame.Game;
import orbits.network.client.PacketJoined;
import orbits.network.client.PacketPause;
import orbits.network.client.PacketPress;
import orbits.network.server.*;
import orbits.server.OrbitsServer;
import org.dyn4j.dynamics.Body;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class IngameGui extends ParentableAbstractGui {
    protected final OrbitsGame orbits;
    protected final Game game;
    protected final LevelGui levelGui;
    protected final EntityRenderer entityRenderer;
    protected final Int2ObjectMap<LocalPlayer> keybindToPlayer = new Int2ObjectOpenHashMap<>();
    protected final Texture ballTexture;
    protected final @Nullable OrbitsServer server;
    protected final int idModifier;
    protected Connection connection;
    protected boolean paused = false;

    public IngameGui(OrbitsGame orbits, Connection connection, @Nullable OrbitsServer server, int idModifier) throws GameException {
        super(orbits.launcher());
        this.orbits = orbits;
        this.server = server;
        this.idModifier = idModifier;
        this.connection = connection;
        game = orbits.currentLobby();
        connection.addHandler(PacketEntityData.class, (con, packet) -> {
            launcher().gameThread().submit(() -> {
                Int2ObjectMap<Entity> entities = game.entities();
                if (!entities.containsKey(packet.entity.entityId())) {
                    entities.put(packet.entity.entityId(), packet.entity);
                    if (packet.entity instanceof LocalPlayer) {
                        LocalPlayer lp = (LocalPlayer) packet.entity;
                        int kid = lp.keybindId() - idModifier;
                        if (keybindToPlayer.containsKey(kid)) {
                            LocalPlayer old = keybindToPlayer.get(kid);
                            lp.display(old.display());
                            keybindToPlayer.put(kid, lp);
                        }
                    }
                    Body body = game.setupBody(packet.entity);
                    game.physicsEngine().world().addBody(body);
                    game.setMotionToWorld(packet.entity);
                } else {
                    Ball b = (Ball) entities.get(packet.entity.entityId());
                    b.position().set(packet.entity.position());
                    b.color().set(packet.entity.color());
                    b.motion().set(packet.entity.motion());
                    game.setMotionToWorld(b);
                    game.setPositionToWorld(b);
                    if (b instanceof Player && packet.entity instanceof Player) {
                        Player p = (Player) b;
                        Player n = (Player) packet.entity;
                        p.dodgeMultiplier(game, n.dodgeMultiplier());
                        p.dodgeMultiplierApplied(n.dodgeMultiplierApplied());
                        p.currentOrbit(game.level(), n.currentOrbitsId());
                        p.orbiting(n.orbiting(), n.orbitingTheta());
                    }
                }
            });
        });
        connection.addHandler(PacketRemoveTrail.class, (con, packet) -> {
            launcher().gameThread().submit(() -> {
                Entity e = game.entities().get(packet.entityId);
                Ball t = (Ball) game.entities().get(packet.trailId);
                if (e instanceof Player) {
                    Player p = (Player) e;
                    t.prev().pull(t.pull());
                    t.pull(null);
                    t.ownerId(0);
                    p.updateMotion(game);
                    game.setupProjectile(p, t);
                    System.out.println("shoot");
                } else {
                    System.out.println("Player no player????");
                }
            }).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
        });
        connection.addHandler(PacketAddTrail.class, (con, packet) -> {
            launcher().gameThread().submit(() -> {
                Entity e = game.entities().get(packet.entityId);
                Ball t = (Ball) game.entities().get(packet.trailId);
                if (e instanceof Player) {
                    if (t.prev() != null) {
                        t.prev().pull(t.pull());
                        t.pull(null);
                    }
                    ((Player) e).addTrail(t);
                } else {
                    System.out.println("Entity: " + e + " not a player");
                }
            });
        });
        connection.addHandler(PacketEntityRemove.class, (con, packet) -> {
            launcher().gameThread().submit(() -> {
                Int2ObjectMap<Entity> entities = game.entities();
                Entity entity = entities.remove(packet.entityId);
                entity.entityId(0);
                game.physicsEngine().world().removeBody(entity.body);
                launcher().frame().renderThread().submit(() -> {
                    if (entity.model != null) {
                        entity.model.cleanup();
                    }
                });
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
        connection.addHandler(PacketPaused.class, (con, packet) -> {
            launcher().gameThread().submit(() -> {
                paused = packet.paused;
            });
        });

        ColorGui background = launcher().guiManager().createGui(ColorGui.class);
        background.color().set(.5F, .5F, .5F, 1);
        background.xProperty().bind(xProperty());
        background.yProperty().bind(yProperty());
        background.widthProperty().bind(widthProperty());
        background.heightProperty().bind(heightProperty());
        addGUI(background);

        levelGui = new LevelGui(orbits, game.level(), false);
        levelGui.widthProperty().bind(widthProperty());
        levelGui.heightProperty().bind(heightProperty());
        levelGui.xProperty().bind(xProperty());
        levelGui.yProperty().bind(yProperty());
        addGUI(levelGui);
        entityRenderer = new EntityRenderer();
        entityRenderer.widthProperty().bind(levelGui.realWidth());
        entityRenderer.heightProperty().bind(levelGui.realHeight());
        entityRenderer.xProperty().bind(levelGui.realX());
        entityRenderer.yProperty().bind(levelGui.realY());
        addGUI(entityRenderer);
        ballTexture = orbits.textureStorage().texture("ball.png");
        for (Player player : game.players()) {
            if (player instanceof LocalPlayer) {
                keybindToPlayer.put(((LocalPlayer) player).keybindId(), (LocalPlayer) player);
            }
        }
        if (server != null) {
            registerKeybindHandler(KeyboardKeybindEvent.CharacterKeybindEvent.class, event -> {
                if (event.character() != ' ') return;
                connection.sendPacket(new PacketPause());
            });
        }
    }

    @Override
    protected void doInit() throws GameException {
        connection.sendPacket(new PacketJoined());
    }

    @Override
    protected boolean doHandle(KeybindEvent entry) {
        LocalPlayer p = keybindToPlayer.get(entry.keybind().uniqueId());
        if (p != null) {
            connection.sendPacket(new PacketPress(entry.keybind().uniqueId()));
        }
        return true;
    }

    @Override
    protected void doUpdate() throws GameException {
        if (!paused) {
            game.physicsEngine().tick();
            redraw();
        }
    }

    @Override
    public void onClose() throws GameException {
        if (server != null) {
            server.stop();
        }
        if (connection != null) {
            connection.cleanup();
        }
        super.onClose();
    }

    private class EntityRenderer extends ParentableAbstractGui {
        private DrawContext context;
        private Texture2DModel ballModel;

        public EntityRenderer() {
            super(orbits.launcher());
        }

        @Override
        protected void doInit() throws GameException {
            context = launcher().contextProvider().loadContext(launcher().frame().framebuffer(), ContextProvider.ContextType.HUD);
            ballModel = new Texture2DModel((GLESTexture) ballTexture);
        }

        @Override
        protected void doCleanup() throws GameException {
            launcher().contextProvider().freeContext(context, ContextProvider.ContextType.HUD);
            for (Entity entity : game.entities().values()) {
                if (entity.model != null) {
                    entity.model.cleanup();
                    entity.model = null;
                    entity.gameItem = null;
                }
            }
            ballModel.cleanup();
        }

        @Override
        protected boolean doRender(float mouseX, float mouseY, float partialTick) throws GameException {
            for (Entity entity : game.entities().values()) {
                if (entity instanceof Ball) {
                    Ball ball = (Ball) entity;
                    if (entity.model == null) {
                        Model model = (ball instanceof Player ? (ball.ballItem = new GameItem(ballModel, false) {
                            @Override
                            public void applyToTransformationMatrix(Matrix4f transformationMatrix) {
                                if (ball.motion().x() == 0 && ball.motion().y() == 0) {
                                    super.applyToTransformationMatrix(transformationMatrix);
                                    return;
                                }
                                transformationMatrix.rotate(Math.toRadians(-rotation().x.floatValue()), new Vector3f((float) ball.motion().x(), (float) ball.motion().y(), 0).normalize());
                                transformationMatrix.translate(position().x.floatValue(), position().y.floatValue(), position().z.floatValue());
                                transformationMatrix.scale(scale().x.floatValue(), scale().y.floatValue(), scale().z.floatValue());
                            }
                        }) : (ball.ballItem = new GameItem(ballModel, false))).createModel();
                        if (ball instanceof LocalPlayer) {
                            GlyphStaticModel m = launcher().glyphProvider().loadStaticModel(Component.text(Character.toString(((LocalPlayer) ball).display())), 50);
                            GameItem gi = new GameItem(m);
                            ((LocalPlayer) ball).textColor = gi;
                            gi.scale(1 / 70F, 1 / 70F, 1);
                            gi.position().x.bind(NumberValue.constant(-.5F).multiply(m.width()).divide(70));
                            gi.position().y.bind(m.descent().subtract(1).divide(70));
                            model = new GLESCombinedModelsModel(model, gi.createModel());
                        }
                        entity.gameItem = new GameItem(model);
                        entity.model = entity.gameItem.createModel();
                    }
                    ball.ballItem.color().x.number(ball.color().x());
                    ball.ballItem.color().y.number(ball.color().y());
                    ball.ballItem.color().z.number(ball.color().z());
                    if (entity instanceof LocalPlayer) {
                        LocalPlayer p = (LocalPlayer) entity;
                        p.textColor.color().set(1 - ball.ballItem.color().x.floatValue(), 1 - ball.ballItem.color().y.floatValue(), 1 - ball.ballItem.color().z.floatValue(), 1);
                    }
                    if (ball instanceof Player) {
                        Player p = (Player) ball;
                        long diff = p.dodgeMultiplierApplied() + Player.DODGE_DURATION - System.currentTimeMillis();
                        if (diff > 0 && diff <= Player.DODGE_DURATION) {
                            p.ballItem.rotation().x.number((float) diff / Player.DODGE_DURATION * 360);
                        } else {
                            p.ballItem.rotation().x.number(0);
                        }
                    }
                    entity.gameItem.position().x.number(ball.position().x() * width());
                    entity.gameItem.position().y.number(ball.position().y() * height());
                    context.drawModel(entity.model, x(), y(), 0, 0, 0, 0, height() * game.playerSize() * game.scale(), height() * game.playerSize() * game.scale(), 0);
                }
            }
            return super.doRender(mouseX, mouseY, partialTick);
        }
    }
}
