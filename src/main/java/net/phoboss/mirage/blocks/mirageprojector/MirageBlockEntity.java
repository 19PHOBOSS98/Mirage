package net.phoboss.mirage.blocks.mirageprojector;

import com.google.gson.Gson;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.ModBlockEntities;
import net.phoboss.mirage.client.rendering.customworld.MirageStructure;
import net.phoboss.mirage.client.rendering.customworld.MirageWorld;
import net.phoboss.mirage.client.rendering.customworld.StructureStates;
import net.phoboss.mirage.utility.RedstoneStateChecker;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class MirageBlockEntity extends BlockEntity implements IAnimatable {
    public MirageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIRAGE_BLOCK, pos, state);
        setBookSettingsPOJO(new MirageProjectorBook());
    }

    public void setActiveLow(boolean activeLow) {
        getBookSettingsPOJO().setActiveLow(activeLow);
        markDirty();
    }
    public void setMove(Vec3i move) {
        getBookSettingsPOJO().setMove(move);
        markDirty();
    }
    public void setRotate(String rotate) {
        getBookSettingsPOJO().setRotate(Integer.parseInt(rotate));
        markDirty();
    }
    public void setMirror(String mirror) {
        getBookSettingsPOJO().setMirror(mirror);
        markDirty();
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
    private ConcurrentHashMap<Integer,MirageWorld> mirageWorlds;

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
        /*if(mirageWorlds != null) {
            for (int i = 0; i < count; ++i) {
                mirageWorlds.put(i,new MirageWorld(world));
                Thread.currentThread().sleep(1000);
            }
        }*/
    }

    //I would try to use more threads to load in multiple mirage-frames all at once but each thread would require a lot of memory... too much for the computer to provide all at once
    private MirageLoader mirageLoader = new MirageLoader();

    public void stopMirageLoader(){
        if(this.mirageLoader.isAlive()){
            this.mirageLoader.interrupt();
        }
        try{
            this.mirageLoader.join(5000);
        }catch (Exception e){
            Mirage.LOGGER.error("Error on mirageLoader.interrupt()",e);
        }

    }

    public void executeNewMirageLoaderTask(){
        stopMirageLoader();
        this.mirageLoader = new MirageLoader();
        this.mirageLoader.start();
    }

    public class MirageLoader extends Thread{
        public MirageLoader() {
            setName("MirageLoader");
        }

        @Override
        public void run() {
            try{
                loadMirage();
            }catch (Exception e){
                Mirage.LOGGER.error("Error on MirageLoader Thread: ",e);
            }
        }
    }

    public void loadMirage() throws Exception{
        if(this.mirageWorlds == null){
            return;
        }
        String fileName = "";
        int fileCount = 0;
        try {
            List<String> files = getFileNames();
            fileCount = files.size();
            resetMirageWorlds(fileCount);

            HashMap<Integer,Frame> frames = getBookSettingsPOJO().getFrames();

            for(int i=0;i<fileCount;++i){
                if(Thread.currentThread().isInterrupted()){
                    throw new InterruptedException();
                }
                Thread.currentThread().sleep(1000);
                this.mirageWorlds.put(i,new MirageWorld(this.world));

                MirageWorld mirageWorld = this.mirageWorlds.get(i);

                fileName = files.get(i);
                NbtCompound buildingNBT = getBuildingNbt(fileName);

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

                actualMove = actualMove.add(frame.getMoveVec3i());
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
    public void loadMirageWorld(MirageWorld mirageWorld, NbtCompound nbt, Vec3i move, int rotate, String mirror) {
        if(!world.isClient()) {
            return;
        }
        if(nbt == null){
            return;
        }
        BlockPos pos = getPos().add(move);
        MirageStructure fakeStructure = new MirageStructure();
        fakeStructure.readNbt(nbt);

        StructurePlacementData structurePlacementData = new StructurePlacementData();
        structurePlacementData.setIgnoreEntities(false);

        structurePlacementData.setRotation(StructureStates.ROTATION_STATES.get(rotate));
        structurePlacementData.setMirror(StructureStates.MIRROR_STATES.get(mirror));

        mirageWorld.clearMirageWorld();
        mirageWorld.setHasBlockEntities(false);
        fakeStructure.place(mirageWorld,pos,pos,structurePlacementData,mirageWorld.random,Block.NOTIFY_ALL);

        //this.mirageWorld.initVertexBuffers(pos);      //the RenderDispatchers "camera" subojects are null on initialization causing errors
        mirageWorld.setOverideRefreshBuffer(true);   //I couldn't find an Architectury API Event similar to Fabric's "ClientBlockEntityEvents.BLOCK_ENTITY_LOAD" event
                                                        //I could try to use @ExpectPlatform but I couldn't find anything similar for Forge either.
                                                        // So I just let the BER.render(...) method decide when's the best time to refresh the VertexBuffers :)

    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.mirageWorlds = new ConcurrentHashMap<>();
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
    public void startMirage() throws Exception{
        validateNBTFiles(getFileNames());
        markDirty();//load schematic to mirageWorld in "readNBT(...)"
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


    public ConcurrentHashMap<Integer,MirageWorld> getMirageWorlds() {
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
    protected void writeNbt(NbtCompound nbt) {
        try {
            nbt.putString("bookJSON",serializeBook());
            nbt.putInt("mirageWorldIndex",getMirageWorldIndex());
        } catch (Exception e) {
            Mirage.LOGGER.error("Error on writeNBT: ",e);
        }
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        try{
            MirageProjectorBook newBook = deserializeBook(nbt.getString("bookJSON"));
            boolean shouldReloadMirage = newBookShouldReloadMirage(newBook);
            setBookSettingsPOJO(newBook);
            this.mirageWorldIndex = nbt.getInt("mirageWorldIndex");
            if(shouldReloadMirage && getWorld()!=null) {
                executeNewMirageLoaderTask();
            }
        }catch (Exception e){
            Mirage.LOGGER.error("Error on readNBT: ",e);
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = super.toInitialChunkDataNbt();
        writeNbt(nbt);
        return nbt;
    }

    @Override
    public void markDirty() {
        if(!(getWorld() instanceof MirageWorld)) {
            getWorld().updateListeners(getPos(), getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
        super.markDirty();
    }
    public boolean isReverse(){
        return getBookSettingsPOJO().isReverse();
    }
    public void setReverse(boolean reverse){
        getBookSettingsPOJO().setReverse(reverse);
        markDirty();
    }
    public boolean isTopPowered() {
        boolean active = false;
        try {
            active = getWorld().getEmittedRedstonePower(getPos().up(), Direction.UP)>0;
        }catch(Exception e){
            Mirage.LOGGER.error("Error on isTopPowered() method: ",e);
        }
        return active;
    }

    public boolean isPowered() {
        boolean active = false;
        try {
            active = getWorld().getEmittedRedstonePower(getPos().down(), Direction.DOWN)>0;
            active = isActiveLow() != active;
        }catch(Exception e){
            Mirage.LOGGER.error("Error on isPowered() method: ",e);
        }
        return active;
    }


    public boolean areSidesPowered() {
        boolean active = false;
        try {
            int currentSideRedstoneState =  getWorld().getEmittedRedstonePower(getPos().north(), Direction.NORTH)+
                                            getWorld().getEmittedRedstonePower(getPos().south(), Direction.SOUTH)+
                                            getWorld().getEmittedRedstonePower(getPos().east(), Direction.EAST)+
                                            getWorld().getEmittedRedstonePower(getPos().west(), Direction.WEST);
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
        markDirty();
    }
    public void setStep(int step){
        getBookSettingsPOJO().setStep(step);
        markDirty();
    }
    public void nextBookStep(int listSize){
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
            nextStep = Math.abs(Math.max(0,Math.min(nextStep,getMirageWorlds().size()-1)));
        }
        setStep(nextStep);
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
                index = Math.abs(Math.max(0,Math.min(index,getMirageWorlds().size()-1)));
            }
            this.previousTime = currentTime;
        }
        return index;
    }


    public static void tick(World world, BlockPos pos, BlockState state, MirageBlockEntity blockEntity) {
        try {
            ConcurrentHashMap<Integer, MirageWorld> mirageWorldList = blockEntity.getMirageWorlds();
            //to synchronize with server side
            //int mirageListLength = mirageWorldList.size();
            int mirageListLength = blockEntity.getFileNames().size();

            boolean isPowered = blockEntity.isPowered();
            boolean isTopPowered = blockEntity.isTopPowered();
            boolean areSidesPowered = blockEntity.areSidesPowered();

            if(isPowered) {
                MirageProjectorBook mirageProjectorBook = blockEntity.getBookSettingsPOJO();
                if (mirageProjectorBook.isAutoPlay()) {
                    if (!blockEntity.isPause()) {
                        int nextIndex = blockEntity.nextMirageWorldIndex(mirageListLength);
                        blockEntity.setMirageWorldIndex(nextIndex);

                    }
                } else {
                    if (blockEntity.isStepping()) {
                        blockEntity.nextBookStep(mirageListLength);
                    }
                    int newIndex = Math.abs(mirageProjectorBook.getStep());

                    if (newIndex != blockEntity.getMirageWorldIndex()) {
                        blockEntity.setMirageWorldIndex(newIndex);
                    }
                }
            }
            blockEntity.savePreviousTopPowerState(isTopPowered);
            blockEntity.savePreviousBottomPowerState(isPowered);
            blockEntity.savePreviousSidesPowerState(areSidesPowered);
            if (world.getTime() % 1000L == 0L && isPowered) {
                blockEntity.markDirty();
            }
            mirageWorldList.forEach((Integer, mirageWorld) -> {
                synchronized (mirageWorld) {
                    mirageWorld.tick();
                }
            });
            if(!world.isClient()){// note to self only update state properties in server-side
                world.setBlockState(pos,state.with(Properties.LIT,blockEntity.isPowered()),Block.NOTIFY_ALL);
            }
        }catch(Exception e){
            Mirage.LOGGER.error("Error on MirageBlockEntity.tick...",e);
        }
    }

    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);
    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<MirageBlockEntity>(this,"controller",0,this::predicate));
    }

    private PlayState predicate(AnimationEvent<MirageBlockEntity> event) {
        MirageBlockEntity subject = event.getAnimatable();
        AnimationController controller = event.getController();
        controller.transitionLengthTicks = 0;
        if(subject.isPowered()&&!subject.wasBottomPowered()){
            controller.setAnimation(new AnimationBuilder().addAnimation("ramp_up", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
            return PlayState.CONTINUE;
        }
        else if(!subject.isPowered() && subject.wasBottomPowered()){
            controller.setAnimation(new AnimationBuilder().addAnimation("ramp_down", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
            return PlayState.CONTINUE;
        }
        if(controller.getAnimationState() != AnimationState.Stopped) {
            return PlayState.CONTINUE;
        }
        if (subject.isPowered()) {
            controller.setAnimation(new AnimationBuilder().addAnimation("projecting", ILoopType.EDefaultLoopTypes.LOOP));
            return PlayState.CONTINUE;
        }
        controller.setAnimation(new AnimationBuilder().addAnimation("idle", ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;

    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

}






















