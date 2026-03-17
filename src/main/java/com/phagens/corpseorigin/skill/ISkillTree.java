package com.phagens.corpseorigin.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 技能树接口 - 定义完整的技能进化路线
 * 参考《尸兄》中的进化体系
 */
public interface ISkillTree {
    
    /**
     * 获取技能树ID
     */
    ResourceLocation getId();
    
    /**
     * 获取技能树名称
     */
    Component getName();
    
    /**
     * 获取技能树描述
     */
    Component getDescription();
    
    /**
     * 获取技能树图标
     */
    ItemStack getIcon();
    
    /**
     * 获取根节点
     */
    ISkillNode getRootNode();
    
    /**
     * 获取所有节点
     */
    List<ISkillNode> getAllNodes();
    
    /**
     * 根据ID获取节点
     */
    ISkillNode getNode(ResourceLocation id);
    
    /**
     * 获取特定层级的所有节点
     */
    List<ISkillNode> getNodesByTier(int tier);
    
    /**
     * 获取技能树的最大层级
     */
    int getMaxTier();
    
    /**
     * 获取技能树类型
     */
    TreeType getTreeType();
    
    /**
     * 检查技能是否属于此树
     */
    boolean containsSkill(ISkill skill);
    
    /**
     * 获取解锁此技能树所需的条件描述
     */
    Component getUnlockCondition();
    
    /**
     * 技能树类型枚举
     */
    enum TreeType {
        // 尸兄进化树 - 感染变异路线
        CORPSE_EVOLUTION("corpse_evolution", "尸兄进化"),
        
        // 修仙路线 - 人类修炼路线（未来扩展）
        CULTIVATION("cultivation", "修仙之道"),
        
        // 混合路线 - 尸兄修仙混合
        HYBRID("hybrid", "混元之道");
        
        private final String id;
        private final String displayName;
        
        TreeType(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public String getId() {
            return id;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
