package net.phoboss.mirage.client.rendering.customworld;

import com.google.common.collect.Lists;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.*;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.phoboss.mirage.Mirage;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MirageStructure extends Structure {

    private final List<StructureEntityInfo> mirageEntities = Lists.newArrayList();

    public MirageStructure() {
        super();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        NbtList entitiesNbt = nbt.getList("entities", 10);
        for(int j = 0; j < entitiesNbt.size(); ++j) {
            NbtCompound NbtCompound = entitiesNbt.getCompound(j);
            NbtList NbtList3 = NbtCompound.getList("pos", 6);
            Vec3d vec3 = new Vec3d(NbtList3.getDouble(0), NbtList3.getDouble(1), NbtList3.getDouble(2));
            NbtList NbtList4 = NbtCompound.getList("blockPos", 3);
            BlockPos blockpos = new BlockPos(NbtList4.getInt(0), NbtList4.getInt(1), NbtList4.getInt(2));
            if (NbtCompound.contains("nbt")) {
                NbtCompound NbtCompound1 = NbtCompound.getCompound("nbt");
                this.mirageEntities.add(new StructureEntityInfo(vec3, blockpos, NbtCompound1));
            }
        }
    }

    @Override
    public boolean place(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, Random random, int flags) {
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

    public static NbtCompound getBuildingNbt(String structureName) throws Exception{
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

    public static NbtCompound getFragmentStructureNBTTemplate(NbtList size,int dataVersion){
        NbtCompound fragmentStructureNBT = new NbtCompound();
        fragmentStructureNBT.put("size", size);
        fragmentStructureNBT.putInt("DataVersion",dataVersion);

        return fragmentStructureNBT;
    }
    public static List<NbtCompound> splitStructureNBT(NbtCompound structureNBT){
        List<NbtCompound> splitStructureNBTList = new ArrayList<>();
        int dataVersion = structureNBT.getInt("DataVersion");
        NbtList size = (NbtList)structureNBT.get("size");

        NbtList entitiesNBT = structureNBT.getList("entities", 10);

        NbtList blocksNBT = structureNBT.getList("blocks", 10);


        NbtList paletteNBT = new NbtList();
        if (structureNBT.contains("palettes", 9)) {
            NbtList NbtList2 = structureNBT.getList("palettes", 9);

            for(int i = 0; i < NbtList2.size(); ++i) {
                paletteNBT.addAll(NbtList2.getList(i));
            }
        } else {
            paletteNBT = structureNBT.getList("palette", 10);
        }

        NbtCompound fragmentStructureNBT = getFragmentStructureNBTTemplate(size,dataVersion);

        NbtList fragmentEntities = new NbtList();
        NbtList fragmentBlocks = new NbtList();
        NbtList fragmentPalette = new NbtList();

        fragmentStructureNBT.put("entities", fragmentEntities);
        fragmentStructureNBT.put("blocks", fragmentBlocks);
        fragmentStructureNBT.put("palette", fragmentPalette);

        HashMap<Integer, List<NbtCompound>> sortedPaletteBlockList = new HashMap<>();
        for(int i = 0; i < paletteNBT.size(); ++i) {
            sortedPaletteBlockList.put(i,new ArrayList<>());
        }
        for(int i = 0; i < blocksNBT.size(); ++i) {
            NbtCompound block = blocksNBT.getCompound(i);
            int blockState = block.getInt("state");
            block.putInt("state",0);
            sortedPaletteBlockList.get(blockState).add(block);
        }
        try {
            for (int paletteIdx = 0; paletteIdx < paletteNBT.size(); ++paletteIdx) {
                fragmentStructureNBT = getFragmentStructureNBTTemplate(size, dataVersion);
                fragmentEntities = new NbtList();
                fragmentBlocks = new NbtList();

                NbtCompound p = paletteNBT.getCompound(paletteIdx);
                if (p.getString("Name").equals("minecraft:air")) {
                    continue;
                }
                fragmentPalette = new NbtList();
                fragmentPalette.add(p);

                List<NbtCompound> blockList = sortedPaletteBlockList.get(paletteIdx);
                for (NbtCompound block : blockList) {

                    fragmentBlocks.add(block);
                    fragmentStructureNBT.put("blocks", fragmentBlocks);
                    fragmentStructureNBT.put("palette", fragmentPalette);
                    int byteSize = getSizeInBytes(fragmentStructureNBT, 0);
                    if (byteSize >= 262144) {
                        fragmentStructureNBT.put("entities", fragmentEntities);
                        splitStructureNBTList.add(fragmentStructureNBT.copy());

                        fragmentStructureNBT = getFragmentStructureNBTTemplate(size, dataVersion);
                        fragmentEntities = new NbtList();
                        fragmentBlocks = new NbtList();
                    }
                }
                splitStructureNBTList.add(fragmentStructureNBT.copy());
            }

            if(entitiesNBT.isEmpty()){
                return splitStructureNBTList;
            }

            for (int i = 0; i < entitiesNBT.size(); ++i) {
                NbtCompound entity = entitiesNBT.getCompound(i);
                fragmentEntities.add(entity);

                fragmentStructureNBT.put("entities", fragmentEntities);
                int byteSize = getSizeInBytes(fragmentStructureNBT, 0);
                if (byteSize >= 262144) {
                    fragmentStructureNBT.put("blocks", fragmentBlocks);
                    fragmentStructureNBT.put("palette", fragmentPalette);
                    splitStructureNBTList.add(fragmentStructureNBT.copy());

                    fragmentStructureNBT = getFragmentStructureNBTTemplate(size, dataVersion);
                    fragmentEntities = new NbtList();
                    fragmentBlocks = new NbtList();
                }
            }
            splitStructureNBTList.add(fragmentStructureNBT.copy());
        }catch (Exception e){
            Mirage.LOGGER.error("Error while fragmenting NBT",e);
        }

        return splitStructureNBTList;
    }

    public static int getSizeInBytes(NbtElement baseTag, int recursionCount) throws Exception {
        if(baseTag instanceof NbtCompound tag){
            if(recursionCount>=50){
                throw new Exception("NbtCompound too deep!");
            }
            int i = 48;

            for(String key : tag.getKeys()) {
                i += 28 + 2 * key.length();
                i += 36;
                i += getSizeInBytes(tag.get(key),recursionCount+1);
            }

            return i;
        }else
        if(baseTag instanceof NbtByteArray tag){
            return 24 + 1 * tag.size();
        }else
        if(baseTag instanceof NbtByte tag){
            return 9;
        }else
        if(baseTag instanceof NbtDouble || baseTag instanceof NbtLong){
            return 16;
        }else
        if(baseTag instanceof NbtEnd tag){
            return 8;
        }else
        if(baseTag instanceof NbtFloat || baseTag instanceof NbtInt){
            return 12;
        }else
        if(baseTag instanceof NbtIntArray tag){
            return 24 + 4 * tag.size();
        }else
        if(baseTag instanceof NbtList tag){
            if(recursionCount>=50){
                throw new Exception("NbtList too deep!");
            }
            int i = 37;
            i += 4 * tag.size();
            for(int j=0;j<tag.size();j++){
                i += getSizeInBytes(tag.get(j),recursionCount+1);
            }
            return i;
        }else
        if(baseTag instanceof NbtLongArray tag){
            return 24 + 8 * tag.size();
        }else
        if(baseTag instanceof NbtString tag){
            return 24 + 8 * tag.toString().length();
        }else
        if(baseTag instanceof NbtShort tag){
            return 10;
        }
        return 9999999;//sure why not...
    }
}
