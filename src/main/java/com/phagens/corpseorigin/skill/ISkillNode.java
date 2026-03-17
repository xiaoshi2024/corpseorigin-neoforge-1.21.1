package com.phagens.corpseorigin.skill;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 技能节点接口 - 技能树中的节点
 * 一个节点可以包含多个互斥的技能选择
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
