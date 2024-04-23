package net.phoboss.mirage.network.packets;


import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlock;
import net.phoboss.mirage.client.rendering.customworld.MirageStructure;
import net.phoboss.mirage.network.MirageNBTPacketHandler;


import java.util.ArrayList;
import java.util.List;


public class MirageNBTPacketC2S {

    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender){
        BlockPos mirageBlockEntityPos = buf.readBlockPos();
        String fileName = buf.readString();
        int mirageWorldIndex = buf.readInt();
        List<Integer> mirageFragmentCheckList = buf.readCollection(c -> new ArrayList<>(), PacketByteBuf::readInt);
        
        if(player.isDisconnected()){
            return;
        }
        ServerWorld level = player.getWorld();

        try {
            BlockState blockState = level.getBlockState(mirageBlockEntityPos);
            if(!(blockState.getBlock() instanceof MirageBlock)){
                player.sendMessage(new LiteralText("Mirage Projector Not Found"), false);
                throw new Exception("Mirage Projector Not Found");
            }

                    /*
                    MirageBlockEntity mirageBlockEntity = (MirageBlockEntity) be;
                    return saved structureNBT (not actually "save" strcutreNBTs as NBTFiles just let it run in the server's memory)
                    */

            Mirage.SERVER_THREAD_POOL.execute(() -> {
                try{
                    NbtCompound structureNBT = MirageStructure.getBuildingNbt(fileName);

                    List<NbtCompound> splitStructureNBTList = MirageStructure.splitStructureNBT(structureNBT);
                    int totalFragments = splitStructureNBTList.size();
                    for(int fragmentIdx=0; fragmentIdx<totalFragments; fragmentIdx++){
                        if(mirageFragmentCheckList.contains(fragmentIdx)){
                            continue;
                        }
                        NbtCompound splitStructureNBT = splitStructureNBTList.get(fragmentIdx);

                        PacketByteBuf message = PacketByteBufs.create();
                        message.writeBlockPos(mirageBlockEntityPos);
                        message.writeInt(mirageWorldIndex);
                        message.writeInt(fragmentIdx);
                        message.writeInt(totalFragments);
                        message.writeNbt(splitStructureNBT);
                        MirageNBTPacketHandler.sendToPlayer(message,player);
                    }


                    Mirage.LOGGER.info("Mirage File: "+fileName+" totalFragments:"+totalFragments+" MirageProjector: "+mirageBlockEntityPos + " For Client: "+player.getName());
                }catch (Exception e){
                    Mirage.LOGGER.error("Error on MirageLoader Thread: ",e);
                }
            });



            //MirageNBTPacketHandler.splitAndSendToPlayer(new MirageNBTPacketS2C(msg.mirageBlockPosition,,idx),player);
            //MirageNBTPacketHandler.sendToPlayer(new MirageNBTPacketS2C(mirageBlockEntityPos,nbtFile,idx),player);
        }catch(Exception e){
            Mirage.LOGGER.error("Error on mirage NBT file query",e);
            player.sendMessage(new LiteralText("Could not load NBT file: "+fileName), false);
        }
    }

}
