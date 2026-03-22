package com.phagens.corpseorigin.skill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;

/**
 * 技能处理器接口 - 管理玩家的技能状态
 *
 * 【功能说明】
 * 1. 管理玩家已学习的技能集合
 * 2. 管理进化点数（学习技能消耗/遗忘返还）
 * 3. 管理技能冷却时间
 * 4. 处理技能激活和停用
 * 5. 提供技能学习条件检查
 *
 * 【数据持久化】
 * - 使用Attachment系统保存玩家数据
 * - 支持死亡后保留（copyOnDeath）
 * - 需要客户端-服务器同步
 *
 * 【学习条件检查】
 * - 玩家必须是尸兄身份
 * - 满足等级要求
 * - 满足前置技能要求
 * - 进化点数充足
 * - 不与已学技能互斥
 *
 * 【关联系统】
 * - SkillHandler: 接口的实现类
 * - SkillAttachment: 数据附件管理
 * - CorpseSkills: 技能定义
 *
 * @author Phagens
 * @version 1.0
 */
public interface ISkillHandler {
    
    /**
     * 获取关联的玩家
     */
    Player getPlayer();
    
    /**
     * 获取玩家已学习的所有技能
     */
    Set<ISkill> getLearnedSkills();
    
    /**
     * 获取玩家已学习的技能ID集合
     */
    Set<ResourceLocation> getLearnedSkillIds();
    
    /**
     * 检查玩家是否已学习特定技能
     */
    boolean hasLearned(ISkill skill);
    
    /**
     * 检查玩家是否已学习特定技能（通过ID）
     */
    boolean hasLearned(ResourceLocation skillId);
    
    /**
     * 尝试学习技能
     * @return 学习结果
     */
    LearnResult learnSkill(ISkill skill);
    
    /**
     * 尝试学习技能（通过ID）
     */
    LearnResult learnSkill(ResourceLocation skillId);
    
    /**
     * 遗忘技能
     */
    boolean forgetSkill(ISkill skill);
    
    /**
     * 遗忘技能（通过ID）
     */
    boolean forgetSkill(ResourceLocation skillId);
    
    /**
     * 遗忘所有技能
     */
    void forgetAllSkills();
    
    /**
     * 检查是否可以学习技能
     */
    boolean canLearn(ISkill skill);
    
    /**
     * 获取剩余进化点数
     */
    int getEvolutionPoints();
    
    /**
     * 获取已使用的进化点数
     */
    int getUsedEvolutionPoints();
    
    /**
     * 获取总进化点数
     */
    int getTotalEvolutionPoints();
    
    /**
     * 增加进化点数
     */
    void addEvolutionPoints(int points);
    
    /**
     * 消耗进化点数
     */
    boolean consumeEvolutionPoints(int points);
    
    /**
     * 设置进化点数
     */
    void setEvolutionPoints(int points);
    
    /**
     * 获取当前激活的技能
     */
    List<ISkill> getActiveSkills();
    
    /**
     * 激活技能
     */
    boolean activateSkill(ISkill skill);
    
    /**
     * 激活技能（通过ID）
     */
    boolean activateSkill(ResourceLocation skillId);
    
    /**
     * 获取技能冷却剩余时间
     */
    int getCooldownRemaining(ISkill skill);
    
    /**
     * 获取技能冷却剩余时间（通过ID）
     */
    int getCooldownRemaining(ResourceLocation skillId);
    
    /**
     * 设置技能冷却
     */
    void setCooldown(ISkill skill, int cooldown);
    
    /**
     * 检查技能是否在冷却中
     */
    boolean isOnCooldown(ISkill skill);
    
    /**
     * 获取学习技能失败的原因
     */
    String getCannotLearnReason(ISkill skill);
    
    /**
     * 同步技能数据到客户端
     */
    void syncToClient();
    
    /**
     * 从NBT加载技能数据
     */
    void loadFromNBT(net.minecraft.nbt.CompoundTag tag);
    
    /**
     * 保存技能数据到NBT
     */
    net.minecraft.nbt.CompoundTag saveToNBT();
    
    /**
     * 学习结果枚举
     */
    enum LearnResult {
        SUCCESS("成功学习技能"),
        ALREADY_LEARNED("已经学习过此技能"),
        INSUFFICIENT_POINTS("进化点数不足"),
        MISSING_PREREQUISITES("未满足前置技能要求"),
        LEVEL_TOO_LOW("等级不足"),
        MUTUALLY_EXCLUSIVE("与已学技能互斥"),
        NOT_CORPSE("只有尸兄可以学习此技能"),
        UNKNOWN_SKILL("未知技能"),
        ERROR("学习时发生错误");
        
        private final String message;
        
        LearnResult(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }
}
