package net.phoboss.mirage.network.packets;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.Frame;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlock;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntity;
import net.phoboss.mirage.client.rendering.customworld.MirageStructure;
import net.phoboss.mirage.network.MirageNBTPacketHandler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class MirageNBTPacketC2S {

    int phoneBookIdx;

    String fileName;

    int mirageWorldIndex;

    List<Integer> mirageFragmentCheckList;
    public MirageNBTPacketC2S() {
    }
    public MirageNBTPacketC2S(int phoneBookIdx, String file, int mirageWorldIndex, List<Integer> mirageFragmentCheckList) {
        this.phoneBookIdx = phoneBookIdx;
        this.fileName = file;
        this.mirageWorldIndex = mirageWorldIndex;
        this.mirageFragmentCheckList = mirageFragmentCheckList;
    }
    public MirageNBTPacketC2S(FriendlyByteBuf buf) {
        this.phoneBookIdx = buf.readInt();
        this.fileName = buf.readUtf();
        this.mirageWorldIndex = buf.readInt();
        this.mirageFragmentCheckList = buf.readCollection(c -> new ArrayList<>(), FriendlyByteBuf::readInt);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.phoneBookIdx);
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

                int phoneBookIdx = msg.phoneBookIdx;
                String fileName = msg.fileName;
                int mirageWorldIdx = msg.mirageWorldIndex;
                List<Integer> checkList = msg.mirageFragmentCheckList;

                try {

                    /*
                    MirageBlockEntity mirageBlockEntity = (MirageBlockEntity) be;
                    return saved structureNBT (not actually "save" strcutreNBTs as NBTFiles just let it run in the server's memory)
                    */

                    Mirage.SERVER_THREAD_POOL.execute(() -> {
                        try{
                            CompoundTag structureNBT = MirageStructure.getBuildingNbt(fileName);

                            List<CompoundTag> splitStructureNBTList = MirageStructure.splitStructureNBT(structureNBT);
                            int totalFragments = splitStructureNBTList.size();
                            for(int fragmentIdx=0; fragmentIdx<totalFragments; fragmentIdx++){
                                if(checkList.contains(fragmentIdx)){
                                    continue;
                                }
                                CompoundTag splitStructureNBT = splitStructureNBTList.get(fragmentIdx);

                                MirageNBTPacketHandler.sendToPlayer(new MirageNBTPacketS2C(phoneBookIdx, mirageWorldIdx, fragmentIdx, totalFragments, splitStructureNBT),player);
                            }


                            Mirage.LOGGER.info("Mirage File: "+fileName+" totalFragments:"+totalFragments + " For Client: "+player.getName());
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
