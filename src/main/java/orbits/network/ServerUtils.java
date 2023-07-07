package orbits.network;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataMemory;
import gamelauncher.engine.network.Connection;
import gamelauncher.engine.network.packet.Packet;
import gamelauncher.engine.network.packet.PacketEncoder;
import gamelauncher.engine.network.packet.PacketNotRegisteredException;
import gamelauncher.netty.standalone.packet.c2s.PacketPayloadOutC2S;
import gamelauncher.netty.standalone.packet.c2s.PacketPayloadOutS2A;
import gamelauncher.netty.standalone.packet.c2s.PacketPayloadOutS2C;
import java8.util.concurrent.CompletableFuture;

import java.util.Arrays;

public class ServerUtils {
    public static void clientSendPacket(PacketEncoder encoder, Connection connection, Packet packet) {
        PacketPayloadOutC2S p = new PacketPayloadOutC2S();
        p.data = prepareSendPacket(encoder, packet);
        connection.sendPacket(p);
    }

    public static CompletableFuture<Void> clientSendPacketAsync(PacketEncoder encoder, Connection connection, Packet packet) {
        PacketPayloadOutC2S p = new PacketPayloadOutC2S();
        p.data = prepareSendPacket(encoder, packet);
        return connection.sendPacketAsync(p);
    }

    public static void serverSendPacket(PacketEncoder encoder, String serverId, Connection connection, int targetClient, Packet packet) {
        connection.sendPacket(new PacketPayloadOutS2C(serverId, targetClient, prepareSendPacket(encoder, packet)));
    }

    public static CompletableFuture<Void> serverSendPacketAsync(PacketEncoder encoder, String serverId, Connection connection, int targetClient, Packet packet) {
        return connection.sendPacketAsync(new PacketPayloadOutS2C(serverId, targetClient, prepareSendPacket(encoder, packet)));
    }

    public static void serverSendPacketAll(PacketEncoder encoder, String serverId, Connection connection, Packet packet) {
        connection.sendPacket(new PacketPayloadOutS2A(serverId, prepareSendPacket(encoder, packet)));
    }

    public static CompletableFuture<Void> serverSendPacketAllAsync(PacketEncoder encoder, String serverId, Connection connection, Packet packet) {
        return connection.sendPacketAsync(new PacketPayloadOutS2A(serverId, prepareSendPacket(encoder, packet)));
    }

    public static byte[] prepareSendPacket(PacketEncoder encoder, Packet packet) {
        DataMemory dm = new DataMemory();
        DataBuffer db = new DataBuffer(dm);
        try {
            encoder.write(db, packet);
        } catch (PacketNotRegisteredException e) {
            throw new RuntimeException(e);
        }
        return Arrays.copyOf(dm.array(), db.writerIndex());
    }

    public static Packet receivePayload(PacketEncoder encoder, Connection connection, byte[] payload) {
        DataMemory dm = new DataMemory(payload);
        DataBuffer db = new DataBuffer(dm);
        db.writerIndex(payload.length);
        try {
            Packet p = encoder.read(db);
            return p;
        } catch (PacketNotRegisteredException e) {
            throw new RuntimeException(e);
        }
    }
}
