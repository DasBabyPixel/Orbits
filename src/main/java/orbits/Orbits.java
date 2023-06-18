package orbits;

import gamelauncher.engine.plugin.Plugin;
import gamelauncher.engine.plugin.Plugin.GamePlugin;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.Key;
import gamelauncher.engine.util.logging.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Locale;

@GamePlugin
public class Orbits extends Plugin {
    private OrbitsGame game;

    public Orbits() {
        super("orbits");
    }

    @Override
    public void onEnable() throws GameException {
        Logger.asyncLogStream().async(false);
        game = new OrbitsGame(this);
        launcher().languageManager().language(Locale.ENGLISH).load(new Key(this, "languages/en.json"));
        launcher().gameRegistry().register(game);
        launcher().eventManager().registerListener(game);
        try {
            NetworkInterface.networkInterfaces().forEach(i->{
                System.out.println(i + " " + Collections.list(i.getInetAddresses()));
            });
            for (InetAddress localhost : InetAddress.getAllByName("localhost")) {
                System.out.println(localhost);
            }
        } catch (UnknownHostException | SocketException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        launcher().gameRegistry().unregister(game.key());
        launcher().eventManager().unregisterListener(game);
    }

    public OrbitsGame game() {
        return game;
    }
}
