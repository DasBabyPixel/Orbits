package orbits.lwjgl;

import gamelauncher.engine.util.GameException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Start {
    public static void main(String[] args) throws IOException, URISyntaxException, GameException {
        List<String> list = new ArrayList<>(Arrays.asList(args));
        list.add("internalPlugin:orbits.Orbits");
        gamelauncher.lwjgl.Start.main(list.toArray(new String[0]));
    }
}
