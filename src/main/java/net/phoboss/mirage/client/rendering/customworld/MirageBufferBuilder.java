package net.phoboss.mirage.client.rendering.customworld;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class MirageBufferBuilder extends BufferBuilder {
    public MirageBufferBuilder(int initialCapacity) {
        super(initialCapacity);
    }
    private BlockPos actualPos = new BlockPos(0,0,0);
    private Vec3d actualVector = new Vec3d(0,0,0);
    public void setActualPos(BlockPos actualPos) {
        this.actualPos = actualPos;
        actualVector = new Vec3d(actualPos.getX(),actualPos.getY(),actualPos.getZ());
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        Vec3d recreatedBitwisePos = new Vec3d(  actualPos.getX() & 15,
                                                actualPos.getY() & 15,
                                                actualPos.getZ() & 15);
        Vec3d actualVertex = new Vec3d(x,y,z).subtract(recreatedBitwisePos).add(actualVector);//basically replacing the recreatedBitwisePos with the actualVector
        return super.vertex(actualVertex.x,actualVertex.y,actualVertex.z);
    }
}
