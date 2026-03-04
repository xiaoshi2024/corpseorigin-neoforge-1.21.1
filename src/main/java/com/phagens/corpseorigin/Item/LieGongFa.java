package com.phagens.corpseorigin.Item;

import com.phagens.corpseorigin.GongFU.GongFaZL.BaseGongFaItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class LieGongFa extends BaseGongFaItem {

    public LieGongFa(Properties properties, String type) {
        super(new Properties().stacksTo(1), "REX");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
