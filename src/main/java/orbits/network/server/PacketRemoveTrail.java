package orbits.network.server;

public class PacketRemoveTrail extends PacketAddTrail {
    public PacketRemoveTrail() {
        this(0, 0);
    }

    public PacketRemoveTrail(int entityId, int trailId) {
        super("remove_trail", entityId, trailId);
    }
}
