package orbits.lwjgl;

import gamelauncher.engine.data.DataUtil;
import gamelauncher.engine.resource.ResourceStream;
import gamelauncher.engine.util.GameException;
import gamelauncher.gles.texture.PNGDecoder;
import gamelauncher.lwjgl.LWJGLGameLauncher;
import orbits.Orbits;
import org.fusesource.jansi.AnsiConsole;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Start extends LWJGLGameLauncher {
    public Start() throws GameException {
        super();
    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        Runtime.getRuntime().addShutdownHook(new Thread(AnsiConsole::systemUninstall));
        try {
            new Start().start(args);
        } catch (GameException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void start0() throws GameException {
        super.start0();

        ResourceStream s = resourceLoader().resource(embedFileSystem().getPath("orbits64x64.png")).newResourceStream();
        ByteArrayInputStream bin = new ByteArrayInputStream(s.readAllBytes());
        s.cleanup();
        try {
            PNGDecoder decoder = new PNGDecoder(bin);
            getGLFWThread().submit(() -> {
                ByteBuffer pixels64 = memoryManagement().allocDirect(decoder.getWidth() * decoder.getHeight() * DataUtil.BYTES_INT);
                try {
                    decoder.decode(pixels64, decoder.getWidth() * DataUtil.BYTES_INT, PNGDecoder.Format.RGBA);
                } catch (IOException e) {
                    throw new GameException(e);
                }
                pixels64.flip();
                GLFWImage.Buffer buffer = GLFWImage.malloc(1);
                buffer.position(0).width(decoder.getWidth()).height(decoder.getHeight()).pixels(pixels64);
                buffer.position(0);

                GLFW.glfwSetWindowIcon(frame().getGLFWId(), buffer);
                buffer.free();
                memoryManagement().free(pixels64);
            });
        } catch (IOException e) {
            throw new GameException(e);
        }
    }

    @Override
    protected void loadCustomPlugins() {
        pluginManager().loadPlugin(new Orbits());
    }
}
