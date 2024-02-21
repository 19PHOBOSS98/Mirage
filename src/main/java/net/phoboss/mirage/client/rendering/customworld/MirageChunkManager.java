package net.phoboss.mirage.client.rendering.customworld;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class MirageChunkManager extends ChunkManager {
    private final World mirageWorld;
    private Map<ChunkPos,Chunk> chunks;
    public MirageChunkManager(World world) {
        this.chunks = new HashMap<>();
        this.mirageWorld = world;
    }

    @Override
    public BlockView getWorld() {
        return this.mirageWorld;
    }


    @Nullable
    @Override
    public Chunk getChunk(int x, int z, ChunkStatus leastStatus, boolean create) {
        return getChunk(x,z);
    }


    public Chunk getChunk(int x, int z) {
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
    public String getDebugString() {
        return null;
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return null;
    }

    public static class MirageChunk extends WorldChunk {

        public MirageChunk(World world, ChunkPos chunkPos) {
            super(world, chunkPos);
        }

        public BlockState getBlockState(BlockPos blockPos) {
            return Blocks.VOID_AIR.getDefaultState();
        }

        @Nullable
        @Override
        public BlockState setBlockState(BlockPos pos, BlockState state, boolean moved) {
            return null;
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return Fluids.EMPTY.getDefaultState();
        }

        @Override
        public int getLuminance(BlockPos pos) {
            return 0;
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos, CreationType creationType) {
            return null;
        }

        @Override
        public void addBlockEntity(BlockEntity blockEntity) {

        }

        @Override
        public void setNeedsSaving(boolean needsSaving) {}

        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean areSectionsEmptyBetween(int lowerHeight, int upperHeight) {
            return super.areSectionsEmptyBetween(lowerHeight, upperHeight);
        }

        @Override
        public ChunkLevelType getLevelType() {
            return ChunkLevelType.FULL;
        }
    }
}
