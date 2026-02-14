package com.phagens.corpseorigin.event;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Block.custom.QiXingGuan;
import com.phagens.corpseorigin.data.InfectionData;
import com.phagens.corpseorigin.register.Moditems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class BucketEmptyEventHandler {
    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.isCanceled()) {
            return;
        }

        ItemStack heldItem = event.getItemStack();
        if (heldItem.getItem() == Moditems.BYWATER_BUCKET.get() && event.getUsePhase() == UseItemOnBlockEvent.UsePhase.BLOCK) {
            BlockPos pos = event.getPos().relative(event.getFace());
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                InfectionData.markWaterInfectedStatic(serverLevel, pos);
            }
        }
    }
}