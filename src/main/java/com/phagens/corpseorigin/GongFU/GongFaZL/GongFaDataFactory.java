package com.phagens.corpseorigin.GongFU.GongFaZL;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//功法真数据
public class GongFaDataFactory {
    // 功法数据
    public static GongFaData createLeiXiData(int rarity,String ceng) {
        Map<String, Double> attrs = new HashMap<>();
        List<String> skills = new ArrayList<>();
        switch (rarity) {
            case 1: // 普通
                attrs.put("attack_damage", 2.0);
                attrs.put("movement_speed", 0.05);
                skills.add("基础雷击");
                break;
            case 3: // 稀有
                attrs.put("attack_damage", 5.0);
                attrs.put("movement_speed", 0.1);
                attrs.put("max_health", 10.0);
                skills.add("雷电打击");
                skills.add("疾风雷");
                break;
            case 5: // 传说
                attrs.put("attack_damage", 12.0);
                attrs.put("movement_speed", 0.2);
                attrs.put("max_health", 25.0);
                attrs.put("knockback_resistance", 0.3);
                skills.add("天雷降世");
                skills.add("万雷归宗");
                skills.add("雷神附体");
                break;
        }
        return new GongFaData("REX", attrs, skills, rarity,ceng);
    }

    // 通用创建方法
    public static ItemStack createGongFaItem(BaseGongFaItem item, int rarity, String ceng) {
        ItemStack stack = new ItemStack(item);
        GongFaData data = switch (item.gongFaType) {
            case "REX" -> createLeiXiData(rarity,ceng);

            default -> throw new IllegalArgumentException("未知功法类型: " + item.gongFaType);
        };

        item.setDataToItem(stack, data);
        return stack;
    }
}
