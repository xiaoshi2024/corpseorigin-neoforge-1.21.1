package com.phagens.corpseorigin.skill;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 技能节点实现
 */
public class SkillNode implements ISkillNode {
    
    private final ResourceLocation id;
    private final List<ISkill> skills;
    private final List<ResourceLocation> lockingNodes;
    private final List<ResourceLocation> parentIds;
    private final List<ResourceLocation> childIds;
    private final int tier;
    private final int x;
    private final int y;
    
    private SkillNode(Builder builder) {
        this.id = builder.id;
        this.skills = new ArrayList<>(builder.skills);
        this.lockingNodes = new ArrayList<>(builder.lockingNodes);
        this.parentIds = new ArrayList<>(builder.parentIds);
        this.childIds = new ArrayList<>(builder.childIds);
        this.tier = builder.tier;
        this.x = builder.x;
        this.y = builder.y;
    }
    
    @Override
    public ResourceLocation getId() {
        return id;
    }
    
    @Override
    public List<ISkill> getSkills() {
        return Collections.unmodifiableList(skills);
    }
    
    @Override
    public List<ResourceLocation> getLockingNodes() {
        return Collections.unmodifiableList(lockingNodes);
    }
    
    @Override
    public List<ResourceLocation> getParentIds() {
        return Collections.unmodifiableList(parentIds);
    }
    
    @Override
    public List<ResourceLocation> getChildIds() {
        return Collections.unmodifiableList(childIds);
    }
    
    @Override
    public int getTier() {
        return tier;
    }
    
    @Override
    public int getX() {
        return x;
    }
    
    @Override
    public int getY() {
        return y;
    }
    
    /**
     * 添加子节点
     */
    public void addChild(ResourceLocation childId) {
        if (!childIds.contains(childId)) {
            childIds.add(childId);
        }
    }
    
    /**
     * 构建器类
     */
    public static class Builder {
        private ResourceLocation id;
        private List<ISkill> skills = new ArrayList<>();
        private List<ResourceLocation> lockingNodes = new ArrayList<>();
        private List<ResourceLocation> parentIds = new ArrayList<>();
        private List<ResourceLocation> childIds = new ArrayList<>();
        private int tier = 0;
        private int x = 0;
        private int y = 0;
        
        public Builder(ResourceLocation id) {
            this.id = id;
        }
        
        public Builder skill(ISkill skill) {
            this.skills.add(skill);
            return this;
        }
        
        public Builder lockingNode(ResourceLocation nodeId) {
            this.lockingNodes.add(nodeId);
            return this;
        }
        
        public Builder parent(ResourceLocation parentId) {
            this.parentIds.add(parentId);
            return this;
        }
        
        public Builder child(ResourceLocation childId) {
            this.childIds.add(childId);
            return this;
        }
        
        public Builder tier(int tier) {
            this.tier = tier;
            return this;
        }
        
        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }
        
        public SkillNode build() {
            return new SkillNode(this);
        }
    }
}
