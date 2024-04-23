package net.phoboss.mirage.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.network.packets.MirageNBTPacketC2S;
import net.phoboss.mirage.network.packets.MirageNBTPacketS2C;

public class MirageNBTPacketHandler {
    public static final Identifier MIRAGE_NBT_CHANNEL = new Identifier(Mirage.MOD_ID, "mirage_nbt_channel");
    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(MIRAGE_NBT_CHANNEL,MirageNBTPacketC2S::receive);
        ClientPlayNetworking.registerGlobalReceiver(MIRAGE_NBT_CHANNEL,MirageNBTPacketS2C::receive);
    }
    public static void registerS2CPackets() {

    }

    public static void sendToServer(PacketByteBuf message){
        ClientPlayNetworking.send(MIRAGE_NBT_CHANNEL, message);
    }

    public static void sendToPlayer(PacketByteBuf message, ServerPlayerEntity player){
        ServerPlayNetworking.send( player,MIRAGE_NBT_CHANNEL, message);
    }
}








