package com.phagens.corpseorigin.skill;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 技能节点接口 - 技能树中的节点
 *
 * 【功能说明】
 * 1. 表示技能树中的一个节点，可包含多个技能
 * 2. 支持节点间的父子关系（前置/后置）
 * 3. 支持互斥锁定（选择此节点会锁定其他节点）
 * 4. 包含UI显示位置信息
 *
 * 【节点关系】
 * - 父节点(parentIds): 学习此节点需要先学习的节点
 * - 子节点(childIds): 学习此节点后解锁的节点
 * - 锁定节点(lockingNodes): 选择此节点会锁定的互斥节点
 *
 * 【使用场景】
 * - 技能树的分支选择（如力量/敏捷/特殊路线）
 * - 技能升级路径（基础->进阶->终极）
 * - 互斥技能组（选择A就不能选择B）
 *
 * 【关联系统】
 * - SkillNode: 接口的实现类
 * - ISkillTree: 包含多个节点形成技能树
 * - CorpseSkillTree: 具体的技能树定义
 *
 * @author Phagens
 * @version 1.0
 */
public interface ISkillNode {
    
    /**
     * 获取节点ID
     */
    ResourceLocation getId();
    
    /**
     * 获取节点包含的技能
     */
    List<ISkill> getSkills();
    
    /**
     * 检查节点是否包含特定技能
     */
    default boolean containsSkill(ISkill skill) {
        return getSkills().contains(skill);
    }
    
    /**
     * 获取互斥锁定节点（选择此节点会锁定的其他节点）
     */
    List<ResourceLocation> getLockingNodes();
    
    /**
     * 检查此节点是否与另一个节点互斥
     */
    default boolean isMutuallyExclusiveWith(ISkillNode other) {
        return getLockingNodes().contains(other.getId()) || 
               other.getLockingNodes().contains(getId());
    }
    
    /**
     * 获取父节点ID
     */
    List<ResourceLocation> getParentIds();
    
    /**
     * 获取子节点ID
     */
    List<ResourceLocation> getChildIds();
    
    /**
     * 获取节点在树中的层级
     */
    int getTier();
    
    /**
     * 获取节点位置X坐标（用于UI显示）
     */
    int getX();
    
    /**
     * 获取节点位置Y坐标（用于UI显示）
     */
    int getY();
}
