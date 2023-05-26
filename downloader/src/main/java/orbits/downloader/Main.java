package orbits.downloader;

import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        System.setProperty("java.net.useSystemProxies", "true");
        URL url = new URL("https://github.com/DasBabyPixel/Orbits/releases/download/1.0/orbits.bat");
        InputStream in = url.openStream();

        Path temp = Files.createTempFile("orbits", ".bat");
        Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
        in.close();

        Process process = Runtime.getRuntime().exec("\"" + temp.toAbsolutePath() + "\"");
        InputStream is = process.getInputStream();
        int b;
        while ((b = is.read()) != -1) {
            System.out.write(b);
        }
        process.onExit().get();
    }
}
