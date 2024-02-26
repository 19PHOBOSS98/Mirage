package net.phoboss.mirage.blocks.mirageprojector;


import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import net.minecraft.core.Vec3i;

import java.util.Arrays;

public class Frame {
    int[] move = {0,0,0};
    String mirror = "NONE";
    int rotate = 0;

    float[] pScale = {1,1,1};

    float[] pMove = {0,0,0};

    float[] pRotate = {0,0,0};
    float[] pRotatePivot = {0,0,0};

    float[] pSpinPivot = {0,0,0};
    float[] pSpinAxis = {0,1,0};
    float pSpinSpeed = 0;

    float pSpinOffset = 0;

    public Vec3i getMoveVec3i() {
        return new Vec3i(move[0],move[1],move[2]);
    }

    public String getMirror() {
        return mirror;
    }

    public int getRotate() {
        return rotate;
    }

    public float[] getPScale() {
        return pScale;
    }

    public void setPScale(float[] pScale) {
        this.pScale = pScale;
    }

    public float[] getPMove() {
        return pMove;
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
    public Quaternion pRotateAsQuat = Quaternion.ONE;

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
        Vector3f pRot = bookSettings.getPRotateAsVec();
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

    public void setPRotatePivot(float[] pRotatePivot) {
        this.pRotatePivot = pRotatePivot;
    }

    public float[] getPSpinPivot() {
        return pSpinPivot;
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

    public float getPSpinOffset() {
        return pSpinOffset;
    }
    public void setPSpinOffset(float pSpinOffset) {
        this.pSpinOffset = pSpinOffset;
    }
    public String toString() {
        return "Frame{" +
                "move=" + Arrays.toString(move) +
                ", mirror='" + mirror + '\'' +
                ", rotate=" + rotate +
                '}';
    }
}
