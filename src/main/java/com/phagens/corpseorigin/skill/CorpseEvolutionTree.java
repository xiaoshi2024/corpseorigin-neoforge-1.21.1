package com.phagens.corpseorigin.skill;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.register.Moditems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 尸兄进化技能树构建器
 * 构建完整的尸兄进化路线
 */
public class CorpseEvolutionTree {
    
    public static final ResourceLocation TREE_ID = ResourceLocation.fromNamespaceAndPath(
            CorpseOrigin.MODID, "corpse_evolution");
    
    private static ISkillTree instance;
    
    /**
     * 获取或创建尸兄进化技能树
     */
    public static ISkillTree getTree() {
        if (instance == null) {
            instance = buildTree();
        }
        return instance;
    }
    
    /**
     * 构建技能树
     */
    private static ISkillTree buildTree() {
        CorpseOrigin.LOGGER.info("构建尸兄进化技能树...");
        
        // 创建根节点
        SkillNode rootNode = new SkillNode.Builder(id("root"))
                .tier(0)
                .position(0, 0)
                .build();
        
        // 创建技能树
        SkillTree.Builder treeBuilder = new SkillTree.Builder(TREE_ID)
                .name(Component.translatable("skilltree.corpseorigin.corpse_evolution"))
                .description(Component.translatable("skilltree.corpseorigin.corpse_evolution.desc"))
                .icon(new ItemStack(Moditems.NULL_S_AGENT.get()))
                .rootNode(rootNode)
                .treeType(ISkillTree.TreeType.CORPSE_EVOLUTION)
                .unlockCondition(Component.translatable("skilltree.corpseorigin.corpse_evolution.condition"));
        
        // ========== 第1层：基础进化 ==========
        SkillNode basicNode1 = new SkillNode.Builder(id("basic_tier1"))
                .tier(1)
                .position(-100, 50)
                .parent(rootNode.getId())
                .skill(CorpseSkills.HARDENED_SKIN)
                .skill(CorpseSkills.SHARP_CLAWS)
                .build();
        
        treeBuilder.node(basicNode1);
        
        // ========== 第2层：基础进化进阶 ==========
        SkillNode basicNode2 = new SkillNode.Builder(id("basic_tier2"))
                .tier(2)
                .position(-100, 100)
                .parent(basicNode1.getId())
                .skill(CorpseSkills.DEVOUR_ENHANCEMENT)
                .skill(CorpseSkills.EVOLUTION_SENSE)
                .skill(CorpseSkills.SWIFT_MOVEMENT)
                .skill(CorpseSkills.REGENERATION)
                .build();
        
        treeBuilder.node(basicNode2);
        
        // ========== 第3层：分支选择 ==========
        
        // 力量分支
        SkillNode powerNode1 = new SkillNode.Builder(id("power_tier1"))
                .tier(3)
                .position(-200, 150)
                .parent(basicNode1.getId())
                .skill(CorpseSkills.GIANT_STRENGTH)
                .build();
        
        treeBuilder.node(powerNode1);
        
        // 敏捷分支
        SkillNode agilityNode1 = new SkillNode.Builder(id("agility_tier1"))
                .tier(3)
                .position(0, 150)
                .parent(basicNode2.getId())
                .skill(CorpseSkills.LEAP)
                .build();
        
        treeBuilder.node(agilityNode1);
        
        // 特殊分支
        SkillNode specialNode1 = new SkillNode.Builder(id("special_tier1"))
                .tier(3)
                .position(100, 150)
                .parent(basicNode2.getId())
                .skill(CorpseSkills.VENOM)
                .build();
        
        treeBuilder.node(specialNode1);
        
        // ========== 第4层：分支进阶 ==========
        
        // 力量进阶
        SkillNode powerNode2 = new SkillNode.Builder(id("power_tier2"))
                .tier(4)
                .position(-200, 200)
                .parent(powerNode1.getId())
                .skill(CorpseSkills.BERSERK)
                .skill(CorpseSkills.HEAVY_STRIKE)
                .build();
        
        treeBuilder.node(powerNode2);
        
        // 敏捷进阶
        SkillNode agilityNode2 = new SkillNode.Builder(id("agility_tier2"))
                .tier(4)
                .position(0, 200)
                .parent(agilityNode1.getId())
                .skill(CorpseSkills.EVASION)
                .build();
        
        treeBuilder.node(agilityNode2);
        
        // 特殊进阶
        SkillNode specialNode2 = new SkillNode.Builder(id("special_tier2"))
                .tier(4)
                .position(100, 200)
                .parent(specialNode1.getId())
                .skill(CorpseSkills.FEAR_AURA)
                .build();
        
        treeBuilder.node(specialNode2);
        
        // ========== 第5层：终极能力 ==========
        
        // 力量终极
        SkillNode powerUltimate = new SkillNode.Builder(id("power_ultimate"))
                .tier(5)
                .position(-200, 250)
                .parent(powerNode2.getId())
                .skill(CorpseSkills.CORPSE_KING_POWER)
                .build();
        
        treeBuilder.node(powerUltimate);
        
        // 敏捷终极
        SkillNode agilityUltimate = new SkillNode.Builder(id("agility_ultimate"))
                .tier(5)
                .position(0, 250)
                .parent(agilityNode2.getId())
                .skill(CorpseSkills.SHADOW_STRIKE)
                .build();
        
        treeBuilder.node(agilityUltimate);
        
        // 特殊终极
        SkillNode specialUltimate = new SkillNode.Builder(id("special_ultimate"))
                .tier(5)
                .position(100, 250)
                .parent(specialNode2.getId())
                .skill(CorpseSkills.IMMORTAL_BODY)
                .build();
        
        treeBuilder.node(specialUltimate);
        
        ISkillTree tree = treeBuilder.build();
        
        // 注册到技能管理器
        SkillManager.getInstance().registerTree(tree);
        
        CorpseOrigin.LOGGER.info("尸兄进化技能树构建完成，共 {} 个节点", tree.getAllNodes().size());
        
        return tree;
    }
    
    /**
     * 创建资源位置
     */
    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "node_" + path);
    }
    
    /**
     * 初始化技能树
     * 注意：这个方法应该在所有技能注册完成后调用
     */
    public static void init() {
        // 延迟到第一次访问时构建，确保所有技能已注册
        CorpseOrigin.LOGGER.info("尸兄进化技能树初始化完成（延迟加载）");
    }
}
