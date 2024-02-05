package net.phoboss.mirage.client.rendering.customworld;


import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class MirageBufferBuilder extends BufferBuilder {
    public MirageBufferBuilder(int initialCapacity) {
        super(initialCapacity);
    }
    private BlockPos actualPos = new BlockPos(0,0,0);
    private Vec3 actualVector = new Vec3(0,0,0);
    public void setActualPos(BlockPos actualPos) {
        this.actualPos = actualPos;
        actualVector = new Vec3(actualPos.getX(),actualPos.getY(),actualPos.getZ());
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        Vec3 recreatedBitwisePos = new Vec3(  actualPos.getX() & 15,
                                                actualPos.getY() & 15,
                                                actualPos.getZ() & 15);
        Vec3 actualVertex = new Vec3(x,y,z).subtract(recreatedBitwisePos).add(actualVector);//basically replacing the recreatedBitwisePos with the actualVector
        return super.vertex(actualVertex.x,actualVertex.y,actualVertex.z);
    }
}
