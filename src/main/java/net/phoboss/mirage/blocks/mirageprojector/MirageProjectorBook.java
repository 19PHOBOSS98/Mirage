package net.phoboss.mirage.blocks.mirageprojector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.Vec3i;
import net.phoboss.mirage.client.rendering.customworld.StructureStates;
import net.phoboss.mirage.utility.Book;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//this class is used to hold Book&Quill settings
public class MirageProjectorBook implements Book {
    int[] move = {0,0,0};
    String mirror = "NONE";
    int rotate = 0;
    boolean activeLow = false;// toggle by tapping with redstone torch
    boolean loop = true;
    boolean autoPlay = false;// toggle by tapping with soul torch
    boolean reverse = false;
    float delay = 2f;
    List<String> files = new ArrayList<>();
    HashMap<Integer,Frame> frames = new HashMap<>();

    /*
    if(isAutoPlay) pause/play: 1/0
    else increment when rising-edge redstone signal is on side
     */
    int step = 0;

    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public float getDelay() {
        return delay;
    }

    public void setDelay(float delay) {
        this.delay = delay;
    }

    public HashMap<Integer,Frame> getFrames() {
        return frames;
    }

    public void setFrames(HashMap<Integer,Frame> frames) {
        this.frames = frames;
    }

    public int[] getMove() {
        return move;
    }

    public Vec3i getMoveVec3i() {
        return new Vec3i(move[0],move[1],move[2]);
    }

    public void setMove(int[] move) {
        this.move = move;
    }
    public void setMove(Vec3i move) {
        setMove(new int[]{move.getX(), move.getY(), move.getZ()});
    }
    public String getMirror() {
        return mirror;
    }

    public void setMirror(String mirror) {
        this.mirror = mirror;
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public boolean isActiveLow() {
        return activeLow;
    }

    public void setActiveLow(boolean activeLow) {
        this.activeLow = activeLow;
    }

    String[] BOOLEAN_KEYS = {"activeLow","loop","autoPlay","reverse"};

    @Override
    public String toString() {
        return "MirageProjectorBook{" +
                "move=" + Arrays.toString(move) +
                ", mirror='" + mirror + '\'' +
                ", rotate=" + rotate +
                ", activeLow=" + activeLow +
                ", loop=" + loop +
                ", autoPlay=" + autoPlay +
                ", reverse=" + reverse +
                ", delay=" + delay +
                ", files=" + files +
                ", frames=" + frames +
                ", step=" + step +
                ", BOOLEAN_KEYS=" + Arrays.toString(BOOLEAN_KEYS) +
                '}';
    }

    public String getRelevantSettings(){
        return "move=" + Arrays.toString(move) +
                ", mirror='" + mirror + '\'' +
                ", rotate=" + rotate+
                ", files=" + files +
                ", frames=" + frames;
    }


    @Override
    public Book validateNewBookSettings(JsonObject newSettings) throws Exception {
        JsonArray moveArray = newSettings.get("move").getAsJsonArray();
        if(moveArray.size()!=3){
            throw new Exception("Invalid Move Value: "+ newSettings.get("move"));
        }
        try {
            moveArray.forEach((mv)->{
                mv.getAsInt();
            });
        }catch (Exception e){
            throw new Exception("Invalid Move Value: "+ newSettings.get("move"));
        }

        for(String key:BOOLEAN_KEYS){
            String value = newSettings.get(key).getAsString();
            if(value.equals("false") == value.equals("true")){
                throw new Exception("Invalid "+key+" Value: "+ value);
            }
        }

        MirageProjectorBook newBook = new Gson().fromJson(newSettings, MirageProjectorBook.class);



        if(!StructureStates.ROTATION_STATES_KEYS.contains(newBook.getRotate())){
            throw new Exception("Invalid Rotation Value: "+ newBook.getRotate() +"\nSupported Values: 0,90,180,270");
        }
        if(!StructureStates.MIRROR_STATES_KEYS.contains(newBook.getMirror())){
            throw new Exception("Invalid Mirror Value: "+ newBook.getMirror() +"\nSupported Values: NONE,FRONT_BACK,LEFT_RIGHT");
        }
        if(newBook.getStep()<0){
            throw new Exception("Invalid Index Value: "+ newBook.getStep());
        }

        return newBook;
    }


}
