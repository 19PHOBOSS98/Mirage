package net.phoboss.mirage.utility;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ErrorResponse {
    static ActionResult onErrorActionResult(World world,BlockPos pos,PlayerEntity player,String field){
        onError(world,pos,player,field);
        return ActionResult.FAIL;
    }
    static void onError(World world,BlockPos pos,PlayerEntity player,String field){
        if(!world.isClient()) {
            SpecialEffects.playSound(world, pos, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER);
            player.sendMessage(Text.literal(field), false);
        }
    }

}
