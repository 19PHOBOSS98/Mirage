package net.phoboss.mirage.blocks.mirageprojector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3i;
import net.phoboss.mirage.client.rendering.customworld.StructureStates;
import net.phoboss.mirage.utility.Book;

import java.util.*;

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
    float pSpinOffset = 0;

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

    public Vec3f getPScaleVec() {
        return new Vec3f(pScale[0],pScale[1],pScale[2]);
    }

    public void setPScale(float[] pScale) {
        this.pScale = pScale;
    }

    public float[] getPMove() {
        return pMove;
    }

    public Vec3d getPMoveAsVec() {
        return new Vec3d(
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
    public void setPRotate(float[] pRotate) {
        this.pRotate = pRotate;
        //setPRotateAsQuat(pRotate);
    }
    public Vec3f getPRotateAsVec() {
        return new Vec3f(pRotate[0],pRotate[1],pRotate[2]);
    }

    public Quaternion pRotateAsQuat = Quaternion.IDENTITY;

    public Quaternion getPRotateAsQuat() {
        return pRotateAsQuat;
    }

    public static Quaternion convertToQuat(float[] pRotate) {
        return new Quaternion(pRotate[0],pRotate[1],pRotate[2],true);
    }

    public void setPRotateAsQuat(float[] pRotate) {
        /*
        if(pRotate[0]==0 && pRotate[1]==0 && pRotate[2]==0){
            this.pRotateAsQuat = new Quaternionf();
            return;
        }
        Vec3f pRot = bookSettings.getPRotateAsVec();
        pRot.mul(0.017453292F);
        float pRotAngle = pRot.length();
        return Axis.of(pRot.normalize()).rotationDegrees(pRotAngle);
        return new Quaternionf(new AxisAngle4f(pRotAngle, pRot.x, pRot.y, pRot.z)//couldn't get these to work :(
        */
        this.pRotateAsQuat = convertToQuat(pRotate);
    }

    public float[] getPRotatePivot() {
        return pRotatePivot;
    }

    public Vec3d getPRotatePivotAsVec() {
        return new Vec3d(
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
    public Vec3d getPSpinPivotAsVec() {
        return new Vec3d(
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
    public Vec3f getPSpinAxisAsVec3() {
        return new Vec3f(pSpinAxis[0],pSpinAxis[1],pSpinAxis[2]);
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

    public float getPSpinOffset() {
        return pSpinOffset;
    }
    public void setPSpinOffset(float pSpinOffset) {
        this.pSpinOffset = pSpinOffset;
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

    public JsonObject validateJSONObject(JsonObject newSettings) throws Exception {

        if(newSettings.get("move") != null) {
            JsonArray moveArray = newSettings.get("move").getAsJsonArray();
            if (moveArray.size() != 3) {
                throw new Exception("Invalid Move Value: " + newSettings.get("move"));
            }
            try {
                moveArray.forEach((mv) -> {
                    mv.getAsInt();
                });
            } catch (Exception e) {
                throw new Exception("Invalid Move Value: " + newSettings.get("move"));
            }
        }

        if(newSettings.get("rotate") != null) {
            if (!StructureStates.ROTATION_STATES_KEYS.contains(newSettings.get("rotate").getAsInt())) {
                throw new Exception("Invalid Rotation Value: " + newSettings.get("rotate") + "\nSupported Values: 0,90,180,270");
            }
        }

        if(newSettings.get("mirror") != null) {
            if (!StructureStates.MIRROR_STATES_KEYS.contains(newSettings.get("mirror").getAsString())) {
                throw new Exception("Invalid Mirror Value: " + newSettings.get("mirror") + "\nSupported Values: NONE,FRONT_BACK,LEFT_RIGHT");
            }
        }

        if(newSettings.get("step") != null) {
            if (newSettings.get("step").getAsInt() < 0) {
                throw new Exception("Invalid Index Value: " + newSettings.get("step"));
            }
        }

        for (String key : BOOLEAN_KEYS) {
            if(newSettings.get(key) != null) {
                String value = newSettings.get(key).getAsString();
                if (value.equals("false") == value.equals("true")) {
                    throw new Exception("Invalid " + key + " Value: " + value);
                }
            }
        }

        if(newSettings.get("pMove") != null) {
            JsonArray pMoveArray = newSettings.get("pMove").getAsJsonArray();
            if (pMoveArray.size() != 3) {
                throw new Exception("Invalid pMove Value: " + newSettings.get("pMove"));
            }
            try {
                pMoveArray.forEach((mv) -> {
                    mv.getAsFloat();
                });
            } catch (Exception e) {
                throw new Exception("Invalid pMove Value: " + newSettings.get("pMove"));
            }
        }

        if(newSettings.get("pScale") != null) {
            JsonArray pScaleArray = newSettings.get("pScale").getAsJsonArray();
            if (pScaleArray.size() != 3) {
                throw new Exception("Invalid pScale Value: " + newSettings.get("pScale"));
            }
            try {
                pScaleArray.forEach((v) -> {
                    v.getAsFloat();
                });
            } catch (Exception e) {
                throw new Exception("Invalid pScale Value: " + newSettings.get("pScale"));
            }
        }

        if(newSettings.get("pRotatePivot") != null) {
            JsonArray pRotatePivotArray = newSettings.get("pRotatePivot").getAsJsonArray();
            if (pRotatePivotArray.size() != 3) {
                throw new Exception("Invalid pRotatePivot Value: " + newSettings.get("pRotatePivot"));
            }
            try {
                pRotatePivotArray.forEach((v) -> {
                    v.getAsDouble();
                });
            } catch (Exception e) {
                throw new Exception("Invalid pRotatePivot Value: " + newSettings.get("pRotatePivot"));
            }
        }

        if(newSettings.get("pRotate") != null) {
            JsonArray pRotateArray = newSettings.get("pRotate").getAsJsonArray();
            try {
                pRotateArray.forEach((v) -> {
                    v.getAsFloat();
                });
            } catch (Exception e) {
                throw new Exception("Invalid pRotate Value: " + newSettings.get("pRotate"));
            }
            if (pRotateArray.size() != 3) {
                if (pRotateArray.size() == 4) {
                    //USER INPUT: 0:W,1:X,2:Y,3:Z
                    //1.18- 1.19: QUATERNION = i,j,k,r
                    //1.20: QUATERNION = x,y,z,w
                    /*JsonObject newPRotateAsQuat = new JsonObject();
                    newPRotateAsQuat.add("x", pRotateArray.get(1));
                    newPRotateAsQuat.add("y", pRotateArray.get(2));
                    newPRotateAsQuat.add("z", pRotateArray.get(3));
                    newPRotateAsQuat.add("w", pRotateArray.get(0));*/
                    JsonObject newPRotateAsQuat = new Gson().toJsonTree(new Quaternion(
                            pRotateArray.get(1).getAsFloat(),//i:x
                            pRotateArray.get(2).getAsFloat(),//j:y
                            pRotateArray.get(3).getAsFloat(),//k:z
                            pRotateArray.get(0).getAsFloat()//r:w
                    )).getAsJsonObject();
                    newSettings.add("pRotateAsQuat", newPRotateAsQuat);
                    JsonArray emptyArray = new JsonArray();
                    emptyArray.add(0);
                    emptyArray.add(0);
                    emptyArray.add(0);
                    newSettings.add("pRotate", emptyArray);
                } else {
                    throw new Exception("Invalid pRotate Value: " + newSettings.get("pRotate"));
                }
            } else {
                JsonObject newPRotateAsQuat = convertToJSONQuaternion(pRotateArray);
                newSettings.add("pRotateAsQuat", newPRotateAsQuat);
            }
        }

        if(newSettings.get("pSpinPivot") != null) {
            JsonArray pSpinPivotArray = newSettings.get("pSpinPivot").getAsJsonArray();
            if (pSpinPivotArray.size() != 3) {
                throw new Exception("Invalid pSpinPivot Value: " + newSettings.get("pSpinPivot"));
            }
            try {
                pSpinPivotArray.forEach((v) -> {
                    v.getAsDouble();
                });
            } catch (Exception e) {
                throw new Exception("Invalid pSpinPivot Value: " + newSettings.get("pSpinPivot"));
            }
        }

        if(newSettings.get("pSpinAxis") != null) {
            JsonArray pSpinAxisArray = newSettings.get("pSpinAxis").getAsJsonArray();
            if (pSpinAxisArray.size() != 3) {
                throw new Exception("Invalid pSpinAxis Value: " + newSettings.get("pSpinAxis"));
            }
            try {
                pSpinAxisArray.forEach((v) -> {
                    v.getAsFloat();
                });
            } catch (Exception e) {
                throw new Exception("Invalid pSpinAxis Value: " + newSettings.get("pSpinAxis"));
            }
            Vec3f spinAxis = new Vec3f(pSpinAxisArray.get(0).getAsFloat(),pSpinAxisArray.get(1).getAsFloat(),pSpinAxisArray.get(2).getAsFloat());
            if( spinAxis.getX() == 0
                    && spinAxis.getY() == 0
                    && spinAxis.getZ() == 0){
                throw new Exception("Invalid pSpinAxis Value, must have at least one non-zero value: "+ newSettings.get("pSpinAxis"));
            }else{
                try {
                    spinAxis.normalize();
                    JsonArray norm = new JsonArray();
                    norm.add(spinAxis.getX());
                    norm.add(spinAxis.getY());
                    norm.add(spinAxis.getZ());
                    newSettings.add("pSpinAxis",norm);
                }catch (Exception e){
                    throw new Exception("Could Not Normalize pSpinAxis Value: "+ newSettings.get("pSpinAxis"));
                }
            }
        }

        if(newSettings.get("pSpinSpeed") != null) {
            try {
                newSettings.get("pSpinSpeed").getAsFloat();
            } catch (Exception e) {
                throw new Exception("Invalid pSpinSpeed Value: " + newSettings.get("pSpinSpeed"));
            }
        }
        if(newSettings.get("pSpinOffset") != null) {
            try {
                newSettings.get("pSpinOffset").getAsFloat();
            } catch (Exception e) {
                throw new Exception("Invalid pSpinOffset Value: " + newSettings.get("pSpinOffset"));
            }
        }

        return newSettings;
    }

    public static JsonObject convertToJSONQuaternion(JsonArray pRotateArray){
        /*Quaternion quat = convertToQuat(new float[]{pRotateArray.get(0).getAsFloat(),pRotateArray.get(1).getAsFloat(),pRotateArray.get(2).getAsFloat()});
        JsonObject quatJson = new JsonObject();
        quatJson.addProperty("w",quat.w);
        quatJson.addProperty("x",quat.x);
        quatJson.addProperty("y",quat.y);
        quatJson.addProperty("z",quat.z);*/
        return new Gson().toJsonTree(new Quaternion(
                pRotateArray.get(0).getAsFloat(),
                pRotateArray.get(1).getAsFloat(),
                pRotateArray.get(2).getAsFloat(),true)).getAsJsonObject();
    }

    @Override
    public Book validateNewBookSettings(JsonObject newSettings) throws Exception {
        newSettings = validateJSONObject(newSettings);

        Set<Map.Entry<String, JsonElement>> entrySet = newSettings.get("frames").getAsJsonObject().entrySet();
        for(Map.Entry<String,JsonElement> entry : entrySet){
            JsonObject newFrame = validateJSONObject(entry.getValue().getAsJsonObject());
            entry.setValue(newFrame);
        }

        MirageProjectorBook newBook = new Gson().fromJson(newSettings, MirageProjectorBook.class);

        return newBook;
    }
}
