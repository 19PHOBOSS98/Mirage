package net.phoboss.mirage.client.rendering.customworld;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public interface StructureStates {
    List<String> MIRROR_STATES_KEYS = Util.make(new ArrayList<>(), (map) -> {
        map.add("NONE");
        map.add("FRONT_BACK");
        map.add("LEFT_RIGHT");
    });
    List<Integer> ROTATION_STATES_KEYS = Util.make(new ArrayList<>(), (map) -> {
        map.add(0);
        map.add(90);
        map.add(180);
        map.add(270);
    });
    Object2ObjectLinkedOpenHashMap<String, BlockMirror> MIRROR_STATES = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
        map.put(MIRROR_STATES_KEYS.get(0),BlockMirror.NONE);
        map.put(MIRROR_STATES_KEYS.get(1),BlockMirror.FRONT_BACK);
        map.put(MIRROR_STATES_KEYS.get(2),BlockMirror.LEFT_RIGHT);
    });
    Object2ObjectLinkedOpenHashMap<Integer, BlockRotation> ROTATION_STATES = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
        map.put(ROTATION_STATES_KEYS.get(0),BlockRotation.NONE);
        map.put(ROTATION_STATES_KEYS.get(1),BlockRotation.CLOCKWISE_90);
        map.put(ROTATION_STATES_KEYS.get(2),BlockRotation.CLOCKWISE_180);
        map.put(ROTATION_STATES_KEYS.get(3),BlockRotation.COUNTERCLOCKWISE_90);
    });
}
