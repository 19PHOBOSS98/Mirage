package net.phoboss.mirage.network;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.filters.VanillaPacketSplitter;
import net.minecraftforge.network.simple.SimpleChannel;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.network.packets.MirageNBTPacketC2S;
import net.phoboss.mirage.network.packets.MirageNBTPacketS2C;

import java.util.ArrayList;
import java.util.List;

public class MirageNBTPacketHandler {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Mirage.MOD_ID,"mirage_nbt_channel"))
                .networkProtocolVersion(() -> "1.0")
                .serverAcceptedVersions(version -> true)
                .clientAcceptedVersions(version -> true)
                .simpleChannel();



        net.messageBuilder(MirageNBTPacketC2S.class,id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MirageNBTPacketC2S::new)
                .encoder(MirageNBTPacketC2S::toBytes)
                .consumerMainThread(MirageNBTPacketC2S.Handler::handle)
                .add();

        net.messageBuilder(MirageNBTPacketS2C.class,id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MirageNBTPacketS2C::new)
                .encoder(MirageNBTPacketS2C::toBytes)
                .consumerMainThread(MirageNBTPacketS2C.Handler::handle)
                .add();

        INSTANCE = net;
    }

    public static <MSG> void sendToServer(MSG message){
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void splitAndSendToPlayer(MSG message, ServerPlayer player){
        Packet packet = INSTANCE.toVanillaPacket(message, NetworkDirection.PLAY_TO_CLIENT);
        List<Packet<?>> splitPackets = new ArrayList<Packet<?>>();
        VanillaPacketSplitter.appendPackets(ConnectionProtocol.PLAY, PacketFlow.CLIENTBOUND, packet, splitPackets);
        splitPackets.forEach(player.connection::send);
    }
}








