package net.phoboss.mirage.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface ErrorResponse {
    static InteractionResult onErrorActionResult(Level world, BlockPos pos, Player player, String field){
        onError(world,pos,player,field);
        return InteractionResult.FAIL;
    }
    static void onError(Level world,BlockPos pos,Player player,String field){
        if(!world.isClientSide()) {
            SpecialEffects.playSound(world, pos, SoundEvents.LIGHTNING_BOLT_THUNDER);
            player.displayClientMessage(Component.literal(field), false);
        }
    }

}
