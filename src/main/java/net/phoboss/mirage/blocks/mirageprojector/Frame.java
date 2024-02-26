package net.phoboss.mirage.blocks.mirageprojector;

import net.minecraft.util.math.Vec3i;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    public Quaternionf pRotateAsQuat = new Quaternionf();

    public Quaternionf getPRotateAsQuat() {
        return pRotateAsQuat;
    }

    public static Quaternionf convertToQuat(float[] pRotate) {
        float xRad = pRotate[0] * 0.017453292F;
        float yRad = pRotate[1] * 0.017453292F;
        float zRad = pRotate[2] * 0.017453292F;
        float f = (float)Math.sin(0.5F * xRad);
        float g = (float)Math.cos(0.5F * xRad);
        float h = (float)Math.sin(0.5F * yRad);
        float i = (float)Math.cos(0.5F * yRad);
        float j = (float)Math.sin(0.5F * zRad);
        float k = (float)Math.cos(0.5F * zRad);
        float x = f * i * k + g * h * j;
        float y = g * h * k - f * i * j;
        float z = f * h * k + g * i * j;
        float w = g * i * k - f * h * j;
        return new Quaternionf(x,y,z,w);
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
