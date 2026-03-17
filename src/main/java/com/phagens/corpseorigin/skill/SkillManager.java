package com.phagens.corpseorigin.skill;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 技能管理器 - 管理所有技能的注册和查询
 * 单例模式
 */
public class SkillManager {
    
    private static SkillManager INSTANCE;
    
    private final Map<ResourceLocation, ISkill> skills = new HashMap<>();
    private final Map<ResourceLocation, ISkillNode> nodes = new HashMap<>();
    private final Map<ResourceLocation, ISkillTree> trees = new HashMap<>();
    
    // 按类型分类的技能
    private final Map<ISkill.SkillType, List<ISkill>> skillsByType = new EnumMap<>(ISkill.SkillType.class);
    
    private SkillManager() {
        // 初始化技能类型列表
        for (ISkill.SkillType type : ISkill.SkillType.values()) {
            skillsByType.put(type, new ArrayList<>());
        }
    }
    
    /**
     * 获取技能管理器实例
     */
    public static SkillManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SkillManager();
        }
        return INSTANCE;
    }
    
    /**
     * 注册技能
     */
    public void registerSkill(ISkill skill) {
        if (skills.containsKey(skill.getId())) {
            CorpseOrigin.LOGGER.warn("技能 {} 已被注册，将被覆盖", skill.getId());
        }
        skills.put(skill.getId(), skill);
        skillsByType.get(skill.getSkillType()).add(skill);
        CorpseOrigin.LOGGER.debug("注册技能: {}", skill.getId());
    }
    
    /**
     * 注册技能节点
     */
    public void registerNode(ISkillNode node) {
        if (nodes.containsKey(node.getId())) {
            CorpseOrigin.LOGGER.warn("技能节点 {} 已被注册，将被覆盖", node.getId());
        }
        nodes.put(node.getId(), node);
        CorpseOrigin.LOGGER.debug("注册技能节点: {}", node.getId());
    }
    
    /**
     * 注册技能树
     */
    public void registerTree(ISkillTree tree) {
        if (trees.containsKey(tree.getId())) {
            CorpseOrigin.LOGGER.warn("技能树 {} 已被注册，将被覆盖", tree.getId());
        }
        trees.put(tree.getId(), tree);
        CorpseOrigin.LOGGER.debug("注册技能树: {}", tree.getId());
    }
    
    /**
     * 获取技能
     */
    @Nullable
    public ISkill getSkill(ResourceLocation id) {
        return skills.get(id);
    }
    
    /**
     * 获取技能节点
     */
    @Nullable
    public ISkillNode getNode(ResourceLocation id) {
        return nodes.get(id);
    }
    
    /**
     * 获取技能树
     */
    @Nullable
    public ISkillTree getTree(ResourceLocation id) {
        return trees.get(id);
    }
    
    /**
     * 获取所有技能
     */
    public Collection<ISkill> getAllSkills() {
        return Collections.unmodifiableCollection(skills.values());
    }
    
    /**
     * 获取所有技能节点
     */
    public Collection<ISkillNode> getAllNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }
    
    /**
     * 获取所有技能树
     */
    public Collection<ISkillTree> getAllTrees() {
        return Collections.unmodifiableCollection(trees.values());
    }
    
    /**
     * 获取特定类型的所有技能
     */
    public List<ISkill> getSkillsByType(ISkill.SkillType type) {
        return Collections.unmodifiableList(skillsByType.getOrDefault(type, Collections.emptyList()));
    }
    
    /**
     * 获取特定类型的所有技能树
     */
    public List<ISkillTree> getTreesByType(ISkillTree.TreeType type) {
        return trees.values().stream()
                .filter(tree -> tree.getTreeType() == type)
                .toList();
    }
    
    /**
     * 检查技能是否存在
     */
    public boolean hasSkill(ResourceLocation id) {
        return skills.containsKey(id);
    }
    
    /**
     * 检查技能节点是否存在
     */
    public boolean hasNode(ResourceLocation id) {
        return nodes.containsKey(id);
    }
    
    /**
     * 检查技能树是否存在
     */
    public boolean hasTree(ResourceLocation id) {
        return trees.containsKey(id);
    }
    
    /**
     * 获取玩家技能处理器
     */
    public ISkillHandler getSkillHandler(Player player) {
        // 从玩家数据附件中获取或创建
        return SkillHandler.getOrCreate(player);
    }
    
    /**
     * 清除所有注册的数据（主要用于测试）
     */
    public void clear() {
        skills.clear();
        nodes.clear();
        trees.clear();
        skillsByType.values().forEach(List::clear);
        CorpseOrigin.LOGGER.info("技能管理器已清除所有数据");
    }
    
    /**
     * 获取尸兄进化技能树
     */
    @Nullable
    public ISkillTree getCorpseEvolutionTree() {
        return trees.values().stream()
                .filter(tree -> tree.getTreeType() == ISkillTree.TreeType.CORPSE_EVOLUTION)
                .findFirst()
                .orElse(null);
    }
}
