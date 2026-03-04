package com.phagens.corpseorigin.GongFU.GongFaZL;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//功法数据
public class GongFaData {
    /// nbt:TypeId 类型 ，
    /// nbt:Rarity 稀有度 1-9
    /// nbt:Ceng 层数
    /// nbt:Attributes 属性
    /// nbt:Skills 技能
    private final String typeId;  // 类型标识
    private final Map<String, Double> attributes;  // 属性加成
    private final List<String> skills;  // 技能列表
    private final int rarity;  // 稀有度 1-9
    private final String Ceng;//层级

    public GongFaData(String typeId, Map<String, Double> attributes, List<String> skills, int rarity, String ceng) {
        this.typeId = typeId;
        this.attributes = new HashMap<>(attributes);
        this.skills = new ArrayList<>(skills);
        this.rarity = rarity;
        this.Ceng=ceng ;
    }

    public String getTypeId() { return typeId; }
    public Map<String, Double> getAttributes() { return attributes; }
    public List<String> getSkills() { return skills; }
    public int getRarity() { return rarity; }
    public String getCeng() { return Ceng; }
    //序列化到nbt
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("TypeId", typeId);
        tag.putInt("Rarity", rarity);
        tag.putString("Ceng", Ceng);
        // 保存属性
        CompoundTag attrTag = new CompoundTag();
        attributes.forEach(attrTag::putDouble);
        tag.put("Attributes", attrTag);
        // 保存技能
        ListTag skillsTag = new ListTag();
        skills.forEach(skill -> skillsTag.add(StringTag.valueOf(skill)));
        tag.put("Skills", skillsTag);
        return tag;
    }

    // 从NBT反序列化
    public static GongFaData fromNBT(CompoundTag tag) {
        String typeId = tag.getString("TypeId");
        int rarity = tag.getInt("Rarity");
        String ceng =tag.getString("Ceng");

        Map<String, Double> attributes = new HashMap<>();
        if (tag.contains("Attributes")) {
            CompoundTag attrTag = tag.getCompound("Attributes");
            for (String key : attrTag.getAllKeys()) {
                attributes.put(key, attrTag.getDouble(key));
            }
        }

        List<String> skills = new ArrayList<>();
        if (tag.contains("Skills")) {
            ListTag skillsTag = tag.getList("Skills", Tag.TAG_STRING);
            for (int i = 0; i < skillsTag.size(); i++) {
                skills.add(skillsTag.getString(i));
            }
        }

        return new GongFaData(typeId, attributes, skills, rarity,ceng);
    }



}
