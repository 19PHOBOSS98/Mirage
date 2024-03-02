package net.phoboss.mirage.client.rendering.customworld;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class MirageStructure extends StructureTemplate {

    private final List<StructureEntityInfo> mirageEntities = Lists.newArrayList();

    public MirageStructure() {
        super();
    }

    public void load(CompoundTag nbt) {
        load(BuiltInRegistries.BLOCK.asLookup(), nbt);
    }
    @Override
    public void load(HolderGetter<Block> holderGetter, CompoundTag nbt) {
        super.load(holderGetter, nbt);
        ListTag entitiesNbt = nbt.getList("entities", 10);
        for(int j = 0; j < entitiesNbt.size(); ++j) {
            CompoundTag compoundtag = entitiesNbt.getCompound(j);
            ListTag listtag3 = compoundtag.getList("pos", 6);
            Vec3 vec3 = new Vec3(listtag3.getDouble(0), listtag3.getDouble(1), listtag3.getDouble(2));
            ListTag listtag4 = compoundtag.getList("blockPos", 3);
            BlockPos blockpos = new BlockPos(listtag4.getInt(0), listtag4.getInt(1), listtag4.getInt(2));
            if (compoundtag.contains("nbt")) {
                CompoundTag compoundtag1 = compoundtag.getCompound("nbt");
                this.mirageEntities.add(new StructureEntityInfo(vec3, blockpos, compoundtag1));
            }
        }
    }

    @Override
    public boolean placeInWorld(ServerLevelAccessor world, BlockPos pos, BlockPos pivot, StructurePlaceSettings placementData, RandomSource random, int flags) {
        boolean ignoreEntities = placementData.isIgnoreEntities();
        placementData.setIgnoreEntities(true);
        boolean result =  super.placeInWorld(world, pos, pivot, placementData, random, flags);
        placementData.setIgnoreEntities(ignoreEntities);
        if (!placementData.isIgnoreEntities()) {
            spawnEntities((Level)world, pos,placementData.getMirror(),placementData.getRotation(),placementData.getRotationPivot(),placementData.getBoundingBox(), placementData.shouldFinalizeEntities());
        }

        if (world instanceof MirageWorld mw){
            mw.resetWorldForBlockEntities();
            mw.initBlockRenderLists();
        }

        return result;
    }


    public void spawnEntities(Level world, BlockPos pos, Mirror blockMirror, Rotation blockRotation, BlockPos pivot, @Nullable BoundingBox area, boolean initializeMobs) {
        this.mirageEntities.forEach((structureEntityInfo)->{
            BlockPos blockPosOff = transform(structureEntityInfo.blockPos, blockMirror, blockRotation, pivot).offset(pos);
            if(area != null && !area.isInside(blockPosOff)){
                return;
            }
            Vec3 blockPosOffVec3 = transform(structureEntityInfo.pos, blockMirror, blockRotation, pivot).add(new Vec3(pos.getX(),pos.getY(),pos.getZ()));
            CompoundTag nbtCompound = structureEntityInfo.nbt.copy();

            ListTag nbtList = new ListTag();
            nbtList.add(DoubleTag.valueOf(blockPosOff.getX()));
            nbtList.add(DoubleTag.valueOf(blockPosOff.getY()));
            nbtList.add(DoubleTag.valueOf(blockPosOff.getZ()));
            nbtCompound.put("Pos", nbtList);
            nbtCompound.remove("UUID");

            EntityType.create(nbtCompound,world).ifPresent((entity) -> {
                float rotatedYaw = 0;

                BlockPos entityPos = blockPosOff;
                Vec3 entityPosVec3 = blockPosOffVec3;
                if(entity instanceof HangingEntity painting){
                    rotatedYaw = entity.mirror(blockMirror);
                    rotatedYaw += entity.getYRot() - entity.rotate(blockRotation);

                    if(painting.getWidth()>16){
                        if(blockMirror != Mirror.NONE){
                            BlockPos lookVector = new BlockPos(Direction.fromYRot(rotatedYaw).getNormal());
                            BlockPos lookRight = lookVector.rotate(Rotation.CLOCKWISE_90);
                            entityPos = entityPos.offset(lookRight);
                        }
                    }
                    entity.moveTo(entityPos, rotatedYaw, entity.getXRot());
                }else{
                    rotatedYaw = entity.rotate(blockRotation);
                    rotatedYaw += entity.mirror(blockMirror) - entity.getYRot();
                    entity.moveTo(entityPosVec3.x(),entityPosVec3.y(),entityPosVec3.z(), rotatedYaw, entity.getXRot());
                }

                entity.setYRot(rotatedYaw);
                entity.yRotO = rotatedYaw;
                entity.setXRot(entity.getXRot());
                entity.xRotO = entity.getXRot();
                entity.setYHeadRot(rotatedYaw);
                entity.setYBodyRot(rotatedYaw);
                if(entity instanceof Mob mobEntity){
                    mobEntity.yHeadRotO =  mobEntity.yHeadRot;
                    mobEntity.yBodyRotO =  mobEntity.yBodyRot;
                }

                entity.setOldPosAndRot();


                if (world instanceof MirageWorld mw) {
                    mw.spawnMirageEntityAndPassengers(entity);
                }
            });


        });
        /*this.mirageEntities.forEach((structureEntityInfo)->{
            BlockPos blockPosOff = transform(structureEntityInfo.blockPos, blockMirror, blockRotation, pivot).offset(pos);
            if(area != null && !area.isInside(blockPosOff)){
                return;
            }

            CompoundTag nbtCompound = structureEntityInfo.nbt.copy();
            Vec3 entityPosRotated = new Vec3(blockPosOff.getX(),blockPosOff.getY(),blockPosOff.getZ());

            ListTag nbtList = new ListTag();
            nbtList.add(DoubleTag.valueOf(entityPosRotated.x));
            nbtList.add(DoubleTag.valueOf(entityPosRotated.y));
            nbtList.add(DoubleTag.valueOf(entityPosRotated.z));
            nbtCompound.put("Pos", nbtList);
            nbtCompound.remove("UUID");

            //needs improvement
            *//*Entity entity = EntityType.loadEntityWithPassengers(nbtCompound, world, (entityx) -> {
                entityx.refreshPositionAndAngles(entityPosRotated.x, entityPosRotated.y, entityPosRotated.z, entityx.getYaw(), entityx.getPitch());
                return entityx;
            });
            if(entity == null){
                return;
            }
            float f = entity.applyMirror(blockMirror);
            f += entity.getYaw() - entity.applyRotation(blockRotation);

            BlockPos entityPos = blockPosOff;
            if(entity instanceof HangingEntity painting){
                if(painting.getWidth()>16){
                    if(blockMirror != BlockMirror.NONE){
                        BlockPos lookVector = new BlockPos(Direction.fromRotation(f).getVector());
                        BlockPos lookRight = lookVector.rotate(BlockRotation.CLOCKWISE_90);
                        entityPos = entityPos.add(lookRight);
                    }
                }
            }

            entity.refreshPositionAndAngles(entityPos, f, entity.getPitch());

            entity.setYaw(f);
            entity.prevYaw = f;
            entity.setPitch(entity.getPitch());
            entity.prevPitch = entity.getPitch();

            entity.resetPosition();
            if (world instanceof MirageWorld mw) {
                mw.spawnMirageEntityAndPassengers(entity);
            }*//*
            //needs improvement

            EntityType.create(nbtCompound,world).ifPresent((entity) -> {
                float rotatedYaw = 0;

                BlockPos entityPos = blockPosOff;
                if(entity instanceof HangingEntity painting){
                    rotatedYaw = entity.mirror(blockMirror);
                    rotatedYaw += entity.getYRot() - entity.rotate(blockRotation);

                    if(painting.getWidth()>16){
                        if(blockMirror != Mirror.NONE){
                            BlockPos lookVector = new BlockPos(Direction.fromYRot(rotatedYaw).getNormal());
                            BlockPos lookRight = lookVector.rotate(Rotation.CLOCKWISE_90);
                            entityPos = entityPos.offset(lookRight);
                        }
                    }
                    entity.moveTo(entityPos, rotatedYaw, entity.getXRot());
                }else{
                    rotatedYaw = entity.rotate(blockRotation);
                    rotatedYaw += entity.mirror(blockMirror) - entity.getYRot();
                    entity.moveTo(entityPos, rotatedYaw, entity.getXRot());
                }

                entity.setYRot(rotatedYaw);
                entity.yRotO = rotatedYaw;
                entity.setXRot(entity.getXRot());
                entity.xRotO = entity.getXRot();
                entity.setYHeadRot(rotatedYaw);
                entity.setYBodyRot(rotatedYaw);
                if(entity instanceof Mob mobEntity){
                    mobEntity.yHeadRotO =  mobEntity.yHeadRot;
                    mobEntity.yBodyRotO =  mobEntity.yBodyRot;
                }

                entity.setOldPosAndRot();


                if (world instanceof MirageWorld mw) {
                    mw.spawnMirageEntityAndPassengers(entity);
                }
            });


        });*/

    }
}
