package net.phoboss.mirage.client.rendering.customworld;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;


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
    Object2ObjectLinkedOpenHashMap<String, Mirror> MIRROR_STATES = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
        map.put(MIRROR_STATES_KEYS.get(0),Mirror.NONE);
        map.put(MIRROR_STATES_KEYS.get(1),Mirror.FRONT_BACK);
        map.put(MIRROR_STATES_KEYS.get(2),Mirror.LEFT_RIGHT);
    });
    Object2ObjectLinkedOpenHashMap<Integer, Rotation> ROTATION_STATES = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
        map.put(ROTATION_STATES_KEYS.get(0),Rotation.NONE);
        map.put(ROTATION_STATES_KEYS.get(1),Rotation.CLOCKWISE_90);
        map.put(ROTATION_STATES_KEYS.get(2),Rotation.CLOCKWISE_180);
        map.put(ROTATION_STATES_KEYS.get(3),Rotation.COUNTERCLOCKWISE_90);
    });
}
