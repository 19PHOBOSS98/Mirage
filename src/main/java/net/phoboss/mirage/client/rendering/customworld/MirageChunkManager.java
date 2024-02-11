package net.phoboss.mirage.client.rendering.customworld;


import net.minecraft.core.BlockPos;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ChunkHolder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class MirageChunkManager extends ChunkSource {
    private final Level mirageWorld;
    private Map<ChunkPos, ChunkAccess> chunks;
    public MirageChunkManager(Level world) {
        this.chunks = new HashMap<>();
        this.mirageWorld = world;
    }

    @Override
    public BlockGetter getLevel() {
        return this.mirageWorld;
    }


    @Nullable
    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
        return getChunk(x,z);
    }


    public ChunkAccess getChunk(int x, int z) {
        ChunkPos key = new ChunkPos(x, z);
        if(chunks.containsKey(key)){
            return chunks.get(key);
        }
        MirageChunk mirageChunk = new MirageChunk(this.mirageWorld,key);
        chunks.put(key,mirageChunk);
        return mirageChunk;
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking, boolean tickChunks) {

    }

    @Override
    public String gatherStats() {
        return null;
    }

    @Override
    public int getLoadedChunksCount() {
        return 0;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return null;
    }

    public static class MirageChunk extends LevelChunk {

        public MirageChunk(Level world, ChunkPos chunkPos) {
            super(world, chunkPos);
        }

        public BlockState getBlockState(BlockPos blockPos) {
            return Blocks.VOID_AIR.defaultBlockState();
        }

        @Nullable
        @Override
        public BlockState setBlockState(BlockPos pos, BlockState state, boolean moved) {
            return null;
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return Fluids.EMPTY.defaultFluidState();
        }

        @Override
        public int getLightEmission(BlockPos pos) {
            return 0;
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pPos, EntityCreationType pCreationType) {
            return super.getBlockEntity(pPos, pCreationType);
        }


        @Override
        public void addAndRegisterBlockEntity(BlockEntity blockEntity) {

        }

        @Override
        public void setUnsaved(boolean needsSaving) {}

        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean isYSpaceEmpty(int lowerHeight, int upperHeight) {
            return super.isYSpaceEmpty(lowerHeight, upperHeight);
        }

        @Override
        public FullChunkStatus getFullStatus() {
            return FullChunkStatus.FULL;
        }
    }
}
