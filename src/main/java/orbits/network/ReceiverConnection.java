package orbits.network;

import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.packet.Packet;

public interface ReceiverConnection extends Connection {
    void receivePacket(Packet packet);
}
