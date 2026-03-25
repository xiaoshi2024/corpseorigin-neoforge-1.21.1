// 创建 BaseGongFa.java
package com.phagens.corpseorigin.Item;

import com.phagens.corpseorigin.GongFU.GongFaZL.BaseGongFaItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class BaseGongFa extends BaseGongFaItem {
    public BaseGongFa(Properties properties, String type) {
        super(properties, type);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}