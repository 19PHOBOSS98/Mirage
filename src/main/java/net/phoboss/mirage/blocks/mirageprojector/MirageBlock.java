package net.phoboss.mirage.blocks.mirageprojector;

import com.google.gson.JsonObject;
import net.coderbot.iris.shaderpack.materialmap.BlockRenderType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.ModBlockEntities;
import net.phoboss.mirage.utility.BookSettingsUtility;
import net.phoboss.mirage.utility.ErrorResponse;
import org.jetbrains.annotations.Nullable;

public class MirageBlock extends BaseEntityBlock implements EntityBlock, BookSettingsUtility {

    public MirageBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(defaultBlockState().setValue(BlockStateProperties.LIT, true));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MirageBlockEntity(pPos,pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(BlockStateProperties.LIT);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModBlockEntities.MIRAGE_BLOCK.get(), MirageBlockEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public void onBlockDestruction(Level pLevel, BlockPos pPos){
        if(pLevel.isClientSide()) {
            MirageBlockEntity blockEntity = (MirageBlockEntity) pLevel.getBlockEntity(pPos);
            blockEntity.stopMirageLoader();
            blockEntity.resetMirageWorlds();
        }
        //System.gc();
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
        onBlockDestruction(pLevel, pPos);
    }

    @Override
    public void wasExploded(Level pLevel, BlockPos pPos, Explosion pExplosion) {
        super.wasExploded(pLevel, pPos, pExplosion);
        onBlockDestruction(pLevel, pPos);
    }


    @Override
    public InteractionResult use(BlockState pState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult pHit) {
        ItemStack mainHandItemStack = player.getMainHandItem();
        Item mainHandItem = mainHandItemStack.getItem();
        if (hand == InteractionHand.MAIN_HAND) {
            if(!world.isClientSide()) {
                MirageBlockEntity blockEntity = (MirageBlockEntity) world.getBlockEntity(pos);
                if (mainHandItem == Items.REDSTONE_TORCH) {
                    blockEntity.setActiveLow(!blockEntity.isActiveLow());
                    return InteractionResult.SUCCESS;
                } else if (mainHandItem == Items.SOUL_TORCH) {
                    blockEntity.setAutoPlay(!blockEntity.isAutoPlay());
                    return InteractionResult.SUCCESS;
                } else if (mainHandItemStack.hasTag() && mainHandItemStack.getTag().contains("pages")) {
                    try {
                        executeBookProtocol(mainHandItemStack, blockEntity, blockEntity.getBookSettingsPOJO(), player.isCreative());
                        loadMirage(blockEntity);
                        return InteractionResult.SUCCESS;
                    }catch (Exception e){
                        Mirage.LOGGER.error(e.getMessage(),e);
                        ErrorResponse.onError(world,pos,player,e.getMessage());
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }


    @Override
    public void customJSONParsingValidation(JsonObject settingsJSON,boolean override) throws Exception {
        if(override){
            return;
        }
        if(settingsJSON.has("autoPlay")){
            throw new Exception("You have to use a soul torch to toggle autoPlay on ;)");
        }
    }

    @Override
    public void implementBookSettings(BlockEntity blockEntity, JsonObject newSettings,boolean override) throws Exception{
        if(blockEntity instanceof MirageBlockEntity mirageBlockEntity){
            MirageProjectorBook newBook = (MirageProjectorBook) mirageBlockEntity.getBookSettingsPOJO().validateNewBookSettings(newSettings);
            mirageBlockEntity.setBookSettingsPOJO(newBook);
        }
    }

    public static void loadMirage(MirageBlockEntity blockEntity) throws Exception{
        try {
            if (blockEntity != null) {
                blockEntity.startMirage();
            }
        }catch(Exception e){
            throw new Exception(e.getMessage(),e);
        }
    }

}
