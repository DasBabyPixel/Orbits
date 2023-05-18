package orbits.gui;

import gamelauncher.engine.render.texture.Texture;
import gamelauncher.engine.resource.AbstractGameResource;
import gamelauncher.engine.util.GameException;
import java8.util.concurrent.CompletableFuture;
import orbits.OrbitsGame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TextureStorage extends AbstractGameResource {
    private final OrbitsGame orbitsGame;
    private final Map<String, Texture> textures = new ConcurrentHashMap<>();

    public TextureStorage(OrbitsGame orbitsGame) {
        this.orbitsGame = orbitsGame;
    }

    @Override
    protected CompletableFuture<Void> cleanup0() throws GameException {
        CompletableFuture<?>[] futs = new CompletableFuture[textures.size()];
        int i = 0;
        for (Texture texture : textures.values()) {
            futs[i++] = texture.cleanup();
        }
        return CompletableFuture.allOf(futs);
    }

    public Texture texture(String name) {
        return textures.computeIfAbsent(name, s -> {
            try {
                Texture texture = orbitsGame.launcher().textureManager().createTexture();
                texture.uploadAsync(orbitsGame.launcher().resourceLoader().resource(orbitsGame.key().withKey("textures/" + s).toPath(orbitsGame.launcher().assets())).newResourceStream()).thenRun(() -> orbitsGame.launcher().guiManager().redrawAll());
                return texture;
            } catch (GameException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
