package net.phoboss.mirage.network.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntity;


public class MirageNBTPacketS2C {

    public static void receive(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf message, PacketSender packetSender) {
        int phoneBookIdx = message.readInt();
        int mirageWorldIndex = message.readInt();
        int fragmentIdx = message.readInt();
        int totalFragments = message.readInt();
        NbtCompound nbtMirageFragment = message.readNbt();


        MirageBlockEntity mirageBlockEntity = Mirage.getBlockEntityPhoneBook(phoneBookIdx);
        if(mirageBlockEntity == null){
            return;
        }
            try {
                mirageBlockEntity.uploadMirageFragment(mirageWorldIndex,fragmentIdx,totalFragments,nbtMirageFragment);
            } catch (Exception e) {
                Mirage.LOGGER.error("Error on uploading Mirage: "+mirageBlockEntity.getFileNames().get(mirageWorldIndex)+" Fragment: "+fragmentIdx,e);
            }

    }
}
