package com.phagens.corpseorigin.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InfectionData extends SavedData {
    private static final String DATA_NAME = CorpseOrigin.MODID + "_infection_data";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_ENERGY = 15;
    
    // 棺材位置到感染水位置的映射
    private Map<String, Set<String>> coffinInfectedWaterMap = new HashMap<>();
    // 水位置到能量值的映射
    private Map<String, Integer> infectedWaterEnergyMap = new HashMap<>();

    public InfectionData() {
    }

    public static InfectionData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(InfectionData::new, InfectionData::load),
            DATA_NAME
        );
    }

    public static InfectionData load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        InfectionData data = new InfectionData();
        
        // 加载棺材感染水映射
        ListTag coffinList = tag.getList("coffin_infected_water", Tag.TAG_COMPOUND);
        for (Tag coffinTag : coffinList) {
            CompoundTag coffinCompound = (CompoundTag) coffinTag;
            String coffinPos = coffinCompound.getString("coffin_pos");
            ListTag waterList = coffinCompound.getList("water_positions", Tag.TAG_STRING);
            
            Set<String> waterPositions = new HashSet<>();
            for (Tag waterTag : waterList) {
                waterPositions.add(waterTag.getAsString());
            }
            data.coffinInfectedWaterMap.put(coffinPos, waterPositions);
        }
        
        // 加载能量映射
        ListTag energyList = tag.getList("infected_water_energy", Tag.TAG_COMPOUND);
        for (Tag energyTag : energyList) {
            CompoundTag energyCompound = (CompoundTag) energyTag;
            String pos = energyCompound.getString("pos");
            int energy = energyCompound.getInt("energy");
            data.infectedWaterEnergyMap.put(pos, energy);
        }
        
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        // 保存棺材感染水映射
        ListTag coffinList = new ListTag();
        for (Map.Entry<String, Set<String>> entry : coffinInfectedWaterMap.entrySet()) {
            CompoundTag coffinCompound = new CompoundTag();
            coffinCompound.putString("coffin_pos", entry.getKey());
            
            ListTag waterList = new ListTag();
            for (String waterPos : entry.getValue()) {
                waterList.add(StringTag.valueOf(waterPos));
            }
            coffinCompound.put("water_positions", waterList);
            coffinList.add(coffinCompound);
        }
        tag.put("coffin_infected_water", coffinList);
        
        // 保存能量映射
        ListTag energyList = new ListTag();
        for (Map.Entry<String, Integer> entry : infectedWaterEnergyMap.entrySet()) {
            CompoundTag energyCompound = new CompoundTag();
            energyCompound.putString("pos", entry.getKey());
            energyCompound.putInt("energy", entry.getValue());
            energyList.add(energyCompound);
        }
        tag.put("infected_water_energy", energyList);
        
        return tag;
    }

    // 位置转换为字符串
    public static String posToString(net.minecraft.core.BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    // 字符串转换为位置
    public static net.minecraft.core.BlockPos stringToPos(String str) {
        String[] parts = str.split(",");
        return new net.minecraft.core.BlockPos(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2])
        );
    }

    // 添加感染水
    public void addInfectedWater(net.minecraft.core.BlockPos coffinPos, net.minecraft.core.BlockPos waterPos, int energy) {
        String coffinKey = posToString(coffinPos);
        String waterKey = posToString(waterPos);
        
        coffinInfectedWaterMap.computeIfAbsent(coffinKey, k -> new HashSet<>()).add(waterKey);
        infectedWaterEnergyMap.put(waterKey, energy);
        setDirty();
    }

    // 移除棺材的所有感染水
    public void removeCoffinInfections(net.minecraft.core.BlockPos coffinPos) {
        String coffinKey = posToString(coffinPos);
        Set<String> waterPositions = coffinInfectedWaterMap.remove(coffinKey);
        if (waterPositions != null) {
            for (String waterPos : waterPositions) {
                infectedWaterEnergyMap.remove(waterPos);
            }
        }
        setDirty();
    }

    // 检查水是否被感染
    public boolean isWaterInfected(net.minecraft.core.BlockPos pos) {
        String key = posToString(pos);
        return infectedWaterEnergyMap.containsKey(key) && infectedWaterEnergyMap.get(key) > 0;
    }

    // 获取水的能量
    public int getWaterEnergy(net.minecraft.core.BlockPos pos) {
        String key = posToString(pos);
        return infectedWaterEnergyMap.getOrDefault(key, 0);
    }

    // 设置水的能量
    public void setWaterEnergy(net.minecraft.core.BlockPos pos, int energy) {
        String key = posToString(pos);
        if (energy > 0) {
            infectedWaterEnergyMap.put(key, energy);
        } else {
            infectedWaterEnergyMap.remove(key);
        }
        setDirty();
    }

    // 获取相邻感染水的最高能量
    public int getMaxNeighborEnergy(net.minecraft.core.BlockPos pos) {
        int maxEnergy = 0;
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            net.minecraft.core.BlockPos neighbor = pos.relative(direction);
            int energy = getWaterEnergy(neighbor);
            if (energy > maxEnergy) {
                maxEnergy = energy;
            }
        }
        return maxEnergy;
    }

    // 静态方法：检查水是否被感染
    public static boolean isWaterInfectedStatic(ServerLevel level, net.minecraft.core.BlockPos pos) {
        InfectionData data = get(level);
        return data.isWaterInfected(pos);
    }

    // 静态方法：标记水为感染状态
    public static void markWaterInfectedStatic(ServerLevel level, net.minecraft.core.BlockPos pos) {
        InfectionData data = get(level);
        int maxEnergy = data.getMaxNeighborEnergy(pos);
        if (maxEnergy > 0) {
            // 如果有相邻的感染水，传递能量
            int newEnergy = maxEnergy - 1;
            if (newEnergy > 0) {
                data.setWaterEnergy(pos, newEnergy);
            }
        } else {
            // 如果没有相邻的感染水，直接设置为最大能量（用于尸水桶放置）
            data.setWaterEnergy(pos, MAX_ENERGY);
        }
    }
}