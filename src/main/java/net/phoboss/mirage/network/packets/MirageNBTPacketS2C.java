package net.phoboss.mirage.network.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntity;


public class MirageNBTPacketS2C {

    public static void receive(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf message, PacketSender packetSender) {
        BlockPos mirageBlockEntityPos = message.readBlockPos();
        int mirageWorldIndex = message.readInt();
        int fragmentIdx = message.readInt();
        int totalFragments = message.readInt();
        NbtCompound nbtMirageFragment = message.readNbt();


        BlockEntity be = minecraftClient.world.getBlockEntity(mirageBlockEntityPos);
        if(be instanceof MirageBlockEntity mbe){
            try {
                mbe.uploadMirageFragment(mirageWorldIndex,fragmentIdx,totalFragments,nbtMirageFragment);
            } catch (Exception e) {
                Mirage.LOGGER.error("Error on uploading Mirage: "+mbe.getFileNames().get(mirageWorldIndex)+" Fragment: "+fragmentIdx,e);
            }
        }
    }
}
