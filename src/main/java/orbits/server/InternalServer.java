package orbits.server;

import gamelauncher.engine.network.Connection;
import gamelauncher.engine.util.GameException;
import orbits.OrbitsGame;
import orbits.data.level.Level;

public class InternalServer extends OrbitsServer {
    private final P2PConnection connection = new P2PConnection();

    public InternalServer(OrbitsGame orbits, Level level) {
        super(orbits, level);
    }

    public Connection clientConnection() {
        return connection.point();
    }

    @Override
    protected void stop0() throws GameException {
        super.stop0();
        connection.cleanup();
        connection.point().cleanup();
    }

    @Override
    protected Connection createConnection() throws GameException {
        return connection;
    }
}
