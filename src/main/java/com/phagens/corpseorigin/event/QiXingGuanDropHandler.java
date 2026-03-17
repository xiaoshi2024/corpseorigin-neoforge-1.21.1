package com.phagens.corpseorigin.event;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.register.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;

/**
 * 七星棺丢弃处理器
 * 当玩家将七星棺丢弃到水中时，自动放置为方块
 */
@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class QiXingGuanDropHandler {

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemEntity itemEntity = event.getEntity();
        ItemStack itemStack = itemEntity.getItem();

        // 检查是否是七星棺物品
        if (itemStack.is(BlockRegistry.QI_XING_GUAN.get().asItem())) {
            Level level = itemEntity.level();

            // 只在服务端处理
            if (level.isClientSide) {
                return;
            }

            // 延迟检查，等待物品实体落地或进入水中
            level.getServer().tell(new net.minecraft.server.TickTask(
                    level.getServer().getTickCount() + 5,
                    () -> checkAndPlaceCoffin(itemEntity, (ServerLevel) level)
            ));
        }
    }

    /**
     * 检查物品是否在水中，如果是则放置为方块
     */
    private static void checkAndPlaceCoffin(ItemEntity itemEntity, ServerLevel level) {
        // 检查物品实体是否还存在且未被拾取
        if (!itemEntity.isAlive() || itemEntity.isRemoved()) {
            return;
        }

        BlockPos pos = itemEntity.blockPosition();
        FluidState fluidState = level.getFluidState(pos);

        // 检查是否在水中
        if (fluidState.is(FluidTags.WATER)) {
            // 检查位置是否可以放置方块
            BlockState currentState = level.getBlockState(pos);
            if (currentState.isAir() || currentState.getFluidState().is(FluidTags.WATER)) {
                // 放置七星棺方块
                BlockState coffinState = BlockRegistry.QI_XING_GUAN.get().defaultBlockState();
                level.setBlock(pos, coffinState, 3);

                // 移除物品实体
                itemEntity.discard();

                CorpseOrigin.LOGGER.info("七星棺被丢弃到水中，已在 {} 位置自动放置", pos);
            }
        }
    }
}
