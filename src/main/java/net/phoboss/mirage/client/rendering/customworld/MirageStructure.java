package net.phoboss.mirage.client.rendering.customworld;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;

import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class MirageStructure extends StructureTemplate {

    private final List<StructureEntityInfo> mirageEntities = Lists.newArrayList();

    public MirageStructure() {
        super();
    }

    public void readNbt(NbtCompound nbt) {
        readNbt(Registries.BLOCK.getReadOnlyWrapper(), nbt);
    }
    @Override
    public void readNbt(RegistryEntryLookup<Block> blockLookup, NbtCompound nbt) {
        super.readNbt(blockLookup, nbt);
        NbtList entitiesNbt = nbt.getList("entities", 10);
        for(int j = 0; j < entitiesNbt.size(); ++j) {
            NbtCompound compoundtag = entitiesNbt.getCompound(j);
            NbtList listtag3 = compoundtag.getList("pos", 6);
            Vec3d vec3 = new Vec3d(listtag3.getDouble(0), listtag3.getDouble(1), listtag3.getDouble(2));
            NbtList listtag4 = compoundtag.getList("blockPos", 3);
            BlockPos blockpos = new BlockPos(listtag4.getInt(0), listtag4.getInt(1), listtag4.getInt(2));
            if (compoundtag.contains("nbt")) {
                NbtCompound compoundtag1 = compoundtag.getCompound("nbt");
                this.mirageEntities.add(new StructureEntityInfo(vec3, blockpos, compoundtag1));
            }
        }
    }

    @Override
    public boolean place(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, net.minecraft.util.math.random.Random random, int flags) {
        boolean ignoreEntities = placementData.shouldIgnoreEntities();
        placementData.setIgnoreEntities(true);
        boolean result =  super.place(world, pos, pivot, placementData, random, flags);
        placementData.setIgnoreEntities(ignoreEntities);
        if (!placementData.shouldIgnoreEntities()) {
            spawnEntities((World)world, pos,placementData.getMirror(),placementData.getRotation(),placementData.getPosition(),placementData.getBoundingBox(), placementData.shouldInitializeMobs());
        }

        if (world instanceof MirageWorld mw){
            mw.resetWorldForBlockEntities();
            mw.initBlockRenderLists();
        }

        return result;
    }


    public void spawnEntities(World world, BlockPos pos, BlockMirror blockMirror, BlockRotation blockRotation, BlockPos pivot, @Nullable BlockBox area, boolean initializeMobs) {
        this.mirageEntities.forEach((structureEntityInfo)->{
            BlockPos blockPosOff = transformAround(structureEntityInfo.blockPos, blockMirror, blockRotation, pivot).add(pos);
            if(area != null && !area.contains(blockPosOff)){
                return;
            }
            Vec3d blockPosOffVec3 = transformAround(structureEntityInfo.pos, blockMirror, blockRotation, pivot).add(new Vec3d(pos.getX(),pos.getY(),pos.getZ()));
            NbtCompound nbtCompound = structureEntityInfo.nbt.copy();
            Vec3d entityPosRotated = new Vec3d(blockPosOff.getX(),blockPosOff.getY(),blockPosOff.getZ());

            NbtList nbtList = new NbtList();
            nbtList.add(NbtDouble.of(entityPosRotated.x));
            nbtList.add(NbtDouble.of(entityPosRotated.y));
            nbtList.add(NbtDouble.of(entityPosRotated.z));
            nbtCompound.put("Pos", nbtList);
            nbtCompound.remove("UUID");

            EntityType.getEntityFromNbt(nbtCompound,world).ifPresent((entity) -> {
                float rotatedYaw = 0;

                BlockPos entityPos = blockPosOff;
                Vec3d entityPosVec3 = blockPosOffVec3;
                if(entity instanceof AbstractDecorationEntity painting){
                    rotatedYaw = entity.applyMirror(blockMirror);
                    rotatedYaw += entity.getYaw() - entity.applyRotation(blockRotation);

                    if(painting.getWidthPixels()>16){
                        if(blockMirror != BlockMirror.NONE){
                            BlockPos lookVector = new BlockPos(Direction.fromRotation(rotatedYaw).getVector());
                            BlockPos lookRight = lookVector.rotate(BlockRotation.CLOCKWISE_90);
                            entityPos = entityPos.add(lookRight);
                        }
                    }
                    entity.refreshPositionAndAngles(entityPos, rotatedYaw, entity.getPitch());
                }else{
                    rotatedYaw = entity.applyRotation(blockRotation);
                    rotatedYaw += entity.applyMirror(blockMirror) - entity.getYaw();
                    entity.refreshPositionAndAngles(entityPosVec3.getX(),entityPosVec3.getY(),entityPosVec3.getZ(), rotatedYaw, entity.getPitch());
                }

                entity.setYaw(rotatedYaw);
                entity.prevYaw = rotatedYaw;
                entity.setPitch(entity.getPitch());
                entity.prevPitch = entity.getPitch();
                entity.setHeadYaw(rotatedYaw);
                entity.setBodyYaw(rotatedYaw);
                if(entity instanceof MobEntity mobEntity){
                    mobEntity.prevHeadYaw =  mobEntity.headYaw;
                    mobEntity.prevBodyYaw =  mobEntity.bodyYaw;
                }

                entity.resetPosition();


                if (world instanceof MirageWorld mw) {
                    mw.spawnMirageEntityAndPassengers(entity);
                }
            });


        });

    }
}
