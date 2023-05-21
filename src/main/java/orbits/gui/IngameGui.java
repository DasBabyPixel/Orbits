package orbits.gui;

import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.render.ContextProvider;
import gamelauncher.engine.render.DrawContext;
import gamelauncher.engine.render.GameItem;
import gamelauncher.engine.render.texture.Texture;
import gamelauncher.engine.util.GameException;
import gamelauncher.gles.model.Texture2DModel;
import gamelauncher.gles.texture.GLESTexture;
import orbits.OrbitsGame;
import orbits.data.Ball;
import orbits.data.Entity;
import orbits.lobby.Lobby;

public class IngameGui extends ParentableAbstractGui {
    private final OrbitsGame orbits;
    private final Lobby lobby;
    private final LevelGui levelGui;
    private final EntityRenderer entityRenderer;
    private final Texture ballTexture;

    public IngameGui(OrbitsGame orbits) throws GameException {
        super(orbits.launcher());
        this.orbits = orbits;
        lobby = orbits.currentLobby();
        levelGui = new LevelGui(orbits, lobby.level(), false);
        levelGui.widthProperty().bind(widthProperty());
        levelGui.heightProperty().bind(heightProperty());
        levelGui.xProperty().bind(xProperty());
        levelGui.yProperty().bind(yProperty());
        addGUI(levelGui);
        entityRenderer = new EntityRenderer();
        entityRenderer.widthProperty().bind(widthProperty());
        entityRenderer.heightProperty().bind(heightProperty());
        entityRenderer.xProperty().bind(xProperty());
        entityRenderer.yProperty().bind(yProperty());
        addGUI(entityRenderer);
        ballTexture = orbits.textureStorage().texture("ball.png");
    }

    @Override
    protected void doUpdate() throws GameException {
        lobby.physicsEngine().tick();
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
            ballModel.cleanup();
        }

        @Override
        protected boolean doRender(float mouseX, float mouseY, float partialTick) throws GameException {
            for (Entity entity : lobby.entities().values()) {
                if (entity instanceof Ball) {
                    Ball ball = (Ball) entity;
                    if (entity.model == null) {
                        entity.gameItem = new GameItem(ballModel, false);
                        entity.model = entity.gameItem.createModel();
                    }
                    entity.gameItem.color().x.number(ball.color().x());
                    entity.gameItem.color().y.number(ball.color().y());
                    entity.gameItem.color().z.number(ball.color().z());
                    context.drawModel(entity.model);
                }
            }
            return super.doRender(mouseX, mouseY, partialTick);
        }
    }
}
