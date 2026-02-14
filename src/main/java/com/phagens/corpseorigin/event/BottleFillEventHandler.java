package com.phagens.corpseorigin.event;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Block.custom.QiXingGuan;
import com.phagens.corpseorigin.data.InfectionData;
import com.phagens.corpseorigin.register.Moditems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class BottleFillEventHandler {
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.isCanceled()) {
            return;
        }

        ItemStack heldItem = event.getItemStack();
        if (heldItem.getItem() == Items.GLASS_BOTTLE) {
            Player player = event.getEntity();
            Level level = player.level();
            
            if (level.isClientSide) {
                return;
            }
            
            // 检查玩家视线是否对准水方块
            BlockHitResult hitResult = level.clip(new ClipContext(
                player.getEyePosition(),
                player.getEyePosition().add(player.getViewVector(1.0F).scale(5.0F)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                player
            ));
            
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos targetPos = hitResult.getBlockPos();
                if (level.getBlockState(targetPos).getFluidState().is(FluidTags.WATER)) {
                    if (level instanceof ServerLevel serverLevel && InfectionData.isWaterInfectedStatic(serverLevel, targetPos)) {
                        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                        event.setCanceled(true);
                        player.setItemInHand(event.getHand(), new ItemStack(Moditems.BYWATER_BOTTLE.get()));
                    }
                }
            }
        }
    }
}