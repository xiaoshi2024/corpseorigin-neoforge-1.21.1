package com.phagens.corpseorigin.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 技能接口 - 定义尸兄技能的基本属性和行为
 *
 * 【功能说明】
 * 1. 定义技能的基本属性：ID、名称、描述、图标、消耗等
 * 2. 定义技能类型：被动/主动、进化类型
 * 3. 定义技能生命周期：学习、遗忘、激活
 * 4. 支持前置技能、等级要求、冷却时间等机制
 *
 * 【技能类型体系】
 * - BASIC_EVOLUTION: 基础进化 - 所有尸兄都能获得的基础能力
 * - POWER_MUTATION: 力量型变异 - 偏向物理攻击和防御
 * - AGILITY_MUTATION: 敏捷型变异 - 偏向速度和闪避
 * - SPECIAL_MUTATION: 特殊型变异 - 特殊能力如毒素、再生等
 * - DIVINE_ABILITY: 神级能力 - 高级进化能力
 * - SUPREME_ABILITY: 超神级能力 - 终极能力
 *
 * 【技能分类】
 * - 被动技能(isPassive=true): 自动生效，无需手动激活
 * - 主动技能(isActivatable=true): 需要手动触发，有冷却时间
 *
 * 【关联系统】
 * - BaseSkill: 接口的基础实现类
 * - CorpseSkills: 具体的技能定义
 * - ISkillHandler: 管理玩家的技能状态
 *
 * @author Phagens
 * @version 1.0
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
