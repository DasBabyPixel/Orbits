package orbits.gui;

import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.packet.PacketHandler;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.concurrent.Threads;
import gamelauncher.netty.standalone.PacketClientDisconnected;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import orbits.OrbitsGame;
import orbits.data.Player;
import orbits.network.AbstractServerWrapperConnection;
import orbits.network.PacketPress;

public class IngameGuiOwner extends IngameGui {
    private final PacketHandler<PacketPress> press = (connection, packet) -> launcher().gameThread().runLater(() -> press(connection, packet));
    private final PacketHandler<PacketClientDisconnected> clientDisconnected;

    public IngameGuiOwner(OrbitsGame orbits) throws GameException {
        super(orbits);
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
    }

    private void press(Connection connection, PacketPress packet) {
        Int2IntMap map = connection.storedValue(StartIngameGuiMultiplayerOwner.mapped_ids, Int2IntOpenHashMap::new);
        if (!map.containsKey(packet.id)) return;
        int oid = map.get(packet.id);
        Player p = keybindToPlayer.get(oid);
        if (p != null) lobby.tap(p);
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
}
