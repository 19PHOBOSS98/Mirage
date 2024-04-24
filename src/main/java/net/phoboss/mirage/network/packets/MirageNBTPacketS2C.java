package net.phoboss.mirage.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntity;

import java.util.function.Supplier;

public class MirageNBTPacketS2C {
    public int phoneBookIdx;
    public CompoundTag nbtMirageFragment;
    public int mirageWorldIndex;
    public int fragmentIdx;
    public int totalFragmentCount;
    public boolean startRendering;

    public MirageNBTPacketS2C() {
        this.phoneBookIdx = 0;
        this.mirageWorldIndex = 0;
        this.fragmentIdx = 0;
        this.totalFragmentCount = 0;
        this.startRendering = false;
        this.nbtMirageFragment = new CompoundTag();
    }

    public MirageNBTPacketS2C(int phoneBookIdx, int mirageWorldIdx, int fragmentIdx, int totalFragmentCount,boolean startRendering, CompoundTag mirageFragment) {
        this.phoneBookIdx = phoneBookIdx;
        this.mirageWorldIndex = mirageWorldIdx;
        this.fragmentIdx = fragmentIdx;
        this.totalFragmentCount = totalFragmentCount;
        this.startRendering = startRendering;
        this.nbtMirageFragment = mirageFragment;
    }
    public MirageNBTPacketS2C(FriendlyByteBuf buf) {
        this.phoneBookIdx = buf.readInt();
        this.mirageWorldIndex = buf.readInt();
        this.fragmentIdx = buf.readInt();
        this.totalFragmentCount = buf.readInt();
        this.startRendering = buf.readBoolean();
        this.nbtMirageFragment = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.phoneBookIdx);
        buf.writeInt(this.mirageWorldIndex);
        buf.writeInt(this.fragmentIdx);
        buf.writeInt(this.totalFragmentCount);
        buf.writeBoolean(this.startRendering);
        buf.writeNbt(this.nbtMirageFragment);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(MirageNBTPacketS2C msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {

                        MirageBlockEntity mirageBlockEntity = Mirage.getBlockEntityPhoneBook(msg.phoneBookIdx);

                        if(mirageBlockEntity == null){
                            return;
                        }

                        try {
                            mirageBlockEntity.uploadMirageFragment(msg.mirageWorldIndex,msg.fragmentIdx,msg.totalFragmentCount,msg.nbtMirageFragment);
                        } catch (Exception e) {
                            Mirage.LOGGER.error("Error on uploading Mirage: "+mirageBlockEntity.getFileNames().get(msg.mirageWorldIndex)+" Fragment: "+msg.fragmentIdx,e);
                            ctx.get().setPacketHandled(false);
                        }
                    })
            );
            ctx.get().setPacketHandled(true);
        }
    }
}
