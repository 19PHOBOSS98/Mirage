package net.phoboss.mirage.client.rendering.customworld;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.phoboss.mirage.Mirage;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.RecursiveAction;

public class MirageStructure extends StructureTemplate {

    private final List<StructureEntityInfo> mirageEntities = Lists.newArrayList();

    public MirageStructure() {
        super();
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
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

    public static CompoundTag getBuildingNbt(String structureName) throws Exception{
        File nbtFile = getBuildingNbtFile(structureName);
        try {
            return NbtIo.readCompressed(nbtFile);
        }
        catch (Exception e) {
            throw new Exception("Couldn't read nbt file: "+nbtFile,e);
        }
    }
    public static File getBuildingNbtFile(String structureName) throws Exception{
        File nbtFile = null;
        try {
            nbtFile = Mirage.SCHEMATICS_FOLDER.resolve(structureName+".nbt").toFile();
            if(nbtFile.exists()){
                return nbtFile;
            }
        }
        catch (Exception e) {
            throw new Exception("Couldn't open file: \n"+nbtFile.getName(),e);
        }
        throw new Exception("Couldn't find: "+nbtFile.getName()+"\nin schematics folder: "+Mirage.SCHEMATICS_FOLDER.getFileName());
    }

    public static CompoundTag getFragmentStructureNBTTemplate(ListTag size,int dataVersion){
        CompoundTag fragmentStructureNBT = new CompoundTag();
        fragmentStructureNBT.put("size", size);
        fragmentStructureNBT.putInt("DataVersion",dataVersion);

        return fragmentStructureNBT;
    }
    public static List<CompoundTag> splitStructureNBT(CompoundTag structureNBT){
        List<CompoundTag> splitStructureNBTList = new ArrayList<>();
        int dataVersion = structureNBT.getInt("DataVersion");
        ListTag size = (ListTag)structureNBT.get("size");

        ListTag entitiesNBT = structureNBT.getList("entities", 10);

        ListTag blocksNBT = structureNBT.getList("blocks", 10);


        ListTag paletteNBT = new ListTag();
        if (structureNBT.contains("palettes", 9)) {
            ListTag listTag2 = structureNBT.getList("palettes", 9);

            for(int i = 0; i < listTag2.size(); ++i) {
                paletteNBT.addAll(listTag2.getList(i));
            }
        } else {
            paletteNBT = structureNBT.getList("palette", 10);
        }

        CompoundTag fragmentStructureNBT = getFragmentStructureNBTTemplate(size,dataVersion);

        ListTag fragmentEntities = new ListTag();
        ListTag fragmentBlocks = new ListTag();
        ListTag fragmentPalette = new ListTag();

        fragmentStructureNBT.put("entities", fragmentEntities);
        fragmentStructureNBT.put("blocks", fragmentBlocks);
        fragmentStructureNBT.put("palette", fragmentPalette);

        HashMap<Integer, List<CompoundTag>> sortedPaletteBlockList = new HashMap<>();
        for(int i = 0; i < paletteNBT.size(); ++i) {
            sortedPaletteBlockList.put(i,new ArrayList<>());
        }
        for(int i = 0; i < blocksNBT.size(); ++i) {
            CompoundTag block = blocksNBT.getCompound(i);
            int blockState = block.getInt("state");
            block.putInt("state",0);
            sortedPaletteBlockList.get(blockState).add(block);
        }
        try {
            for (int paletteIdx = 0; paletteIdx < paletteNBT.size(); ++paletteIdx) {
                fragmentStructureNBT = getFragmentStructureNBTTemplate(size, dataVersion);
                fragmentEntities = new ListTag();
                fragmentBlocks = new ListTag();

                CompoundTag p = paletteNBT.getCompound(paletteIdx);
                if (p.getString("Name").equals("minecraft:air")) {
                    continue;
                }
                fragmentPalette = new ListTag();
                fragmentPalette.add(p);

                List<CompoundTag> blockList = sortedPaletteBlockList.get(paletteIdx);
                for (CompoundTag block : blockList) {

                    fragmentBlocks.add(block);
                    fragmentStructureNBT.put("blocks", fragmentBlocks);
                    fragmentStructureNBT.put("palette", fragmentPalette);
                    int byteSize = getSizeInBytes(fragmentStructureNBT, 0);
                    if (byteSize >= 262144) {
                        fragmentStructureNBT.put("entities", fragmentEntities);
                        splitStructureNBTList.add(fragmentStructureNBT.copy());

                        fragmentStructureNBT = getFragmentStructureNBTTemplate(size, dataVersion);
                        fragmentEntities = new ListTag();
                        fragmentBlocks = new ListTag();
                    }
                }
                splitStructureNBTList.add(fragmentStructureNBT.copy());
            }

            if(entitiesNBT.isEmpty()){
                return splitStructureNBTList;
            }

            for (int i = 0; i < entitiesNBT.size(); ++i) {
                CompoundTag entity = entitiesNBT.getCompound(i);
                fragmentEntities.add(entity);

                fragmentStructureNBT.put("entities", fragmentEntities);
                int byteSize = getSizeInBytes(fragmentStructureNBT, 0);
                if (byteSize >= 262144) {
                    fragmentStructureNBT.put("blocks", fragmentBlocks);
                    fragmentStructureNBT.put("palette", fragmentPalette);
                    splitStructureNBTList.add(fragmentStructureNBT.copy());

                    fragmentStructureNBT = getFragmentStructureNBTTemplate(size, dataVersion);
                    fragmentEntities = new ListTag();
                    fragmentBlocks = new ListTag();
                }
            }
            splitStructureNBTList.add(fragmentStructureNBT.copy());
        }catch (Exception e){
            Mirage.LOGGER.error("Error while fragmenting NBT",e);
        }

        return splitStructureNBTList;
    }

    public static int getSizeInBytes(Tag baseTag, int recursionCount) throws Exception {
        if(baseTag instanceof CompoundTag tag){
            if(recursionCount>=50){
                throw new Exception("CompoundTag too deep!");
            }
            int i = 48;

            for(String key : tag.getAllKeys()) {
                i += 28 + 2 * key.length();
                i += 36;
                i += getSizeInBytes(tag.get(key),recursionCount+1);
            }

            return i;
        }else
        if(baseTag instanceof ByteArrayTag tag){
            return 24 + 1 * tag.size();
        }else
        if(baseTag instanceof ByteTag tag){
            return 9;
        }else
        if(baseTag instanceof DoubleTag || baseTag instanceof LongTag){
            return 16;
        }else
        if(baseTag instanceof EndTag tag){
            return 8;
        }else
        if(baseTag instanceof FloatTag || baseTag instanceof IntTag){
            return 12;
        }else
        if(baseTag instanceof IntArrayTag tag){
            return 24 + 4 * tag.size();
        }else
        if(baseTag instanceof ListTag tag){
            if(recursionCount>=50){
                throw new Exception("ListTag too deep!");
            }
            int i = 37;
            i += 4 * tag.size();
            for(int j=0;j<tag.size();j++){
                i += getSizeInBytes(tag.get(j),recursionCount+1);
            }
            return i;
        }else
        if(baseTag instanceof LongArrayTag tag){
            return 24 + 8 * tag.size();
        }else
        if(baseTag instanceof StringTag tag){
            return 24 + 8 * tag.getAsString().length();
        }else
        if(baseTag instanceof ShortTag tag){
            return 10;
        }
        return 9999999;//sure why not...
    }

}
