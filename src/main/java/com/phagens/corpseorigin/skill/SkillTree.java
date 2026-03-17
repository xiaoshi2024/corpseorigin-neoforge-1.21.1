package com.phagens.corpseorigin.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 技能树实现
 */
public class SkillTree implements ISkillTree {
    
    private final ResourceLocation id;
    private final Component name;
    private final Component description;
    private final ItemStack icon;
    private final ISkillNode rootNode;
    private final Map<ResourceLocation, ISkillNode> nodes;
    private final TreeType treeType;
    private final Component unlockCondition;
    
    public SkillTree(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.icon = builder.icon;
        this.rootNode = builder.rootNode;
        this.nodes = new HashMap<>(builder.nodes);
        this.treeType = builder.treeType;
        this.unlockCondition = builder.unlockCondition;
    }
    
    @Override
    public ResourceLocation getId() {
        return id;
    }
    
    @Override
    public Component getName() {
        return name;
    }
    
    @Override
    public Component getDescription() {
        return description;
    }
    
    @Override
    public ItemStack getIcon() {
        return icon.copy();
    }
    
    @Override
    public ISkillNode getRootNode() {
        return rootNode;
    }
    
    @Override
    public List<ISkillNode> getAllNodes() {
        return Collections.unmodifiableList(new ArrayList<>(nodes.values()));
    }
    
    @Override
    public ISkillNode getNode(ResourceLocation id) {
        return nodes.get(id);
    }
    
    @Override
    public List<ISkillNode> getNodesByTier(int tier) {
        return nodes.values().stream()
                .filter(node -> node.getTier() == tier)
                .toList();
    }
    
    @Override
    public int getMaxTier() {
        return nodes.values().stream()
                .mapToInt(ISkillNode::getTier)
                .max()
                .orElse(0);
    }
    
    @Override
    public TreeType getTreeType() {
        return treeType;
    }
    
    @Override
    public boolean containsSkill(ISkill skill) {
        for (ISkillNode node : nodes.values()) {
            if (node.containsSkill(skill)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Component getUnlockCondition() {
        return unlockCondition;
    }
    
    /**
     * 获取节点的父节点
     */
    public List<ISkillNode> getParentNodes(ISkillNode node) {
        List<ISkillNode> parents = new ArrayList<>();
        for (ResourceLocation parentId : node.getParentIds()) {
            ISkillNode parent = nodes.get(parentId);
            if (parent != null) {
                parents.add(parent);
            }
        }
        return parents;
    }
    
    /**
     * 获取节点的子节点
     */
    public List<ISkillNode> getChildNodes(ISkillNode node) {
        List<ISkillNode> children = new ArrayList<>();
        for (ResourceLocation childId : node.getChildIds()) {
            ISkillNode child = nodes.get(childId);
            if (child != null) {
                children.add(child);
            }
        }
        return children;
    }
    
    /**
     * 构建器类
     */
    public static class Builder {
        private ResourceLocation id;
        private Component name = Component.empty();
        private Component description;
        private ItemStack icon = ItemStack.EMPTY;
        private ISkillNode rootNode;
        private Map<ResourceLocation, ISkillNode> nodes = new HashMap<>();
        private TreeType treeType = TreeType.CORPSE_EVOLUTION;
        private Component unlockCondition = Component.empty();
        
        public Builder(ResourceLocation id) {
            this.id = id;
        }
        
        public Builder name(Component name) {
            this.name = name;
            return this;
        }
        
        public Builder description(Component description) {
            this.description = description;
            return this;
        }
        
        public Builder icon(ItemStack icon) {
            this.icon = icon;
            return this;
        }
        
        public Builder rootNode(ISkillNode rootNode) {
            this.rootNode = rootNode;
            this.nodes.put(rootNode.getId(), rootNode);
            return this;
        }
        
        public Builder node(ISkillNode node) {
            this.nodes.put(node.getId(), node);
            return this;
        }
        
        public Builder treeType(TreeType treeType) {
            this.treeType = treeType;
            return this;
        }
        
        public Builder unlockCondition(Component unlockCondition) {
            this.unlockCondition = unlockCondition;
            return this;
        }
        
        public SkillTree build() {
            return new SkillTree(this);
        }
    }
}
