package net.phoboss.mirage.blocks.mirageprojector;


import com.mojang.math.Quaternion;
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

    public Quaternion getPRotateAsQuat() {
        return new Quaternion(pRotate[0],pRotate[1],pRotate[2],true);
    }

    public void setPRotate(float[] pRotate) {
        this.pRotate = pRotate;
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

    public String toString() {
        return "Frame{" +
                "move=" + Arrays.toString(move) +
                ", mirror='" + mirror + '\'' +
                ", rotate=" + rotate +
                '}';
    }
}
