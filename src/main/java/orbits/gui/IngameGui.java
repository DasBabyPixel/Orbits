package orbits.gui;

import de.dasbabypixel.api.property.NumberValue;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ColorGui;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.render.ContextProvider;
import gamelauncher.engine.render.DrawContext;
import gamelauncher.engine.render.GameItem;
import gamelauncher.engine.render.model.GlyphStaticModel;
import gamelauncher.engine.render.model.Model;
import gamelauncher.engine.render.texture.Texture;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.concurrent.Threads;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.KeyboardKeybindEvent;
import gamelauncher.engine.util.text.Component;
import gamelauncher.gles.model.GLESCombinedModelsModel;
import gamelauncher.gles.model.Texture2DModel;
import gamelauncher.gles.texture.GLESTexture;
import gamelauncher.netty.standalone.PacketClientDisconnected;
import it.unimi.dsi.fastutil.ints.*;
import orbits.OrbitsGame;
import orbits.data.Ball;
import orbits.data.Entity;
import orbits.data.LocalPlayer;
import orbits.data.Player;
import orbits.lobby.Lobby;
import orbits.network.AbstractServerWrapperConnection;
import orbits.network.PacketPress;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class IngameGui extends ParentableAbstractGui {
    private final OrbitsGame orbits;
    private final Lobby lobby;
    private final LevelGui levelGui;
    private final EntityRenderer entityRenderer;
    private final Int2ObjectMap<LocalPlayer> keybindToPlayer = new Int2ObjectOpenHashMap<>();
    private final Texture ballTexture;
    private final PacketHandler<PacketPress> press = (connection, packet) -> launcher().gameThread().runLater(() -> press(connection, packet));
    private final PacketHandler<PacketClientDisconnected> clientDisconnected;
    private boolean paused = false;

    public IngameGui(OrbitsGame orbits) throws GameException {
        super(orbits.launcher());
        this.orbits = orbits;
        lobby = orbits.currentLobby();
        clientDisconnected = (connection, packet) -> {
            Int2IntMap map = connection.storedValue(StartIngameGuiMultiplayerOwner.mapped_ids);
            if (map == null) return;
            IntIterator it = map.values().intIterator();
            while (it.hasNext()) {
                int id = it.nextInt();
                it.remove();
                launcher().gameThread().runLater(() -> {
                    Player p = keybindToPlayer.get(id);
                    if (p != null) {
                        lobby.kill(p, null);
                    }
                });
            }
        };
        if (lobby.serverConnection() != null) {
            lobby.serverConnection().addHandler(PacketPress.class, press);
            ((AbstractServerWrapperConnection) lobby.serverConnection()).connection().addHandler(PacketClientDisconnected.class, clientDisconnected);
        }
        ColorGui background = launcher().guiManager().createGui(ColorGui.class);
        background.color().set(.5F, .5F, .5F, 1);
        background.xProperty().bind(xProperty());
        background.yProperty().bind(yProperty());
        background.widthProperty().bind(widthProperty());
        background.heightProperty().bind(heightProperty());
        addGUI(background);

        levelGui = new LevelGui(orbits, lobby.level(), false);
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
        for (Player player : lobby.players()) {
            if (player instanceof LocalPlayer) {
                keybindToPlayer.put(((LocalPlayer) player).keybindId(), (LocalPlayer) player);
            }
        }
        registerKeybindHandler(KeyboardKeybindEvent.CharacterKeybindEvent.class, event -> {
            if (event.character() != ' ') return;
            paused = !paused;
        });
    }

    @Override
    public void onClose() throws GameException {
        if (lobby.serverConnection() != null) {
            lobby.serverConnection().removeHandler(PacketPress.class, press);
            ((AbstractServerWrapperConnection) lobby.serverConnection()).connection().removeHandler(PacketClientDisconnected.class, clientDisconnected);
            Threads.await(lobby.serverConnection().cleanup());
        }
        super.onClose();
    }

    private void press(Connection connection, PacketPress packet) {
        Int2IntMap map = connection.storedValue(StartIngameGuiMultiplayerOwner.mapped_ids, Int2IntOpenHashMap::new);
        if (!map.containsKey(packet.id)) return;
        int oid = map.get(packet.id);
        Player p = keybindToPlayer.get(oid);
        if (p != null) lobby.tap(p);
    }

    @Override
    protected boolean doHandle(KeybindEvent entry) {
        LocalPlayer p = keybindToPlayer.get(entry.keybind().uniqueId());
        if (p != null) {
            lobby.tap(p);
        }
        return true;
    }

    @Override
    protected void doUpdate() throws GameException {
        if (!paused) {
            lobby.physicsEngine().tick();
            redraw();
        }
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
            for (Entity entity : lobby.entities().values()) {
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
            for (Entity entity : lobby.entities().values()) {
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
                    context.drawModel(entity.model, x(), y(), 0, 0, 0, 0, height() * lobby.playerSize() * lobby.scale(), height() * lobby.playerSize() * lobby.scale(), 0);
                }
            }
            return super.doRender(mouseX, mouseY, partialTick);
        }
    }
}
