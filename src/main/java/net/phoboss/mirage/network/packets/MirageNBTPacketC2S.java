package net.phoboss.mirage.network.packets;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.Frame;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntity;
import net.phoboss.mirage.client.rendering.customworld.MirageStructure;
import net.phoboss.mirage.network.MirageNBTPacketHandler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class MirageNBTPacketC2S {
    BlockPos mirageBlockPosition;
    String fileName;

    int mirageWorldIndex;

    List<Integer> mirageFragmentCheckList;
    public MirageNBTPacketC2S() {
    }
    public MirageNBTPacketC2S(BlockPos pos, String file, int mirageWorldIndex, List<Integer> mirageFragmentCheckList) {
        this.mirageBlockPosition = pos;
        this.fileName = file;
        this.mirageWorldIndex = mirageWorldIndex;
        this.mirageFragmentCheckList = mirageFragmentCheckList;
    }
    public MirageNBTPacketC2S(FriendlyByteBuf buf) {
        this.mirageBlockPosition = buf.readBlockPos();
        this.fileName = buf.readUtf();
        this.mirageWorldIndex = buf.readInt();
        this.mirageFragmentCheckList = buf.readCollection(c -> new ArrayList<>(), FriendlyByteBuf::readInt);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.mirageBlockPosition);
        buf.writeUtf(this.fileName);
        buf.writeInt(this.mirageWorldIndex);
        buf.writeCollection(this.mirageFragmentCheckList, FriendlyByteBuf::writeInt);
    }

    public static class Handler {
        public Handler() {
        }

        //executed on Server side
        public static boolean handle(MirageNBTPacketC2S msg, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                Level level = player.getLevel();
                if(!player.connection.connection.isConnected()){
                    return;
                }

                BlockPos mirageBlockEntityPos = msg.mirageBlockPosition;
                String fileName = msg.fileName;
                int mirageWorldIdx = msg.mirageWorldIndex;
                List<Integer> checkList = msg.mirageFragmentCheckList;

                try {
                    BlockEntity be = level.getBlockEntity(mirageBlockEntityPos);
                    if(be == null || be.isRemoved()){
                        player.displayClientMessage(new TextComponent("Mirage Projector Not Found"), false);
                        throw new Exception("Mirage Projector Not Found");
                    }

                    /*
                    MirageBlockEntity mirageBlockEntity = (MirageBlockEntity) be;
                    return saved structureNBT (not actually "save" strcutreNBTs as NBTFiles just let it run in the server's memory)
                    */

                    Mirage.SERVER_THREAD_POOL.submit(() -> {
                        try{
                            CompoundTag structureNBT = MirageBlockEntity.getBuildingNbt(fileName);

                            List<CompoundTag> splitStructureNBTList = MirageStructure.splitStructureNBT(structureNBT);
                            int totalFragments = splitStructureNBTList.size();
                            for(int fragmentIdx=0; fragmentIdx<totalFragments; fragmentIdx++){
                                if(checkList.contains(fragmentIdx)){
                                    continue;
                                }
                                CompoundTag splitStructureNBT = splitStructureNBTList.get(fragmentIdx);

                                MirageNBTPacketHandler.sendToPlayer(new MirageNBTPacketS2C(mirageBlockEntityPos, mirageWorldIdx, fragmentIdx, totalFragments, false, splitStructureNBT),player);
                            }

                            player.displayClientMessage(new TextComponent("loading: "+fileName+" totalFragments:"+totalFragments), false);
                        }catch (Exception e){
                            Mirage.LOGGER.error("Error on MirageLoader Thread: ",e);
                        }
                    });



                    //MirageNBTPacketHandler.splitAndSendToPlayer(new MirageNBTPacketS2C(msg.mirageBlockPosition,,idx),player);
                    //MirageNBTPacketHandler.sendToPlayer(new MirageNBTPacketS2C(mirageBlockEntityPos,nbtFile,idx),player);
                }catch(Exception e){
                    Mirage.LOGGER.error("Error on mirage NBT file query",e);
                    player.displayClientMessage(new TextComponent("Could not load NBT file: "+fileName), false);
                }

                //player.displayClientMessage(new TextComponent("THIS MESSAGE IS FROM THE SERVER"), false);

            });
            return true;
        }
    }

}
