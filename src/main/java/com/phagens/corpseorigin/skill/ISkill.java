package com.phagens.corpseorigin.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 技能接口 - 定义尸兄技能的基本属性和行为
 * 参考《尸兄》原作中的各种能力设定
 */
public interface ISkill {
    
    /**
     * 获取技能ID
     */
    ResourceLocation getId();
    
    /**
     * 获取技能名称
     */
    Component getName();
    
    /**
     * 获取技能描述
     */
    @Nullable
    Component getDescription();
    
    /**
     * 获取技能图标路径
     */
    ResourceLocation getIcon();
    
    /**
     * 获取技能消耗（进化点数）
     */
    int getCost();
    
    /**
     * 获取技能类型
     */
    SkillType getSkillType();
    
    /**
     * 获取技能等级要求
     */
    int getRequiredLevel();
    
    /**
     * 获取前置技能列表
     */
    List<ResourceLocation> getPrerequisites();
    
    /**
     * 检查玩家是否可以学习此技能
     */
    boolean canLearn(Player player);
    
    /**
     * 学习技能时的回调
     */
    void onLearn(Player player);
    
    /**
     * 遗忘技能时的回调
     */
    void onForget(Player player);
    
    /**
     * 技能是否被动（自动生效）
     */
    boolean isPassive();
    
    /**
     * 技能是否可激活（需要手动触发）
     */
    default boolean isActivatable() {
        return !isPassive();
    }
    
    /**
     * 激活技能（如果是可激活技能）
     */
    default void activate(Player player) {
        if (isActivatable()) {
            onActivate(player);
        }
    }
    
    /**
     * 激活技能的实现
     */
    void onActivate(Player player);
    
    /**
     * 获取技能冷却时间（tick）
     */
    default int getCooldown() {
        return 0;
    }
    
    /**
     * 获取技能持续时间（tick，-1表示永久）
     */
    default int getDuration() {
        return -1;
    }
    
    /**
     * 技能类型枚举
     */
    enum SkillType {
        // 基础进化 - 所有尸兄都能获得的基础能力
        BASIC_EVOLUTION("basic_evolution"),
        
        // 力量型变异 - 偏向物理攻击和防御
        POWER_MUTATION("power_mutation"),
        
        // 敏捷型变异 - 偏向速度和闪避
        AGILITY_MUTATION("agility_mutation"),
        
        // 特殊型变异 - 特殊能力如毒素、再生等
        SPECIAL_MUTATION("special_mutation"),
        
        // 神级能力 - 高级进化能力
        DIVINE_ABILITY("divine_ability"),
        
        // 超神级能力 - 终极能力
        SUPREME_ABILITY("supreme_ability");
        
        private final String name;
        
        SkillType(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
}
