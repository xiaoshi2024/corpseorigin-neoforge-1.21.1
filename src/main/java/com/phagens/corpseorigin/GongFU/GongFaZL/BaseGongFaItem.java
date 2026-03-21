package com.phagens.corpseorigin.GongFU.GongFaZL;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;


//模板
public class BaseGongFaItem extends Item {
    final String gongFaType;
    public BaseGongFaItem(Properties properties, String type) {
        super(properties);
        this.gongFaType = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        GongFaData data = getDataFromItem(stack);
        if (data != null){
           //翻译组件 支持多语种
            Component nameComponent = Component.translatable(data.getName())
                    .withStyle(style -> style.withBold(true).withColor(0xFFAA00));
            tooltipComponents.add(nameComponent);
            // 显示稀有度
            tooltipComponents.add(Component.literal(getRarityColor(data.getRarity()) +
                    "品级: " + getRarityName(data.getRarity())));
            tooltipComponents.add(Component.literal("§e功法层级: " + getCengName(data.getCeng())));
            // 显示属性
            tooltipComponents.add(Component.literal("§6武学加持:"));
            data.getAttributes().forEach((attr, value) ->
                    tooltipComponents.add(Component.literal("§7" + getAttributeName(attr) + ": §a+" + String.format("%.1f", value))));
            // 显示技能
            if (!data.getSkills().isEmpty()) {
                tooltipComponents.add(Component.literal("§d技艺:"));
                data.getSkills().forEach(skill ->
                        tooltipComponents.add(Component.literal("§7• " + skill)));
            }
        }
    }

    public static GongFaData getDataFromItem(ItemStack stack) {
        //getOrDEfault 从物品获取数据组件 不存在返回默认
        // 值 1 新的数据组件系统存储物品自定义的NBT数据  2 空的自定义数据    //copytype复制一份tag对象 避免重复
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("GongFaData")) {
            return GongFaData.fromNBT(tag.getCompound("GongFaData"));  //提取GongFaData子标签  fromNBT数据序列化为对象
        }
        return null;
    }

    // 设置功法数据到物品
    public void setDataToItem(ItemStack stack, GongFaData data) {
        CompoundTag rootTag = new CompoundTag();
        rootTag.put("GongFaData", data.toNBT());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(rootTag));
    }

    //属性名
    private String getAttributeName(String attrKey) {
        return switch (attrKey) {
            case "attack_damage" -> "攻击力";
            case "movement_speed" -> "移速";
            case "max_health" -> "生命值";
            case "armor" -> "护甲";
            case "knockback_resistance"->"抗性";
            default -> attrKey;
        };
    }

    private String getCengName(String attrKey) {
        return switch (attrKey) {
            case "copy_1" -> "一重天";
            case "copy_2" -> "二重天";
            case "copy_3" -> "三重天";
            case "copy_4" -> "四重天";
            case "copy_5" -> "五重天";
            case "copy_6" -> "六重天";
            case "copy_7" -> "七重天";
            case "copy_8" -> "八重天";
            case "copy_9" -> "九重天";
            default -> attrKey;
        };
    }


    private String getRarityName(int rarity) {
        return switch (rarity) {
            case 1 -> "人"; case 2 -> "地"; case 3 -> "天";
            case 4 -> "神"; case 5 -> "超神"; default -> "？";
        };
    }

    private String getRarityColor(int rarity) {
        return switch (rarity) {
            case 1 -> "§f"; case 2 -> "§a"; case 3 -> "§b";
            case 4 -> "§d"; case 5 -> "§6"; default -> "§7";
        };
    }

    @Override
    public Component getName(ItemStack stack) {
        GongFaData data = getDataFromItem(stack);
        if (data != null && data.getName() != null) {
            // 使用翻译组件，自动根据游戏语言切换
            return Component.translatable(data.getName())
                    .withStyle(style -> style.withBold(true).withColor(0xFFAA00));
        }
        return super.getName(stack);
    }
}
