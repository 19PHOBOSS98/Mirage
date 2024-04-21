package net.phoboss.mirage.blocks.mirageprojector;

import com.google.gson.Gson;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.ModBlockEntities;
import net.phoboss.mirage.client.rendering.customworld.MirageStructure;
import net.phoboss.mirage.client.rendering.customworld.MirageWorld;
import net.phoboss.mirage.client.rendering.customworld.StructureStates;
import net.phoboss.mirage.network.MirageNBTPacketHandler;
import net.phoboss.mirage.network.packets.MirageNBTPacketC2S;
import net.phoboss.mirage.utility.RedstoneStateChecker;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.RenderUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;


public class MirageBlockEntity extends BlockEntity implements GeoBlockEntity, IForgeBlockEntity {
    public MirageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIRAGE_BLOCK.get(), pos, state);
        setBookSettingsPOJO(new MirageProjectorBook());
    }

    public void setActiveLow(boolean activeLow) {
        getBookSettingsPOJO().setActiveLow(activeLow);
        setChanged();
    }
    public void setMove(Vec3i move) {
        getBookSettingsPOJO().setMove(move);
        setChanged();
    }
    public void setRotate(String rotate) {
        getBookSettingsPOJO().setRotate(Integer.parseInt(rotate));
        setChanged();
    }
    public void setMirror(String mirror) {
        getBookSettingsPOJO().setMirror(mirror);
        setChanged();
    }

    public boolean isActiveLow() {
        return getBookSettingsPOJO().isActiveLow();
    }
    public Vec3i getMove() {
        return getBookSettingsPOJO().getMoveVec3i();
    }
    public int getRotate() {
        return getBookSettingsPOJO().getRotate();
    }
    public String getMirror() {
        return getBookSettingsPOJO().getMirror();
    }
    public List<String> getFileNames() {
        return getBookSettingsPOJO().getFiles();
    }

    public void freeMirageWorldMemory(int mirageWorldCount){//MirageBufferBuilders need to be freed else we experience an OutOfMemoryError
        if(mirageWorldCount>10){
            System.gc();
        }
    }
    private ConcurrentHashMap<Integer, MirageWorld> mirageWorlds;

    public void resetMirageWorlds() {
        if(mirageWorlds != null){
            mirageWorlds.forEach((integer, mirageWorld) -> {
                mirageWorld.clearMirageWorld();
            });
            mirageWorlds.clear();
        }
    }
    public void resetMirageWorlds(int count){
        resetMirageWorlds();
        freeMirageWorldMemory(count);
    }

    //I would try to use more threads to load in multiple mirage-frames all at once but each thread would require a lot of memory... too much for the computer to provide all at once
    private Future mirageLoaderFuture;

    public void stopMirageLoader(){
        if(this.mirageLoaderFuture!=null){
            this.mirageLoaderFuture.cancel(true);
            try{
                this.mirageLoaderFuture.get(5, TimeUnit.SECONDS);
            }catch (Exception e){
                Mirage.LOGGER.error("Error on mirageLoader.interrupt()",e);
            }
        }
    }

    public void requestForMirageFilesFromServer() throws Exception{
        resetMirageWorlds(getFileNames().size());
        Runnable myThread = () ->
        {
            int mirageCount = getFileNames().size();
            Thread.currentThread().setName("myThread");
            for (int mirageWorldIndex = 0;mirageWorldIndex<mirageCount;mirageWorldIndex++) {
                //freeMirageWorldMemory(mirageCount);
                this.mirageWorlds.put(mirageWorldIndex,new MirageWorld(this.level));
                MirageNBTPacketHandler.sendToServer(new MirageNBTPacketC2S(getBlockPos(),getFileNames().get(mirageWorldIndex),mirageWorldIndex));
            }
        };

        Thread run = new Thread(myThread);

        // Starting the thread
        run.start();

    }

    public void uploadMirageFragment(int mirageWorldIndex, int fragmentIdx, int totalFragments, CompoundTag nbtMirageFragment){
        //stopMirageLoader();
        this.mirageLoaderFuture = Mirage.CLIENT_THREAD_POOL.submit(() -> {
            try{
                loadMirageFragment(mirageWorldIndex, fragmentIdx, totalFragments, nbtMirageFragment);
            }catch (Exception e){
                Mirage.LOGGER.error("Error on MirageLoader Thread: ",e);
            }
        });
    }

    public void loadMirageFragment(int mirageWorldIndex, int fragmentIdx, int totalFragments, CompoundTag nbtMirageFragment) throws Exception{
        if(this.mirageWorlds == null){
            return;
        }

        try {


            if(Thread.currentThread().isInterrupted() || getLevel().getBlockEntity(getBlockPos())==null){
                throw new InterruptedException();
            }
            //Thread.currentThread().sleep(1000);


            MirageWorld mirageWorld = this.mirageWorlds.get(mirageWorldIndex);
            synchronized (mirageWorld) {
                mirageWorld.addMirageFragmentCheckList(fragmentIdx);

                Vec3i actualMove = getMove();
                int actualRotate = getRotate();
                String actualMirror = getMirror();

                HashMap<Integer, Frame> frames = getBookSettingsPOJO().getFrames();
                Frame frame;
                if (frames.containsKey(mirageWorldIndex)) {
                    frame = frames.get(mirageWorldIndex);
                } else {
                    loadMirageWorldFragment(mirageWorld, nbtMirageFragment, actualMove, actualRotate, actualMirror);
                    if (mirageWorld.fragmentsAreComplete(totalFragments)) {
                        mirageWorld.setOverideRefreshBuffer(true);
                    }
                    return;
                }

                String mainMirror = getMirror();
                String subMirror = frame.getMirror();

                actualMove = actualMove.offset(frame.getMoveVec3i());
                actualRotate += frame.getRotate();


                if (mainMirror.equals(subMirror)) {
                    actualMirror = "NONE";
                } else if (mainMirror.equals("NONE")) {
                    actualMirror = subMirror;
                } else if (subMirror.equals("NONE")) {
                    actualMirror = mainMirror;
                } else {
                    actualMirror = "NONE";
                    actualRotate += 180;
                }

                actualRotate %= 360;
                loadMirageWorldFragment(mirageWorld, nbtMirageFragment, actualMove, actualRotate, actualMirror);
                if (mirageWorld.fragmentsAreComplete(totalFragments)) {
                    mirageWorld.setOverideRefreshBuffer(true);
                }
                freeMirageWorldMemory(totalFragments);
            }

        }catch (InterruptedException e) {
            resetMirageWorlds();
            freeMirageWorldMemory(totalFragments);
            throw new Exception("MirageLoader thread was interrupted..."+totalFragments,e);
        }
        catch (Exception e) {
            throw new Exception("Couldn't read nbt mirage:"+ getFileNames().get(mirageWorldIndex) +" fragment: "+ fragmentIdx,e);
        }
    }
    public void loadMirageWorldFragment(MirageWorld mirageWorld, CompoundTag nbt, Vec3i move, int rotate, String mirror) {
        if(!getLevel().isClientSide()) {
            return;
        }
        if(nbt == null){
            return;
        }
        BlockPos pos = getBlockPos().offset(move);
        MirageStructure fakeStructure = new MirageStructure();
        fakeStructure.load(nbt);

        StructurePlaceSettings StructurePlaceSettings = new StructurePlaceSettings();
        StructurePlaceSettings.setIgnoreEntities(false);

        StructurePlaceSettings.setRotation(StructureStates.ROTATION_STATES.get(rotate));
        StructurePlaceSettings.setMirror(StructureStates.MIRROR_STATES.get(mirror));

        //mirageWorld.clearMirageWorld();
        mirageWorld.setHasBlockEntities(false);
        fakeStructure.placeInWorld(mirageWorld,pos,pos,StructurePlaceSettings,mirageWorld.random, Block.UPDATE_ALL);

        //this.mirageWorld.initVertexBuffers(pos);      //the RenderDispatchers "camera" subojects are null on initialization causing errors


    }

    public void executeNewMirageLoaderTask(List<File> files){
        stopMirageLoader();
        this.mirageLoaderFuture = Mirage.CLIENT_THREAD_POOL.submit(() -> {
            try{
                loadMirage(files);
            }catch (Exception e){
                Mirage.LOGGER.error("Error on MirageLoader Thread: ",e);
            }
        });
    }

    public void loadMirage(List<File> files) throws Exception{
        if(this.mirageWorlds == null){
            return;
        }

        String fileName = "";
        int fileCount = 0;
        try {
            //List<String> files = getFileNames();
            fileCount = files.size();
            resetMirageWorlds(fileCount);

            HashMap<Integer,Frame> frames = getBookSettingsPOJO().getFrames();

            for(int i=0;i<fileCount;++i){
                if(Thread.currentThread().isInterrupted() || getLevel().getBlockEntity(getBlockPos())==null){
                    throw new InterruptedException();
                }
                Thread.currentThread().sleep(1000);
                this.mirageWorlds.put(i,new MirageWorld(this.level));

                MirageWorld mirageWorld = this.mirageWorlds.get(i);

                //fileName = files.get(i);
                //CompoundTag buildingNBT = getBuildingNbt(fileName);
                CompoundTag buildingNBT = getBuildingNbt(files.get(i));

                Vec3i actualMove = getMove();
                int actualRotate = getRotate();
                String actualMirror = getMirror();

                Frame frame;
                if(frames.containsKey(i)){
                    frame = frames.get(i);
                }else{
                    loadMirageWorld(mirageWorld,buildingNBT,actualMove,actualRotate,actualMirror);
                    continue;
                }

                String mainMirror = getMirror();
                String subMirror = frame.getMirror();

                actualMove = actualMove.offset(frame.getMoveVec3i());
                actualRotate += frame.getRotate();


                if(mainMirror.equals(subMirror)){
                    actualMirror = "NONE";
                }else if(mainMirror.equals("NONE")){
                    actualMirror = subMirror;
                }else if(subMirror.equals("NONE")){
                    actualMirror = mainMirror;
                }else {
                    actualMirror = "NONE";
                    actualRotate += 180;
                }

                actualRotate %= 360;
                loadMirageWorld(mirageWorld,buildingNBT,actualMove,actualRotate,actualMirror);
            }
            freeMirageWorldMemory(fileCount);
        }catch (InterruptedException e) {
            resetMirageWorlds();
            freeMirageWorldMemory(fileCount);
            throw new Exception("MirageLoader thread was interrupted..."+fileCount,e);
        }
        catch (Exception e) {
            throw new Exception("Couldn't read nbt file: "+fileName,e);
        }
    }

    public void loadMirageWorld(MirageWorld mirageWorld, CompoundTag nbt, Vec3i move, int rotate, String mirror) {
        if(!getLevel().isClientSide()) {
            return;
        }
        if(nbt == null){
            return;
        }
        BlockPos pos = getBlockPos().offset(move);
        MirageStructure fakeStructure = new MirageStructure();
        fakeStructure.load(nbt);

        StructurePlaceSettings StructurePlaceSettings = new StructurePlaceSettings();
        StructurePlaceSettings.setIgnoreEntities(false);

        StructurePlaceSettings.setRotation(StructureStates.ROTATION_STATES.get(rotate));
        StructurePlaceSettings.setMirror(StructureStates.MIRROR_STATES.get(mirror));

        mirageWorld.clearMirageWorld();
        mirageWorld.setHasBlockEntities(false);
        fakeStructure.placeInWorld(mirageWorld,pos,pos,StructurePlaceSettings,mirageWorld.random, Block.UPDATE_ALL);

        //this.mirageWorld.initVertexBuffers(pos);      //the RenderDispatchers "camera" subojects are null on initialization causing errors
        mirageWorld.setOverideRefreshBuffer(true);   //I couldn't find an Architectury API Event similar to Fabric's "ClientBlockEntityEvents.BLOCK_ENTITY_LOAD" event
                                                        //I could try to use @ExpectPlatform but I couldn't find anything similar for Forge either.
                                                        // So I just let the BER.render(...) method decide when's the best time to refresh the VertexBuffers :)

    }


    @Override
    public void setLevel(Level world) {
        super.setLevel(world);
        this.mirageWorlds = new ConcurrentHashMap<>();
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

    public static CompoundTag getBuildingNbt(File nbtFile) throws Exception{
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
    public void startMirage() throws Exception{
        validateNBTFiles(getFileNames());
        setChanged();//load schematic to mirageWorld in "readNBT(...)"
    }

    public void validateNBTFiles(List<String> fileNames) throws Exception{
        try{
            for(String fileName : fileNames){
                if(fileName.isEmpty()){
                    throw new Exception("Blank File Name");
                }
                getBuildingNbtFile(fileName);
            }
        }catch (Exception e){
            throw new Exception(e.getMessage(),e);
        }
    }


    public ConcurrentHashMap<Integer, MirageWorld> getMirageWorlds() {
        return this.mirageWorlds;
    }

    public MirageProjectorBook bookSettingsPOJO;

    public MirageProjectorBook getBookSettingsPOJO() {
        return this.bookSettingsPOJO;
    }

    public void setBookSettingsPOJO(MirageProjectorBook bookSettingsPOJO) {
        this.bookSettingsPOJO = bookSettingsPOJO;
    }

    public boolean newBookShouldReloadMirage(MirageProjectorBook newBookSettingsPOJO){
        return !this.bookSettingsPOJO.getRelevantSettings().equals(newBookSettingsPOJO.getRelevantSettings());
    }

    public String serializeBook() throws Exception{
        return new Gson().toJson(getBookSettingsPOJO());
    }

    public MirageProjectorBook deserializeBook(String bookString) throws Exception{
        return bookString.isEmpty() ? new MirageProjectorBook() : new Gson().fromJson(bookString, MirageProjectorBook.class);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        try {
            nbt.putString("bookJSON",serializeBook());
            nbt.putInt("mirageWorldIndex",getMirageWorldIndex());
        } catch (Exception e) {
            Mirage.LOGGER.error("Error on writeNBT: ",e);
        }
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        try{
            MirageProjectorBook newBook = deserializeBook(nbt.getString("bookJSON"));
            boolean shouldReloadMirage = newBookShouldReloadMirage(newBook);
            setBookSettingsPOJO(newBook);
            this.mirageWorldIndex = nbt.getInt("mirageWorldIndex");
            if(shouldReloadMirage && getLevel()!=null && getLevel().isClientSide()) {
                //executeNewMirageLoaderTask();
                requestForMirageFilesFromServer();
            }
        }catch (Exception e){
            Mirage.LOGGER.error("Error on readNBT: ",e);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        saveAdditional(nbt);
        return nbt;
    }

    @Override
    public void setChanged() {
        if(!(this.level instanceof MirageWorld)) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
        super.setChanged();
    }
    public boolean isReverse(){
        return getBookSettingsPOJO().isReverse();
    }
    public void setReverse(boolean reverse){
        getBookSettingsPOJO().setReverse(reverse);
        setChanged();
    }
    public boolean isTopPowered() {
        boolean active = false;
        try {
            active = getLevel().getSignal(getBlockPos().above(), Direction.UP)>0;
        }catch(Exception e){
            Mirage.LOGGER.error("Error on isTopPowered() method: ",e);
        }
        return active;
    }

    public boolean isPowered() {
        boolean active = false;
        try {
            active = getLevel().getSignal(getBlockPos().below(), Direction.DOWN)>0;
            active = isActiveLow() != active;
        }catch(Exception e){
            Mirage.LOGGER.error("Error on isPowered() method: ",e);
        }
        return active;
    }


    public boolean areSidesPowered() {
        boolean active = false;
        try {
            int currentSideRedstoneState =  getLevel().getSignal(getBlockPos().north(), Direction.NORTH)+
                                            getLevel().getSignal(getBlockPos().south(), Direction.SOUTH)+
                                            getLevel().getSignal(getBlockPos().east(), Direction.EAST)+
                                            getLevel().getSignal(getBlockPos().west(), Direction.WEST);
            active = currentSideRedstoneState>0;
        }catch(Exception e){
            Mirage.LOGGER.error("Error on areSidesPowered() method: ",e);
        }
        return active;
    }

    public boolean isPause(){
        return areSidesPowered();
    }
    public boolean isStepping(){
        return areSidesPowered() && !wereSidesPowered();
    }
    public boolean isRewind(){
        return isTopPowered();
    }
    public RedstoneStateChecker sidesRedstoneStateChecker = new RedstoneStateChecker();
    public boolean wereSidesPowered() {
        return sidesRedstoneStateChecker.getPreviousState();
    }
    public void savePreviousSidesPowerState(Boolean currentState) {
        sidesRedstoneStateChecker.setPreviousState(currentState);
    }
    public RedstoneStateChecker bottomRedstoneStateChecker = new RedstoneStateChecker();
    public boolean wasBottomPowered() {
        return bottomRedstoneStateChecker.getPreviousState();
    }
    public void savePreviousBottomPowerState(Boolean currentState) {
        bottomRedstoneStateChecker.setPreviousState(currentState);
    }
    public RedstoneStateChecker topRedstoneStateChecker = new RedstoneStateChecker();

    public boolean wasTopPowered() {
        return topRedstoneStateChecker.getPreviousState();
    }
    public void savePreviousTopPowerState(Boolean currentState) {
        topRedstoneStateChecker.setPreviousState(currentState);
    }
    public boolean isAutoPlay(){
        return getBookSettingsPOJO().isAutoPlay();
    }
    public void setAutoPlay(boolean autoPlay){
        getBookSettingsPOJO().setAutoPlay(autoPlay);
        setChanged();
    }
    public void setStep(int step){
        getBookSettingsPOJO().setStep(step);
        setChanged();
    }
    public int nextBookStep(int listSize){
        int nextStep = getBookSettingsPOJO().getStep();
        boolean reverse = getBookSettingsPOJO().isReverse();
        if(isRewind()){
            reverse = !reverse;
        }
        nextStep = reverse ? nextStep - 1 : nextStep + 1;
        if(getBookSettingsPOJO().isLoop()) {
            /*if (getBookSettingsPOJO().isReverse()) {
                nextStep = (nextStep + listSize) % listSize;
            } else {
                nextStep = (nextStep) % listSize;
            }*/
            nextStep = reverse ? nextStep + listSize : nextStep;
            nextStep = (nextStep) % listSize;
        }else{
            nextStep = Math.abs(Math.max(0,Math.min(nextStep,listSize-1)));
        }
        setStep(nextStep);
        return nextStep;
    }

    public int mirageWorldIndex = 0;

    public int getMirageWorldIndex() {
        return this.mirageWorldIndex;
    }

    public void setMirageWorldIndex(int newMirageWorldIndex) {
        this.mirageWorldIndex = newMirageWorldIndex;
    }
    public long previousTime = System.currentTimeMillis();

    public int nextMirageWorldIndex(int listSize){
        long currentTime = System.currentTimeMillis();
        int index = getMirageWorldIndex();
        if (currentTime - this.previousTime >= getBookSettingsPOJO().getDelay()*1000) {
            boolean reverse = getBookSettingsPOJO().isReverse();
            if(isRewind()){
                reverse = !reverse;
            }
            index = reverse ? index - 1 : index + 1;


            if(getBookSettingsPOJO().isLoop()) {
            /*if (getBookSettingsPOJO().isReverse()) {
                nextStep = (nextStep + listSize) % listSize;
            } else {
                nextStep = (nextStep) % listSize;
            }*/
                index = reverse ? index + listSize : index;
                index = (index) % listSize;
            }else{
                index = Math.abs(Math.max(0,Math.min(index,listSize-1)));
            }
            this.previousTime = currentTime;
        }
        return index;
    }

    public static void tick(Level world, BlockPos pos, BlockState state, MirageBlockEntity blockEntity) {
        try {
            ConcurrentHashMap<Integer, MirageWorld> mirageWorldList = blockEntity.getMirageWorlds();
            //to synchronize with server side
            //int mirageListLength = mirageWorldList.size();
            int mirageListLength = blockEntity.getFileNames().size();

            boolean isPowered = blockEntity.isPowered();
            boolean isTopPowered = blockEntity.isTopPowered();
            boolean areSidesPowered = blockEntity.areSidesPowered();
            MirageProjectorBook mirageProjectorBook = blockEntity.getBookSettingsPOJO();
            if(!isPowered) {
                blockEntity.setMirageWorldIndex(0);
                mirageProjectorBook.setStep(0);
                blockEntity.previousTime = System.currentTimeMillis();
            }else{

                if (mirageProjectorBook.isAutoPlay()) {
                    if (!blockEntity.isPause()) {
                        int nextIndex = blockEntity.nextMirageWorldIndex(mirageListLength);
                        blockEntity.setMirageWorldIndex(nextIndex);
                    }
                } else if (blockEntity.isStepping()) {
                    int newIndex = blockEntity.nextBookStep(mirageListLength);
                    if (newIndex != blockEntity.getMirageWorldIndex()) {
                        blockEntity.setMirageWorldIndex(newIndex);
                    }
                }
            }
            blockEntity.savePreviousTopPowerState(isTopPowered);
            blockEntity.savePreviousBottomPowerState(isPowered);
            blockEntity.savePreviousSidesPowerState(areSidesPowered);
            if (world.getGameTime() % 1200L == 0L && isPowered) {
                blockEntity.setChanged();
            }
            mirageWorldList.forEach((Integer, mirageWorld) -> {
                synchronized (mirageWorld) {
                    mirageWorld.tick();
                }
            });
            if (!world.isClientSide()) {// note to self only update state properties in server-side
                world.setBlock(pos, state.setValue(BlockStateProperties.LIT, blockEntity.isPowered()), Block.UPDATE_ALL);
            }
        }catch(Exception e){
            Mirage.LOGGER.error("Error on MirageBlockEntity.tick...",e);
        }

    }

    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController(this,"controller",0,this::predicate));
    }

    private PlayState predicate(AnimationState animationState) {
        MirageBlockEntity subject = (MirageBlockEntity) animationState.getAnimatable();
        AnimationController controller = animationState.getController();
        if(subject.isPowered()&&!subject.wasBottomPowered()){
            controller.setAnimation(RawAnimation.begin().then("ramp_up", Animation.LoopType.PLAY_ONCE));
            return PlayState.CONTINUE;
        }
        else if(!subject.isPowered() && subject.wasBottomPowered()){
            controller.setAnimation(RawAnimation.begin().then("ramp_down", Animation.LoopType.PLAY_ONCE));
            return PlayState.CONTINUE;
        }
        if(controller.getAnimationState() != AnimationController.State.STOPPED) {
            return PlayState.CONTINUE;
        }
        if (subject.isPowered()) {
            controller.setAnimation(RawAnimation.begin().then("projecting", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }
        controller.setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object blockEntity) {
        return RenderUtils.getCurrentTick();
    }


    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = this.getBlockPos();
        Vec3i offset = new Vec3i(512,512,512);
        return new AABB(pos.subtract(offset),pos.offset(offset));
    }


}






















