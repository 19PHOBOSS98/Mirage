package net.phoboss.mirage.blocks.mirageprojector;

import net.minecraft.util.math.Vec3i;

import java.util.Arrays;

public class Frame {
    int[] move = {0,0,0};
    String mirror = "NONE";
    int rotate = 0;

    public Vec3i getMoveVec3i() {
        return new Vec3i(move[0],move[1],move[2]);
    }

    public String getMirror() {
        return mirror;
    }

    public int getRotate() {
        return rotate;
    }

    @Override
    public String toString() {
        return "Frame{" +
                "move=" + Arrays.toString(move) +
                ", mirror='" + mirror + '\'' +
                ", rotate=" + rotate +
                '}';
    }
}
