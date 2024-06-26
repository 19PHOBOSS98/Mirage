package net.phoboss.mirage.client.rendering.customworld;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Matrix4f;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.phoboss.decobeacons.blocks.decobeacon.DecoBeaconBlock;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xfacthd.framedblocks.api.block.FramedBlockEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class MirageWorld extends Level implements ServerLevelAccessor {
    public MirageWorld(Level level, MirageBlockEntity mirageBlockEntity, int mirageWorldIndex) {
        super((WritableLevelData) level.getLevelData(),
                level.dimension(),
                level.dimensionTypeRegistration(),
                level::getProfiler,
                level.isClientSide(),
                level.isDebug(),
                0);
        this.level = level;
        this.mirageBlockEntityTickers = new ObjectArrayList<>();
        this.animatedSprites = new ObjectArrayList<>();
        this.mirageStateNEntities = new ConcurrentHashMap<>();
        this.bERBlocksList = new ConcurrentHashMap<>();
        this.vertexBufferBlocksList = new ConcurrentHashMap<>();
        this.manualBlocksList = new ConcurrentHashMap<>();
        this.manualEntityRenderList = new ConcurrentHashMap<>();
        this.entities = new ArrayList<>();
        setChunkManager(new MirageChunkManager(this));

        this.mirageBufferStorage = new MirageBufferStorage();

        this.parentMirageBlockEntity = mirageBlockEntity;
        this.mirageWorldIndex = mirageWorldIndex;
    }


    public static Minecraft mc = Minecraft.getInstance();
    public static BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
    public static BlockEntityRenderDispatcher blockEntityRenderDispatcher = mc.getBlockEntityRenderDispatcher();
    public static EntityRenderDispatcher entityRenderDispatcher = mc.getEntityRenderDispatcher();

    protected ChunkSource chunkManager;

    public List<Integer> mirageFragmentCheckList = new ArrayList<>();

    public List<Integer> getMirageFragmentCheckList() {
        return this.mirageFragmentCheckList;
    }
    public int getMirageFragmentCheckList(int checkListIndex) {
        return this.mirageFragmentCheckList.get(checkListIndex);
    }
    public void setMirageFragmentCheckList(List<Integer>  mirageFragmentCheckList) {
        this.mirageFragmentCheckList = mirageFragmentCheckList;
    }
    public void addMirageFragmentCheckList(int fragmentIndex) {
        this.mirageFragmentCheckList.add(fragmentIndex);
    }

    public boolean fragmentsAreComplete(int totalFragments) {
        return this.mirageFragmentCheckList.size() == totalFragments;
    }

    public static class StateNEntity {
        public BlockState blockState;
        public BlockEntity blockEntity;
        public Entity entity;
        public StateNEntity(BlockState blockState,BlockEntity blockEntity) {
            this.blockState = blockState;
            this.blockEntity = blockEntity;
        }
        public StateNEntity(BlockState blockState) {
            this.blockState = blockState;
        }
        public StateNEntity(BlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }
        public StateNEntity(Entity entity) {
            this.entity = entity;
        }
    }

    public static class BlockWEntity {
        public BlockState blockState;
        public BlockEntity blockEntity;
        public BlockWEntity(BlockState blockState,BlockEntity blockEntity) {
            this.blockState = blockState;
            this.blockEntity = blockEntity;
        }
        public BlockWEntity(BlockState blockState) {
            this.blockState = blockState;
        }
        public BlockWEntity(BlockEntity blockEntity) {
            this.blockEntity = blockEntity;
        }
    }

    protected Level level;
    public ObjectArrayList<BlockTicker> mirageBlockEntityTickers;
    public ObjectArrayList<TextureAtlasSprite> animatedSprites;
    protected ConcurrentHashMap<Long,StateNEntity> mirageStateNEntities;
    protected ConcurrentHashMap<Long,StateNEntity> manualBlocksList;
    protected ConcurrentHashMap<Long,StateNEntity> manualEntityRenderList;
    protected ConcurrentHashMap<Long,StateNEntity> vertexBufferBlocksList;
    protected ConcurrentHashMap<Long,BlockWEntity> bERBlocksList;
    protected List<Entity> entities;
    private MirageBufferStorage mirageBufferStorage;

    public boolean isVertexBufferBlocksListPopulated(){
        return !this.vertexBufferBlocksList.isEmpty();
    }


    public boolean newlyRefreshedBuffers = true;
    public boolean overideRefreshBuffer = true;

    public boolean getOverideRefreshBuffer(){
        return this.overideRefreshBuffer;
    }
    public void setOverideRefreshBuffer(boolean overide){
        this.overideRefreshBuffer = overide;
    }

    public static void refreshVertexBuffersIfNeeded(BlockPos projectorPos, MirageWorld mirageWorld){
        boolean shadersEnabled = false;
        if(ModList.get().isLoaded("oculus")){
            shadersEnabled = IrisApi.getInstance().getConfig().areShadersEnabled();
        }
        if(shadersEnabled && mirageWorld.newlyRefreshedBuffers || mirageWorld.getOverideRefreshBuffer()){
            mirageWorld.initVertexBuffers(projectorPos);
            mirageWorld.newlyRefreshedBuffers = false;
            mirageWorld.setOverideRefreshBuffer(false);
        }
        if(!shadersEnabled){
            mirageWorld.newlyRefreshedBuffers = true;
        }
    }

    public void render(BlockPos projectorPos, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay){
        try {
            refreshVertexBuffersIfNeeded(projectorPos, this);

            for (Map.Entry<Long, StateNEntity> entry : this.manualEntityRenderList.entrySet()) {
                Entity fakeEntity = entry.getValue().entity;
                matrices.pushPose();
                Vec3 entityPos = fakeEntity.position().subtract(new Vec3(projectorPos.getX(), projectorPos.getY(), projectorPos.getZ()));
                matrices.translate(entityPos.x(), entityPos.y(), entityPos.z());
                try {
                    renderMirageEntity(fakeEntity, 0, matrices, vertexConsumers);
                } catch (Exception e) {
                    Mirage.LOGGER.error("Error in renderMirageEntity(...), removing entry from this.manualEntityRenderList", e);
                    this.manualEntityRenderList.remove(entry.getKey());
                }
                matrices.popPose();
            }

            for (Map.Entry<Long, StateNEntity> entry : this.manualBlocksList.entrySet()) {//need to render multi-model-layered translucent blocks (i.e. slime, honey, DecoBeacons etc) manually :(
                matrices.pushPose();
                BlockPos fakeBlockPos = BlockPos.of(entry.getKey());
                BlockPos relativePos = fakeBlockPos.subtract(projectorPos);
                matrices.translate(relativePos.getX(), relativePos.getY(), relativePos.getZ());
                try {
                    renderMirageBlock(entry.getValue().blockState, fakeBlockPos, this, matrices, vertexConsumers, true, getRandom());
                } catch (Exception e) {
                    Mirage.LOGGER.error("Error in renderMirageBlock(...), removing entry from this.manualBlocksList", e);
                    this.manualBlocksList.remove(entry.getKey());
                }
                matrices.popPose();
            }

            for (Map.Entry<Long, BlockWEntity> entry : this.bERBlocksList.entrySet()) {//animated blocks (enchanting table...)
                matrices.pushPose();
                BlockPos fakeBlockPos = BlockPos.of(entry.getKey());
                BlockPos relativePos = fakeBlockPos.subtract(projectorPos);
                matrices.translate(relativePos.getX(), relativePos.getY(), relativePos.getZ());
                try {
                    renderMirageBlockEntity(entry.getValue().blockEntity, tickDelta, matrices, vertexConsumers);
                } catch (Exception e) {
                    Mirage.LOGGER.error("Error in renderMirageBlockEntity(...), removing entry from this.bERBlocksList", e);
                    this.bERBlocksList.remove(entry.getKey());
                }
                matrices.popPose();
            }

            Matrix4f matrixView = RenderSystem.getModelViewMatrix().copy();
            matrixView.multiply(matrices.last().pose().copy());
            for (Map.Entry<RenderType, VertexBuffer> entry : this.mirageBufferStorage.mirageVertexBuffers.entrySet()) {
                RenderType renderLayer = entry.getKey();
                VertexBuffer vertexBuffer = entry.getValue();
                renderLayer.setupRenderState();
                vertexBuffer.bind();
                try {
                    vertexBuffer.drawWithShader(matrixView, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
                } catch (Exception e) {
                    Mirage.LOGGER.error("Error in vertexBuffer.drawWithShader(...), removing entry from this.mirageBufferStorage.mirageVertexBuffers", e);
                    this.mirageBufferStorage.mirageVertexBuffers.remove(entry.getKey());
                }
                renderLayer.clearRenderState();
            }

            markAnimatedSprite(this.animatedSprites);
        }catch(Exception e){
            Mirage.LOGGER.error("Error in MirageWorld.render(...)", e);
        }
    }

    public void resetMirageBufferStorage(){
        this.mirageBufferStorage.reset();
    }

    public void initVertexBuffers(BlockPos projectorPos) {
        resetMirageBufferStorage();
        PoseStack matrices = new PoseStack();
        MirageImmediate vertexConsumers = this.mirageBufferStorage.getMirageImmediate();

        this.setSearchOffset(projectorPos);

        this.vertexBufferBlocksList.forEach((fakeBlockPosKey, fakeStateNEntity)->{
            BlockPos fakeBlockPos = BlockPos.of(fakeBlockPosKey);
            BlockState fakeBlockState = fakeStateNEntity.blockState;
            BlockEntity fakeBlockEntity = fakeStateNEntity.blockEntity;
            Entity fakeEntity = fakeStateNEntity.entity;


            if (fakeEntity != null) {

                matrices.pushPose();
                Vec3 entityPos = fakeEntity.position().subtract(new Vec3(projectorPos.getX(),projectorPos.getY(),projectorPos.getZ()));
                matrices.translate(entityPos.x(),entityPos.y(),entityPos.z());
                renderMirageEntity(fakeEntity, 0, matrices, vertexConsumers);
                matrices.popPose();

            }

            matrices.pushPose();
            BlockPos relativePos = fakeBlockPos.subtract(projectorPos);
            matrices.translate(relativePos.getX(),relativePos.getY(),relativePos.getZ());

            if (fakeBlockEntity != null) {
                if(shouldRenderModelData(fakeBlockEntity)) {
                    renderMirageModelData(fakeBlockState, fakeBlockPos, this, true, getRandom(), fakeBlockEntity, matrices, vertexConsumers);
                    matrices.popPose();
                    return;
                }
            }


            if (fakeBlockState != null) {
                FluidState fakeFluidState = fakeBlockState.getFluidState();
                if(!fakeFluidState.isEmpty()) {
                    this.searchByRelativeOffset(true);
                    vertexConsumers.setActualPos(relativePos);
                    renderMirageFluid(fakeBlockState, fakeFluidState, relativePos, this, vertexConsumers);
                    vertexConsumers.setActualPos(new BlockPos(relativePos.getX()&15,relativePos.getY()&15,relativePos.getZ()&15));
                    this.searchByRelativeOffset(false);
                }else {
                    renderMirageBlock(fakeBlockState, fakeBlockPos, this, matrices, vertexConsumers, true, getRandom());
                }
            }
            matrices.popPose();
        });
        /*
        mirageStateNEntities gets populated from a different thread (MirageLoader).
        mirageStateNEntities populates vertexBufferBlocksList.
        vertexBufferBlocksList populates vertexConsumers with vertices.

        if process line has reached this point and vertexBufferBlocksList is not empty that means
        the MirageLoader thread has finished populating mirageStateNEntities
        which in turn finished populating vertexBufferBlocksList
        which in turn finished populating the vertexConsumers with vertices.

        This ensures that it is safe to clear out the mirageStateNEntities.
        Otherwise we would empty it out too soon and not populate the vertexBufferBlocksList.
         */
        if(isVertexBufferBlocksListPopulated() && !hasBlockEntities()){//Mirage BlockEntities also need this to check for neighboring block states
            clearMirageStateNEntities();
        }
        this.mirageBufferStorage.uploadBufferBuildersToVertexBuffers(vertexConsumers);

    }

    //WIP FramedBlocks compat
    public static boolean shouldRenderModelData(BlockEntity blockEntity){
        boolean result = false;
        if(ModList.get().isLoaded("framedblocks")){
            result = blockEntity instanceof FramedBlockEntity;
        }
        if(result){
            return true;
        }
        if(ModList.get().isLoaded("create")) {
            result = blockEntity instanceof CopycatBlockEntity;
        }
        return result;
    }
    //WIP FramedBlocks compat

    //WIP Embeddium/Rubidium compat
    public static final boolean SHOULD_MARK_ANIMATED_SPRITES = ModList.get().isLoaded("embeddium")||ModList.get().isLoaded("rubidium");

    public static void markAnimatedSprite(ObjectArrayList<TextureAtlasSprite> animatedSprites){
        if(!SHOULD_MARK_ANIMATED_SPRITES){
            return;
        }
        animatedSprites.forEach((sprite)->{
            SpriteUtil.markSpriteActive(sprite);
        });
    }
    public static TextureAtlasSprite getStillTexture(@Nullable Level level, @Nullable BlockPos pos, FluidState state) {
        if (state.getType() == Fluids.EMPTY) return null;
        ResourceLocation texture = state.getType().getAttributes().getStillTexture(level, pos);
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
    }
    public static TextureAtlasSprite getFlowingTexture(@Nullable Level level, @Nullable BlockPos pos, FluidState state) {
        if (state.getType() == Fluids.EMPTY) return null;
        ResourceLocation texture = state.getType().getAttributes().getFlowingTexture(level, pos);
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
    }
    public static void addFluidToAnimatedSprites(Level world, BlockPos blockPos, FluidState fluidState, ObjectArrayList<TextureAtlasSprite> animatedSprites){
        TextureAtlasSprite stillSprite = getStillTexture(world, blockPos, fluidState);
        if(stillSprite!=null && stillSprite.getAnimationTicker()!=null) {
            if(!animatedSprites.contains(stillSprite)) {
                animatedSprites.add(stillSprite);
            }
        }
        TextureAtlasSprite flowingSprite = getFlowingTexture(world, blockPos, fluidState);
        if(flowingSprite!=null && flowingSprite.getAnimationTicker()!=null) {
            if(!animatedSprites.contains(flowingSprite)) {
                animatedSprites.add(flowingSprite);
            }
        }
    }

    public void addToAnimatedSprites(BakedQuad quad){
        TextureAtlasSprite sprite = quad.getSprite();
        if(sprite != null){
            if(sprite.getAnimationTicker()!=null) {
                if(!this.animatedSprites.contains(sprite)) {
                    this.animatedSprites.add(sprite);
                }
            }
        }
    }
    public void addToAnimatedSprites(BlockState blockState,Random random){
        if(blockState == null){
            return;
        }
        BakedModel model = blockRenderManager.getBlockModel(blockState);
        List<BakedQuad> quads = model.getQuads(blockState, null, random);//null faces returns the whole list of quads
        quads.forEach((quad)->{
            addToAnimatedSprites(quad);
        });
        for(Direction direction:Direction.values()){
            List<BakedQuad> faceQuads = model.getQuads(blockState, direction, random);//some blocks (i.e. Smoker) only have quads in faceQuads
            faceQuads.forEach((quad)->{
                addToAnimatedSprites(quad);
            });
        }


    }
    //WIP Embeddium compat

    public static boolean isOnTranslucentRenderLayer(BlockState blockState){
        return ItemBlockRenderTypes.canRenderInLayer(blockState,RenderType.translucent());
    }

    public static boolean addToManualBlockRenderList(long blockPosKey, StateNEntity stateNEntity, ConcurrentHashMap<Long,StateNEntity> manualRenderBlocks){
        if(ModList.get().isLoaded("decobeacons")) {
            if (stateNEntity.blockState.getBlock() instanceof DecoBeaconBlock) {

                manualRenderBlocks.put(blockPosKey, stateNEntity);
                return true;
            }
        }

        return false;
    }

    public void clearMirageStateNEntities(){
        this.mirageStateNEntities.forEach((key,stateNEntity)->{
            if(stateNEntity.blockEntity instanceof MirageBlockEntity mbe){
                synchronized (Mirage.CLIENT_MIRAGE_PROJECTOR_PHONE_BOOK) {
                    Mirage.removeFromBlockEntityPhoneBook(mbe);
                }
                mbe.resetMirageWorlds();
            }
        });
        this.mirageStateNEntities.clear();
    }

    public void clearMirageWorld(){
        synchronized (this.mirageBufferStorage.mirageImmediate){
            this.mirageBufferStorage.resetMirageImmediateBuffers();
        }
        synchronized (this.mirageBufferStorage){
            this.mirageBufferStorage.clearMirageBuffers();
        }
        synchronized (this.mirageStateNEntities){
            clearMirageStateNEntities();
        }
        synchronized (this.bERBlocksList){
            this.bERBlocksList.clear();
        }
        synchronized (this.vertexBufferBlocksList){
            this.vertexBufferBlocksList.clear();
        }
        synchronized (this.manualBlocksList){
            this.manualBlocksList.clear();
        }
        synchronized (this.manualEntityRenderList){
            this.manualEntityRenderList.clear();
        }
        synchronized (this.mirageBlockEntityTickers){
            this.mirageBlockEntityTickers.clear();
        }
    }

    public void addToManualEntityRenderList(long blockPosKey,Entity entity){
        if(entity == null){
            return;
        }
        entities.add(entity);
        if(entity instanceof HangingEntity){
            this.vertexBufferBlocksList.put(blockPosKey, new StateNEntity(entity));

            return;
        }

        if(entity instanceof ArmorStand armorStandEntity){
            ItemStack mainHandItem = armorStandEntity.getItemBySlot(EquipmentSlot.MAINHAND);
            ItemStack offHandItem = armorStandEntity.getItemBySlot(EquipmentSlot.OFFHAND);
            boolean hasItem = !(mainHandItem.isEmpty() && offHandItem.isEmpty());//mainWorld blockEntities start floating if VertexBuffers render armor-stands that are equipped with something... best to just render them manually

            Iterator<ItemStack> equippedArmor = entity.getArmorSlots().iterator();
            boolean clothed = false;
            while(equippedArmor.hasNext()){
                ItemStack itemStack = equippedArmor.next();
                if(!(itemStack.getItem() instanceof AirItem)){
                    clothed = true;
                    break;
                }
            }

            if(hasItem||clothed){

                this.manualEntityRenderList.put(blockPosKey, new StateNEntity(entity));

                return;
            }

                this.vertexBufferBlocksList.put(blockPosKey, new StateNEntity(entity));

            return;
        }

            this.manualEntityRenderList.put(blockPosKey, new StateNEntity(entity));

    }

    public boolean hasBlockEntities = false;

    public boolean hasBlockEntities() {
        return hasBlockEntities;
    }

    public void setHasBlockEntities(boolean hasBlockEntities) {
        this.hasBlockEntities = hasBlockEntities;
    }

    public void initBlockRenderLists() {
        this.mirageStateNEntities.forEach((blockPosKey,stateNEntity)->{
            BlockState blockState = stateNEntity.blockState;
            BlockEntity blockEntity = stateNEntity.blockEntity;
            Entity entity = stateNEntity.entity;

            addToAnimatedSprites(blockState,getRandom());

            if(entity != null){
                addToManualEntityRenderList(blockPosKey,entity);
                stateNEntity = new StateNEntity(blockState,blockEntity);
            }
            if(blockEntity != null) {
                setHasBlockEntities(true);

                if (blockEntityRenderDispatcher.getRenderer(blockEntity)!=null) {

                        this.bERBlocksList.put(blockPosKey, new BlockWEntity(blockState, blockEntity));

                }
                if (isOnTranslucentRenderLayer(blockState)) {
                    if(addToManualBlockRenderList(blockPosKey,new StateNEntity(blockState,blockEntity), this.manualBlocksList)){//isDecoBeaconBlock
                        return;
                    }
                }

                    this.vertexBufferBlocksList.put(blockPosKey, stateNEntity);

                return;
            }

            if(blockState != null) {
                if(!blockState.getFluidState().isEmpty()){
                    addFluidToAnimatedSprites(this, BlockPos.of(blockPosKey), blockState.getFluidState(), this.animatedSprites);
                }

                if (isOnTranslucentRenderLayer(blockState)) {

                        this.manualBlocksList.put(blockPosKey, new StateNEntity(blockState));

                    return;
                }

                    this.vertexBufferBlocksList.put(blockPosKey, stateNEntity);

            }
        });

    }

    public static void renderMirageBlockEntity(BlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers){
        blockEntityRenderDispatcher.render(blockEntity,tickDelta,matrices,vertexConsumers);
    }
    public static void renderMirageEntity(Entity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers){
        entityRenderDispatcher.render(entity, 0, 0, 0, entity.getYRot(), tickDelta, matrices, vertexConsumers, entityRenderDispatcher.getPackedLightCoords(entity, tickDelta));
    }
    public static void renderMirageBlock(BlockState state, BlockPos referencePos, BlockAndTintGetter world, PoseStack matrices, MultiBufferSource vertexConsumerProvider, boolean cull, Random random){
        RenderType rl = ItemBlockRenderTypes.getRenderType(state,true);
        blockRenderManager.renderBatched(state,referencePos,world,matrices,
                vertexConsumerProvider.getBuffer(rl),cull,random);
    }
    public static void renderMirageFluid(BlockState state, FluidState fluidState, BlockPos referencePos, BlockAndTintGetter world, MultiBufferSource vertexConsumerProvider){
        RenderType rl = ItemBlockRenderTypes.getRenderLayer(fluidState);
        blockRenderManager.renderLiquid(referencePos, world, vertexConsumerProvider.getBuffer(rl), state, fluidState);
    }


    public static void renderMirageModelData(BlockState state, BlockPos referencePos, BlockAndTintGetter world, boolean cull, Random random, BlockEntity blockEntity, PoseStack matrices, MultiBufferSource vertexConsumerProvider){
        IModelData modelData = blockEntity.getModelData();

        for (RenderType renderLayer : RenderType.chunkBufferLayers()) {
            if (ItemBlockRenderTypes.canRenderInLayer(state, renderLayer)) {
                ForgeHooksClient.setRenderType(renderLayer);
                blockRenderManager.renderBatched(state,referencePos,world,matrices,vertexConsumerProvider.getBuffer(renderLayer),cull,random,modelData);
            }
        }

        ForgeHooksClient.setRenderType(null);
    }

    public void resetWorldForBlockEntities(){
        this.mirageStateNEntities.forEach((key,entry)->{
           if(entry.blockEntity != null){
               entry.blockEntity.setLevel(this);//framed block ModelData is set on `FramedBlockEntity.setWorld(...)`
           }
        });
    }

    public void setMirageBlockEntity(BlockPos pos,BlockEntity blockEntity) {
        long key = pos.asLong();

            if (this.mirageStateNEntities.containsKey(key)) {
                StateNEntity mirageStateNEntity = this.mirageStateNEntities.get(key);
                mirageStateNEntity.blockEntity = blockEntity;
            } else {

                this.mirageStateNEntities.put(key, new StateNEntity(blockEntity));

            }

    }

    @Override
    public boolean setBlockAndUpdate(BlockPos pos, BlockState state) {
        if(state.isAir()){
            return true;
        }
        long key = pos.asLong();

            if (this.mirageStateNEntities.containsKey(key)) {
                StateNEntity mirageStateNEntity = this.mirageStateNEntities.get(key);
                mirageStateNEntity.blockState = state;
            } else {

                this.mirageStateNEntities.put(key, new StateNEntity(state));

            }

        //setFluidState(pos,state);
        if (state.getBlock() instanceof EntityBlock bep) {
            setBlockEntity(bep.newBlockEntity(pos,state));
        }
        return true;
    }

    public boolean spawnEntity(BlockPos pos, Entity entity) {
        long key = pos.asLong();
        entity.level = this.level;

            if (this.mirageStateNEntities.containsKey(key)) {
                StateNEntity mirageStateNEntity = this.mirageStateNEntities.get(key);
                mirageStateNEntity.entity = entity;
            } else {

                this.mirageStateNEntities.put(key, new StateNEntity(entity));

            }

        return true;
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
        return setBlockAndUpdate(pos, state);
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        spawnEntity(entity.blockPosition(), entity);
        return true;
    }
    public void spawnMirageEntityAndPassengers(Entity entity) {
        entity.getSelfAndPassengers().forEach(this::addFreshEntity);
    }

    public MirageBlockEntity parentMirageBlockEntity;
    public int mirageWorldIndex;
    public MirageBlockEntity getParentMirageBlockEntity() {
        return parentMirageBlockEntity;
    }


    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        if(blockEntity instanceof MirageBlockEntity mbe){
            mbe.setRecursionLevel(getParentMirageBlockEntity().getRecursionLevel()+1);
        }
        BlockPos pos = blockEntity.getBlockPos();
        blockEntity.setLevel(this);//needs to be done AFTER setBlockState(...) here to properly initialize FramedBlockEntity ModelData
        setMirageBlockEntity(pos,blockEntity);
        setMirageBlockEntityTicker(pos,blockEntity);
    }


    public BlockPos searchOffset = new BlockPos(0,0,0);
    public boolean searchByRelativeOffset = false;

    public void searchByRelativeOffset(boolean searchByRelativeOffset) {
        this.searchByRelativeOffset = searchByRelativeOffset;
    }

    public void setSearchOffset(BlockPos projectorOffset) {
        this.searchOffset = projectorOffset;
    }

    public BlockPos getRelativeOffset(BlockPos blockPos){//For rendering Fluids
        if(searchByRelativeOffset){
            return blockPos.offset(searchOffset);
        }
        return blockPos;
    }
    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {

        long key = getRelativeOffset(pos).asLong();

            StateNEntity entry = this.mirageStateNEntities.get(key);
            if (entry == null) {
                return null;
            }
            if (entry.blockEntity == null) {
                return null;
            }
            return entry.blockEntity;

    }
    @Override
    public BlockState getBlockState(BlockPos pos) {
        long key = getRelativeOffset(pos).asLong();

            if (this.mirageStateNEntities.containsKey(key)) {

                BlockState blockState = this.mirageStateNEntities.get(key).blockState;

                if (blockState != null) {
                    return blockState;
                }

            }

        return Blocks.AIR.defaultBlockState();
    }
    @Override
    public FluidState getFluidState(BlockPos pos) {
        long key = getRelativeOffset(pos).asLong();

            if(this.mirageStateNEntities.containsKey(key)) {

                FluidState fluidState = this.mirageStateNEntities.get(key).blockState.getFluidState();
                if (fluidState != null) {
                    return fluidState;
                }
            }

        return Blocks.AIR.defaultBlockState().getFluidState();
    }
    @Override
    public List<Entity> getEntities(@Nullable Entity pEntity, AABB pArea) {
        return this.getEntities(pEntity, pArea, EntitySelector.NO_SPECTATORS);
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity pEntity, AABB pBoundingBox, Predicate<? super Entity> pPredicate) {
        return this.entities;
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return null;
    }

    @Override
    public ServerLevel getLevel() {
        if (this.level instanceof ServerLevel) {
            return (ServerLevel) this.level;
        }
        throw new IllegalStateException("Cannot use IServerWorld#getWorld in a client environment");
    }

    public class BlockTicker {
        public BlockPos blockPos;
        public BlockState blockState;
        public BlockEntity blockEntity;
        public BlockEntityTicker blockEntityTicker;

        public BlockTicker(BlockPos blockPos, BlockState blockState, BlockEntity blockEntity, BlockEntityTicker blockEntityTicker) {
            this.blockPos = blockPos;
            this.blockState = blockState;
            this.blockEntity = blockEntity;
            this.blockEntityTicker = blockEntityTicker;
        }
    }
    public void setMirageBlockEntityTicker(BlockPos pos,BlockEntity blockEntity) {
        if(!this.level.isClientSide()){//world doesn't save when adding entityTickers in server side
            return;
        }
        if(blockEntity instanceof BeaconBlockEntity){//Don't want to have players having a portable beacon buff :)
            return;
        }

        if(blockEntity instanceof GameMasterBlock){//prevent command blocks, structure blocks, jigsaw blocks from ticking
            return;
        }

        BlockState blockstate = blockEntity.getBlockState();
        BlockEntityTicker blockEntityTicker = blockstate.getTicker(this, blockEntity.getType());
        if (blockEntityTicker != null) {
            synchronized (this.mirageBlockEntityTickers) {
                this.mirageBlockEntityTickers.add(new BlockTicker(pos, blockstate, blockEntity, blockEntityTicker));
            }
        }
    }

    public void tickBlockEntities(){

            for (int i = 0; i < this.mirageBlockEntityTickers.size(); ++i) {
                try {

                    BlockTicker blockTicker = this.mirageBlockEntityTickers.get(i);
                    blockTicker.blockEntityTicker.tick(this, blockTicker.blockPos, blockTicker.blockState, blockTicker.blockEntity);

                } catch (Exception e) {
                    Mirage.LOGGER.error("Error in blockTicker, removing from mirageBlockEntityTickers list", e);
                    this.mirageBlockEntityTickers.remove(i);
                }
            }

    }



    public void tick(){
        tickBlockEntities();
    }

    public void setChunkManager(ChunkSource chunkManager) {
        this.chunkManager = chunkManager;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.level.getBiomeManager();
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
        return this.level.getBlockTint(pos, colorResolver);
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int pX, int pY, int pZ) {
        return level.getUncachedNoiseBiome(pX,pY,pZ);
    }


    @Override
    public long getGameTime() {
        return this.level.getGameTime();
    }

    @Override
    public ChunkSource getChunkSource() {
        return this.chunkManager;
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.level.registryAccess();
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    public List<? extends Player> players() {
        return this.level.players();
    }

    @Override
    public float getShade(Direction pDirection, boolean pShade) {
        return this.level.getShade(pDirection, pShade);
    }

    @Override
    public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {
        this.level.sendBlockUpdated(pPos,pOldState,pNewState,pFlags);
    }

    @Override
    public void playSound(@Nullable Player pPlayer, double pX, double pY, double pZ, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch) {
        this.level.playSound(pPlayer, pX, pY, pZ, pSound, pCategory, pVolume, pPitch);
    }

    @Override
    public String gatherChunkSourceStats() {
        return this.level.gatherChunkSourceStats();
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.level.getScoreboard();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return level.getBlockTicks();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return level.getFluidTicks();
    }

    @Override
    public int getHeight() {
        return 512;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return this.level.getCapability(cap, side);
    }

    @Override
    public void playSound(@Nullable Player pPlayer, Entity pEntity, SoundEvent pEvent, SoundSource pCategory, float pVolume, float pPitch) {

    }

    @Nullable
    @Override
    public Entity getEntity(int pId) {
        return null;
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(String pMapName) {
        return null;
    }

    @Override
    public void setMapData(String pMapId, MapItemSavedData pData) {

    }

    @Override
    public int getFreeMapId() {
        return 0;
    }

    @Override
    public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {

    }

    @Override
    public RecipeManager getRecipeManager() {
        return null;
    }

    @Override
    public void levelEvent(@Nullable Player pPlayer, int pType, BlockPos pPos, int pData) {

    }

    @Override
    public void gameEvent(@Nullable Entity pEntity, GameEvent pEvent, BlockPos pPos) {

    }
}
