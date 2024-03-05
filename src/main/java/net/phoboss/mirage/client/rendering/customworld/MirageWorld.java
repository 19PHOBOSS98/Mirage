package net.phoboss.mirage.client.rendering.customworld;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.block.*;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.phoboss.decobeacons.blocks.decobeacon.DecoBeaconBlock;
import net.phoboss.mirage.Mirage;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


public class MirageWorld extends World implements ServerWorldAccess {
    public MirageWorld(World world) {
        super((MutableWorldProperties) world.getLevelProperties(),
                world.getRegistryKey(),
                world.getRegistryManager(),
                world.getDimensionEntry(),
                world::getProfiler,
                world.isClient(),
                world.isDebugWorld(),
                0,
                10000000);
        this.world = world;
        this.mirageBlockEntityTickers = new ObjectArrayList<>();
        this.animatedSprites = new ObjectArrayList<>();
        this.mirageStateNEntities = new Long2ObjectOpenHashMap<>();
        this.bERBlocksList = new Long2ObjectOpenHashMap<>();
        this.vertexBufferBlocksList = new Long2ObjectOpenHashMap<>();
        this.manualBlocksList = new Long2ObjectOpenHashMap<>();
        this.manualEntityRenderList = new Long2ObjectOpenHashMap<>();
        this.entities = new ArrayList<>();
        setChunkManager(new MirageChunkManager(this));

        this.mirageBufferStorage = new MirageBufferStorage();
    }
    public static MinecraftClient mc = MinecraftClient.getInstance();
    public static BlockRenderManager blockRenderManager = mc.getBlockRenderManager();
    public static BlockEntityRenderDispatcher blockEntityRenderDispatcher = mc.getBlockEntityRenderDispatcher();
    public static EntityRenderDispatcher entityRenderDispatcher = mc.getEntityRenderDispatcher();

    protected ChunkManager chunkManager;

    public World getWorld() {
        return this.world;
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

    protected World world;
    public ObjectArrayList<BlockTicker> mirageBlockEntityTickers;
    public ObjectArrayList<Sprite> animatedSprites;
    protected Long2ObjectOpenHashMap<StateNEntity> mirageStateNEntities;
    protected Long2ObjectOpenHashMap<StateNEntity> manualBlocksList;
    protected Long2ObjectOpenHashMap<StateNEntity> manualEntityRenderList;
    protected Long2ObjectOpenHashMap<StateNEntity> vertexBufferBlocksList;
    protected Long2ObjectOpenHashMap<BlockWEntity> bERBlocksList;
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
        if(FabricLoader.getInstance().isModLoaded("iris")){
            shadersEnabled = IrisApi.getInstance().getConfig().areShadersEnabled();
        }
        if(shadersEnabled && mirageWorld.newlyRefreshedBuffers || mirageWorld.overideRefreshBuffer){
            mirageWorld.initVertexBuffers(projectorPos);
            mirageWorld.newlyRefreshedBuffers = false;
            mirageWorld.overideRefreshBuffer = false;
        }
        if(!shadersEnabled){
            mirageWorld.newlyRefreshedBuffers = true;
        }
    }

    public void render(BlockPos projectorPos,float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay){
        refreshVertexBuffersIfNeeded(projectorPos,this);

        for(Map.Entry<Long, StateNEntity> entry : this.manualEntityRenderList.entrySet()){
            Entity fakeEntity = entry.getValue().entity;
            matrices.push();
            Vec3d entityPos = fakeEntity.getPos().subtract(new Vec3d(projectorPos.getX(), projectorPos.getY(), projectorPos.getZ()));
            matrices.translate(entityPos.getX(), entityPos.getY(), entityPos.getZ());
            try{
                renderMirageEntity(fakeEntity, 0, matrices, vertexConsumers);
            }catch (Exception e){
                Mirage.LOGGER.error("Error in renderMirageEntity(...), removing entry from this.manualEntityRenderList",e);
                this.manualEntityRenderList.remove(entry.getKey());
            }
            matrices.pop();
        }

        for(Map.Entry<Long, StateNEntity> entry : this.manualBlocksList.entrySet()){//need to render multi-model-layered translucent blocks (i.e. slime, honey, DecoBeacons etc) manually :(
            matrices.push();
            BlockPos fakeBlockPos = BlockPos.fromLong(entry.getKey());
            BlockPos relativePos = fakeBlockPos.subtract(projectorPos);
            matrices.translate(relativePos.getX(),relativePos.getY(),relativePos.getZ());
            try{
                renderMirageBlock(entry.getValue().blockState, fakeBlockPos, this, matrices, vertexConsumers, true, getRandom());
            }catch (Exception e){
                Mirage.LOGGER.error("Error in renderMirageBlock(...), removing entry from this.manualBlocksList",e);
                this.manualBlocksList.remove(entry.getKey());
            }
            matrices.pop();
        }

        for (Map.Entry<Long,BlockWEntity> entry : this.bERBlocksList.entrySet()){//animated blocks (enchanting table...)
            matrices.push();
            BlockPos fakeBlockPos = BlockPos.fromLong(entry.getKey());
            BlockPos relativePos = fakeBlockPos.subtract(projectorPos);
            matrices.translate(relativePos.getX(),relativePos.getY(),relativePos.getZ());
            try {
                renderMirageBlockEntity(entry.getValue().blockEntity, tickDelta, matrices, vertexConsumers);
            }catch (Exception e){
                Mirage.LOGGER.error("Error in renderMirageBlockEntity(...), removing entry from this.bERBlocksList",e);
                this.bERBlocksList.remove(entry.getKey());
            }
            matrices.pop();
        }

        Matrix4f matrixView = new Matrix4f(RenderSystem.getModelViewMatrix());
        matrixView.mul(new Matrix4f(matrices.peek().getPositionMatrix()));
        for(Map.Entry<RenderLayer, VertexBuffer> entry : this.mirageBufferStorage.mirageVertexBuffers.entrySet()){
            RenderLayer renderLayer = entry.getKey();
            VertexBuffer vertexBuffer = entry.getValue();
            renderLayer.startDrawing();
            vertexBuffer.bind();
            try{
                vertexBuffer.draw(matrixView, RenderSystem.getProjectionMatrix(),RenderSystem.getShader());
            }catch (Exception e){
                Mirage.LOGGER.error("Error in vertexBuffer.draw(...), removing entry from this.mirageBufferStorage.mirageVertexBuffers",e);
                this.mirageBufferStorage.mirageVertexBuffers.remove(entry.getKey());
            }
            renderLayer.endDrawing();
        }

        markAnimatedSprite(this.animatedSprites);
    }

    public void resetMirageBufferStorage(){
        this.mirageBufferStorage.reset();
    }

    public void initVertexBuffers(BlockPos projectorPos) {
        resetMirageBufferStorage();
        MatrixStack matrices = new MatrixStack();
        MirageImmediate vertexConsumers = this.mirageBufferStorage.getMirageImmediate();

        this.setSearchOffset(projectorPos);

        this.vertexBufferBlocksList.forEach((fakeBlockPosKey, fakeStateNEntity)->{
            BlockPos fakeBlockPos = BlockPos.fromLong(fakeBlockPosKey);
            BlockState fakeBlockState = fakeStateNEntity.blockState;
            BlockEntity fakeBlockEntity = fakeStateNEntity.blockEntity;
            Entity fakeEntity = fakeStateNEntity.entity;


            if (fakeEntity != null) {

                matrices.push();
                Vec3d entityPos = fakeEntity.getPos().subtract(new Vec3d(projectorPos.getX(),projectorPos.getY(),projectorPos.getZ()));
                matrices.translate(entityPos.getX(),entityPos.getY(),entityPos.getZ());
                renderMirageEntity(fakeEntity, 0, matrices, vertexConsumers);
                matrices.pop();

            }

            matrices.push();
            BlockPos relativePos = fakeBlockPos.subtract(projectorPos);
            matrices.translate(relativePos.getX(),relativePos.getY(),relativePos.getZ());

            /*if (fakeBlockEntity != null) {
                if(shouldRenderModelData(fakeBlockEntity)) {
                    renderMirageModelData(fakeBlockState, fakeBlockPos, this, true, getRandom(), fakeBlockEntity, matrices, vertexConsumers);
                    matrices.pop();
                    return;
                }
            }*///remnant from architectury branch


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
            matrices.pop();
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

    /*public static boolean shouldRenderModelData(BlockEntity blockEntity){
        return false;
    }

    public static void renderMirageModelData(BlockState state, BlockPos referencePos, BlockRenderView world, boolean cull, Random random, BlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider){

    }*/

    //WIP Sodium compat
    public static final boolean SHOULD_MARK_ANIMATED_SPRITES = FabricLoader.getInstance().isModLoaded("sodium");
    public static void markAnimatedSprite(ObjectArrayList<Sprite> animatedSprites){
        if(!SHOULD_MARK_ANIMATED_SPRITES){
            return;
        }
        animatedSprites.forEach((sprite)->{
            SpriteUtil.markSpriteActive(sprite);
        });
    }
    public static Sprite getFlowingTexture(@Nullable BlockRenderView level, @Nullable BlockPos pos, FluidState state) {
        if (state.getFluid() == Fluids.EMPTY) return null;
        var handler = FluidRenderHandlerRegistry.INSTANCE.get(state.getFluid());
        if (handler == null) return null;
        var sprites = handler.getFluidSprites(level, pos, state);
        if (sprites == null) return null;
        return sprites[1];
    }
    public static Sprite getStillTexture(@Nullable BlockRenderView level, @Nullable BlockPos pos, FluidState state) {
        if (state.getFluid() == Fluids.EMPTY) return null;
        var handler = FluidRenderHandlerRegistry.INSTANCE.get(state.getFluid());
        if (handler == null) return null;
        var sprites = handler.getFluidSprites(level, pos, state);
        if (sprites == null) return null;
        return sprites[0];
    }

    public static boolean isSpriteAnimatable(Sprite sprite){
        return sprite.getContents().getDistinctFrameCount().count()>1;
    }

    public static void addFluidToAnimatedSprites(World world, BlockPos blockPos, FluidState fluidState, ObjectArrayList<Sprite> animatedSprites){
        Sprite stillSprite = getStillTexture(world, blockPos, fluidState);
        if(stillSprite!=null && isSpriteAnimatable(stillSprite)) {
            if(!animatedSprites.contains(stillSprite)) {
                animatedSprites.add(stillSprite);
            }
        }
        Sprite flowingSprite = getFlowingTexture(world, blockPos, fluidState);
        if(flowingSprite!=null && isSpriteAnimatable(stillSprite)) {
            if(!animatedSprites.contains(flowingSprite)) {
                animatedSprites.add(flowingSprite);
            }
        }
    }

    public void addToAnimatedSprites(BakedQuad quad){
        Sprite sprite = quad.getSprite();
        if(sprite != null){
            if(isSpriteAnimatable(sprite)) {
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
        BakedModel model = blockRenderManager.getModel(blockState);
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
    //WIP Sodium compat
    public static boolean isOnTranslucentRenderLayer(BlockState blockState){
        return RenderLayers.getBlockLayer(blockState) == RenderLayer.getTranslucent();
    }

    public static boolean addToManualBlockRenderList(long blockPosKey, StateNEntity stateNEntity, Long2ObjectOpenHashMap<StateNEntity> manualRenderBlocks){
        if(FabricLoader.getInstance().isModLoaded("decobeacons")) {
            if (stateNEntity.blockState.getBlock() instanceof DecoBeaconBlock) {
                manualRenderBlocks.put(blockPosKey, stateNEntity);
                return true;
            }
        }
        return false;
    }

    public void clearMirageStateNEntities(){
        this.mirageStateNEntities.clear();
    }

    public void clearMirageWorld(){
        synchronized (this.mirageBufferStorage.mirageImmediate){
            this.mirageBufferStorage.resetMirageImmediateBuffers();
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
        if(entity instanceof AbstractDecorationEntity){
            this.vertexBufferBlocksList.put(blockPosKey, new StateNEntity(entity));
            return;
        }

        if(entity instanceof ArmorStandEntity armorStandEntity){
            ItemStack mainHandItem = armorStandEntity.getEquippedStack(EquipmentSlot.MAINHAND);
            ItemStack offHandItem = armorStandEntity.getEquippedStack(EquipmentSlot.OFFHAND);
            boolean hasItem = !(mainHandItem.isEmpty() && offHandItem.isEmpty());//mainWorld blockEntities start floating if VertexBuffers render armor-stands that are equipped with something... best to just render them manually

            Iterator<ItemStack> equippedArmor = entity.getArmorItems().iterator();
            boolean clothed = false;
            while(equippedArmor.hasNext()){
                ItemStack itemStack = equippedArmor.next();
                if(!(itemStack.getItem() instanceof AirBlockItem)){
                    clothed = true;
                    break;
                }
            }

            if(hasItem||clothed){
                this.manualEntityRenderList.put(blockPosKey,new StateNEntity(entity));
                return;
            }
            this.vertexBufferBlocksList.put(blockPosKey, new StateNEntity(entity));
            return;
        }

        this.manualEntityRenderList.put(blockPosKey,new StateNEntity(entity));
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
                if (blockEntityRenderDispatcher.get(blockEntity)!=null) {
                    this.bERBlocksList.put(blockPosKey,new BlockWEntity(blockState,blockEntity));
                }
                if (isOnTranslucentRenderLayer(blockState)) {
                    if(addToManualBlockRenderList(blockPosKey,new StateNEntity(blockState,blockEntity), this.manualBlocksList)){//isDecoBeaconBlock
                        return;
                    }
                }
                this.vertexBufferBlocksList.put(blockPosKey,stateNEntity);
                return;
            }

            if(blockState != null) {
                if(!blockState.getFluidState().isEmpty()){
                    addFluidToAnimatedSprites(this, BlockPos.fromLong(blockPosKey), blockState.getFluidState(), this.animatedSprites);
                }

                if (isOnTranslucentRenderLayer(blockState)) {
                    this.manualBlocksList.put(blockPosKey, new StateNEntity(blockState));
                    return;
                }
            }

            this.vertexBufferBlocksList.put(blockPosKey,stateNEntity);
        });

    }

    public static void renderMirageBlockEntity(BlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers){
        blockEntityRenderDispatcher.render(blockEntity,tickDelta,matrices,vertexConsumers);
    }
    public static void renderMirageEntity(Entity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers){
        entityRenderDispatcher.render(entity, 0, 0, 0, entity.getYaw(), tickDelta, matrices, vertexConsumers, entityRenderDispatcher.getLight(entity, tickDelta));
    }
    public static void renderMirageBlock(BlockState state, BlockPos referencePos, BlockRenderView world, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, boolean cull, Random random){
        RenderLayer rl = RenderLayers.getEntityBlockLayer(state,true);
        blockRenderManager.renderBlock(state,referencePos,world,matrices,
                vertexConsumerProvider.getBuffer(rl),cull,random);
    }
    public static void renderMirageFluid(BlockState state, FluidState fluidState, BlockPos referencePos, BlockRenderView world, VertexConsumerProvider vertexConsumerProvider){
        RenderLayer rl = RenderLayers.getFluidLayer(fluidState);
        blockRenderManager.renderFluid(referencePos, world, vertexConsumerProvider.getBuffer(rl), state, fluidState);
    }





    public void resetWorldForBlockEntities(){
        this.mirageStateNEntities.forEach((key,entry)->{
           if(entry.blockEntity != null){
               entry.blockEntity.setWorld(this);//framed block ModelData is set on `FramedBlockEntity.setWorld(...)`
           }
        });
    }

    public void setMirageBlockEntity(BlockPos pos,BlockEntity blockEntity) {
        long key = pos.asLong();
        if (this.mirageStateNEntities.containsKey(key)) {
            StateNEntity mirageStateNEntity = this.mirageStateNEntities.get(key);
            mirageStateNEntity.blockEntity = blockEntity;
        }else{
            this.mirageStateNEntities.put(key,new StateNEntity(blockEntity));
        }
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state) {
        if(state.isAir()){
            return true;
        }
        long key = pos.asLong();
        if (this.mirageStateNEntities.containsKey(key)) {
            StateNEntity mirageStateNEntity = this.mirageStateNEntities.get(key);
            mirageStateNEntity.blockState = state;
        }else{
            this.mirageStateNEntities.put(key,new StateNEntity(state));
        }
        //setFluidState(pos,state);
        if (state.getBlock() instanceof BlockEntityProvider bep) {
            addBlockEntity(bep.createBlockEntity(pos,state));
        }
        return true;
    }

    public boolean spawnEntity(BlockPos pos, Entity entity) {
        long key = pos.asLong();
        //entity.world = this.world;
        if (this.mirageStateNEntities.containsKey(key)) {
            StateNEntity mirageStateNEntity = this.mirageStateNEntities.get(key);
            mirageStateNEntity.entity = entity;
        }else{
            this.mirageStateNEntities.put(key,new StateNEntity(entity));
        }
        return true;
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
        return setBlockState(pos, state);
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        spawnEntity(entity.getBlockPos(), entity);
        return true;
    }
    public void spawnMirageEntityAndPassengers(Entity entity) {
        entity.streamSelfAndPassengers().forEach(this::spawnEntity);
    }

    @Override
    public void addBlockEntity(BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getPos();
        blockEntity.setWorld(this);//needs to be done AFTER setBlockState(...) here to properly initialize FramedBlockEntity ModelData
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
            return blockPos.add(searchOffset);
        }
        return blockPos;
    }
    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {

        long key = getRelativeOffset(pos).asLong();
        StateNEntity entry = this.mirageStateNEntities.get(key);
        if(entry == null) {
            return null;
        }
        if(entry.blockEntity == null) {
            return null;
        }
        return entry.blockEntity;
    }
    @Override
    public BlockState getBlockState(BlockPos pos) {
        long key = getRelativeOffset(pos).asLong();
        if(this.mirageStateNEntities.containsKey(key)) {
            BlockState blockState = this.mirageStateNEntities.get(key).blockState;
            if ( blockState != null) {
                return blockState;
            }
        }
        return Blocks.AIR.getDefaultState();
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
        return Blocks.AIR.getDefaultState().getFluidState();
    }

    @Override
    public List<Entity> getOtherEntities(@Nullable Entity except, Box box) {
        return this.getOtherEntities(except, box, EntityPredicates.EXCEPT_SPECTATOR);
    }

    @Override
    public List<Entity> getOtherEntities(@Nullable Entity except, Box box, Predicate<? super Entity> predicate) {
        return this.entities;
    }

    @Override
    public void playSound(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {

    }

    @Override
    public ServerWorld toServerWorld() {
        if (this.world instanceof ServerWorld) {
            return (ServerWorld) this.world;
        }
        throw new IllegalStateException("Cannot use ServerWorldAccess#toServerWorld in a client environment");
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
        if(!this.world.isClient()){//world doesn't save when adding entityTickers in server side
            return;
        }
        if(blockEntity instanceof BeaconBlockEntity){//Don't want to have players having a portable beacon buff :)
            return;
        }

        if(blockEntity instanceof OperatorBlock){//prevent command blocks, structure blocks, jigsaw blocks from ticking
            return;
        }

        BlockState blockstate = blockEntity.getCachedState();
        BlockEntityTicker blockEntityTicker = blockstate.getBlockEntityTicker(this, blockEntity.getType());
        if (blockEntityTicker != null) {
            synchronized (this.mirageBlockEntityTickers) {
                this.mirageBlockEntityTickers.add(new BlockTicker(pos, blockstate, blockEntity, blockEntityTicker));
            }
        }
    }

    public void tickBlockEntities(){
        for(int i=0;i<this.mirageBlockEntityTickers.size();++i){
            try {
                BlockTicker blockTicker = this.mirageBlockEntityTickers.get(i);
                blockTicker.blockEntityTicker.tick(this, blockTicker.blockPos, blockTicker.blockState, blockTicker.blockEntity);
            }catch(Exception e){
                Mirage.LOGGER.error("Error in blockTicker, removing from mirageBlockEntityTickers list",e);
                this.mirageBlockEntityTickers.remove(i);
            }
        }
    }



    public void tick(){
        tickBlockEntities();
    }

    public void setChunkManager(ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
    }

    @Override
    public BiomeAccess getBiomeAccess() {
        return world.getBiomeAccess();
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return world.getColor(pos, colorResolver);
    }

    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return world.getGeneratorStoredBiome(biomeX,biomeY,biomeZ);
    }

    @Override
    public long getTime() {
        return this.world.getTime();
    }

    @Override
    public ChunkManager getChunkManager() {
        return this.chunkManager;
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return this.world.getRegistryManager();
    }

    @Override
    public FeatureSet getEnabledFeatures() {
        return this.world.getEnabledFeatures();
    }

    @Override
    public LightingProvider getLightingProvider() {
        return this.world.getLightingProvider();
    }

    @Override
    public List<? extends PlayerEntity> getPlayers() {
        return this.world.getPlayers();
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return this.world.getBrightness(direction, shaded);
    }

    @Override
    public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
        this.world.updateListeners(pos,oldState,newState,flags);
    }

    @Override
    public void playSound(@Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.world.playSound(except, x, y, z, sound, category, volume, pitch);
    }

    @Override
    public String asString() {
        return this.world.asString();
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.world.getScoreboard();
    }

    @Override
    public QueryableTickScheduler<Block> getBlockTickScheduler() {
        return world.getBlockTickScheduler();
    }

    @Override
    public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
        return world.getFluidTickScheduler();
    }

    @Override
    public int getTopY(Heightmap.Type heightmap, int x, int z) {
        return 512;
    }

    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
    }

    @Nullable
    @Override
    public Entity getEntityById(int id) {
        return null;
    }

    @Nullable
    @Override
    public MapState getMapState(String id) {
        return null;
    }

    @Override
    public void putMapState(String id, MapState state) {

    }

    @Override
    public int getNextMapId() {
        return 0;
    }

    @Override
    public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {

    }

    @Override
    public RecipeManager getRecipeManager() {
        return null;
    }

    @Override
    protected EntityLookup<Entity> getEntityLookup() {
        return null;
    }

    @Override
    public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {

    }

    @Override
    public void emitGameEvent(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter) {

    }

    @Override
    public void emitGameEvent(@Nullable Entity entity, GameEvent event, BlockPos pos) {

    }
}
