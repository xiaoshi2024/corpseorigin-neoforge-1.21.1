package com.phagens.corpseorigin.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 技能树接口 - 定义完整的技能进化路线
 *
 * 【功能说明】
 * 1. 定义完整的技能进化体系，包含多个技能节点
 * 2. 支持多种进化路线（尸兄/修仙/混合）
 * 3. 提供技能树的UI展示信息（名称、描述、图标）
 * 4. 管理技能节点的层级关系
 *
 * 【技能树类型】
 * - CORPSE_EVOLUTION: 尸兄进化路线 - 感染变异体系
 * - CULTIVATION: 修仙路线 - 人类修炼体系（未来扩展）
 * - HYBRID: 混合路线 - 尸兄修仙混合体系（未来扩展）
 *
 * 【数据结构】
 * - 根节点(rootNode): 技能树的起点
 * - 节点集合: 包含技能树中的所有节点
 * - 层级系统: 节点按层级组织，表示进化阶段
 *
 * 【关联系统】
 * - SkillTree: 接口的实现类
 * - CorpseSkillTree/CorpseEvolutionTree: 具体的技能树定义
 * - ISkillNode: 技能树的组成单元
 *
 * @author Phagens
 * @version 1.0
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
