package net.phoboss.mirage.blocks.mirageprojector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
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
    float[] pScale = {1,1,1};
    float[] pMove = {0,0,0};
    float[] pRotate = {0,0,0};
    float[] pRotatePivot = {0,0,0};
    float[] pSpinPivot = {0,0,0};
    float[] pSpinAxis = {0,1,0};
    float pSpinSpeed = 0;

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

    public float[] getPScale() {
        return pScale;
    }

    public Vector3f getPScaleVec() {
        return new Vector3f(getPScale());
    }

    public void setPScale(float[] pScale) {
        this.pScale = pScale;
    }

    public float[] getPMove() {
        return pMove;
    }

    public Vector3d getPMoveAsVec() {
        return new Vector3d(
                pMove[0],
                pMove[1],
                pMove[2]);
    }

    public void setPMove(float[] pMove) {
        this.pMove = pMove;
    }

    public float[] getPRotate() {
        return pRotate;
    }
    public Vector3f getPRotateAsVec() {
        return new Vector3f(getPRotate());
    }

    public Quaternion getPRotateAsQuat() {
        return new Quaternion(pRotate[0],pRotate[1],pRotate[2],true);
    }

    public void setPRotate(float[] pRotate) {
        this.pRotate = pRotate;
    }

    public float[] getPRotatePivot() {
        return pRotatePivot;
    }

    public Vector3d getPRotatePivotAsVec() {
        return new Vector3d(
                pRotatePivot[0],
                pRotatePivot[1],
                pRotatePivot[2]);
    }

    public void setPRotatePivot(float[] pRotatePivot) {
        this.pRotatePivot = pRotatePivot;
    }

    public float[] getPSpinPivot() {
        return pSpinPivot;
    }
    public Vector3d getPSpinPivotAsVec() {
        return new Vector3d(
                pSpinPivot[0],
                pSpinPivot[1],
                pSpinPivot[2]);
    }

    public void setPSpinPivot(float[] pSpinPivot) {
        this.pSpinPivot = pSpinPivot;
    }

    public float[] getPSpinAxis() {
        return pSpinAxis;
    }
    public Vector3f getPSpinAxisAsVec3() {
        return new Vector3f(getPSpinAxis());
    }

    public void setPSpinAxis(float[] pSpinAxis) {
        this.pSpinAxis = pSpinAxis;
    }

    public float getPSpinSpeed() {
        return pSpinSpeed;
    }

    public void setPSpinSpeed(float pSpinSpeed) {
        this.pSpinSpeed = pSpinSpeed;
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
                ", files=" + files.toString() +
                ", frames=" + frames.toString() +
                ", step=" + step +
                ", pScale=" + Arrays.toString(pScale) +
                ", pMove=" + Arrays.toString(pMove) +
                ", pRotate=" + Arrays.toString(pRotate) +
                ", pRotatePivot=" + Arrays.toString(pRotatePivot) +
                ", pSpinPivot=" + Arrays.toString(pSpinPivot) +
                ", pSpinAxis=" + Arrays.toString(pSpinAxis) +
                ", pSpinSpeed=" + pSpinSpeed +
                ", BOOLEAN_KEYS=" + Arrays.toString(BOOLEAN_KEYS) +
                '}';
    }

    public String getRelevantSettings(){
        return "move=" + Arrays.toString(move) +
                ", mirror='" + mirror + '\'' +
                ", rotate=" + rotate+
                ", files=" + files +
                ", frames=" + frames.toString();
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


        JsonArray pMoveArray = newSettings.get("pMove").getAsJsonArray();
        if(pMoveArray.size()!=3){
            throw new Exception("Invalid pMove Value: "+ newSettings.get("pMove"));
        }
        try {
            pMoveArray.forEach((mv)->{
                mv.getAsFloat();
            });
        }catch (Exception e){
            throw new Exception("Invalid pMove Value: "+ newSettings.get("pMove"));
        }

        JsonArray pScaleArray = newSettings.get("pScale").getAsJsonArray();
        if(pScaleArray.size()!=3){
            throw new Exception("Invalid pScale Value: "+ newSettings.get("pScale"));
        }
        try {
            pScaleArray.forEach((v)->{
                v.getAsFloat();
            });
        }catch (Exception e){
            throw new Exception("Invalid pScale Value: "+ newSettings.get("pScale"));
        }

        JsonArray pRotateArray = newSettings.get("pRotate").getAsJsonArray();
        if(pRotateArray.size()!=3){
            throw new Exception("Invalid pRotate Value: "+ newSettings.get("pRotate"));
        }
        try {
            pRotateArray.forEach((v)->{
                v.getAsFloat();
            });
        }catch (Exception e){
            throw new Exception("Invalid pRotate Value: "+ newSettings.get("pRotate"));
        }

        JsonArray pRotatePivotArray = newSettings.get("pRotatePivot").getAsJsonArray();
        if(pRotatePivotArray.size()!=3){
            throw new Exception("Invalid pRotatePivot Value: "+ newSettings.get("pRotatePivot"));
        }
        try {
            pRotatePivotArray.forEach((v)->{
                v.getAsDouble();
            });
        }catch (Exception e){
            throw new Exception("Invalid pRotatePivot Value: "+ newSettings.get("pRotatePivot"));
        }

        JsonArray pSpinPivotArray = newSettings.get("pSpinPivot").getAsJsonArray();
        if(pSpinPivotArray.size()!=3){
            throw new Exception("Invalid pSpinPivot Value: "+ newSettings.get("pSpinPivot"));
        }
        try {
            pSpinPivotArray.forEach((v)->{
                v.getAsDouble();
            });
        }catch (Exception e){
            throw new Exception("Invalid pSpinPivot Value: "+ newSettings.get("pSpinPivot"));
        }

        JsonArray pSpinAxisArray = newSettings.get("pSpinAxis").getAsJsonArray();
        if(pSpinAxisArray.size()!=3){
            throw new Exception("Invalid pSpinAxis Value: "+ newSettings.get("pSpinAxis"));
        }
        try {
            pSpinAxisArray.forEach((v)->{
                v.getAsFloat();
            });
        }catch (Exception e){
            throw new Exception("Invalid pSpinAxis Value: "+ newSettings.get("pSpinAxis"));
        }
        if( pSpinAxisArray.get(0).getAsFloat() == 0
            && pSpinAxisArray.get(1).getAsFloat() == 0
            && pSpinAxisArray.get(2).getAsFloat() == 0){
            throw new Exception("Invalid pSpinAxis Value, must have at least one non-zero value: "+ newSettings.get("pSpinAxis"));
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

        try {
            Vector3f normalizedSpinAxis = new Vector3f(newBook.getPSpinAxis());
            normalizedSpinAxis.normalize();
            newBook.getPSpinAxis()[0] = normalizedSpinAxis.x();
            newBook.getPSpinAxis()[1] = normalizedSpinAxis.y();
            newBook.getPSpinAxis()[2] = normalizedSpinAxis.z();
        }catch (Exception e){
            throw new Exception("Could Not Normalize pSpinAxis Value: "+ newSettings.get("pSpinAxis"));
        }

        return newBook;
    }
}
