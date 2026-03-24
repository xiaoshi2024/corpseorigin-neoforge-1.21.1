package com.phagens.corpseorigin.skill;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 尸兄进化技能树构建类
 *
 * 【功能说明】
 * 1. 构建完整的尸兄技能进化树
 * 2. 定义技能节点之间的父子关系
 * 3. 组织技能的分层结构（0-5层）
 * 4. 注册技能树和节点到技能管理器
 *
 * 【技能树结构】
 * 第0层：根节点（基础尸兄状态）
 * 第1层：基础进化（硬化皮肤、利爪、吞噬强化、进化感知）
 * 第2层：分支进化（巨力、疾行、再生、毒液）
 * 第3层：进阶技能（狂暴、重击、跳跃、闪避、恐惧光环）
 * 第4层：神级能力（不死之身）
 * 第5层：终极能力（尸王之力、影袭）
 *
 * 【分支路线】
 * - 力量分支：利爪 -> 巨力 -> 狂暴/重击 -> 尸王之力
 * - 敏捷分支：疾行 -> 跳跃/闪避 -> 影袭
 * - 特殊分支：再生 -> 不死之身 | 毒液 -> 恐惧光环
 *
 * 【节点关系】
 * - 每个节点可以有多个父节点（多前置要求）
 * - 每个节点可以有多个子节点（分支展开）
 * - 节点位置(x,y)用于UI显示布局
 *
 * 【关联系统】
 * - CorpseSkills: 技能定义
 * - SkillTree/SkillNode: 技能树和节点实现
 * - SkillManager: 技能树注册管理
 *
 * @author Phagens
 * @version 1.0
 */
public class CorpseSkillTree {
    
    private static final String MODID = CorpseOrigin.MODID;
    
    /**
     * 创建并注册尸兄进化技能树
     */
    public static void init() {
        // 创建技能树
        SkillTree.Builder treeBuilder = new SkillTree.Builder(id("corpse_evolution"))
                .name(Component.translatable("skilltree.corpseorigin.corpse_evolution"))
                .description(Component.translatable("skilltree.corpseorigin.corpse_evolution.desc"))
                .icon(new ItemStack(Items.ROTTEN_FLESH))
                .treeType(ISkillTree.TreeType.CORPSE_EVOLUTION)
                .unlockCondition(Component.translatable("skilltree.corpseorigin.corpse_evolution.condition"));
        
        // ==================== 第0层：根节点（基础尸兄）====================
        SkillNode rootNode = new SkillNode.Builder(id("root"))
                .tier(0)
                .position(0, 0)
                .build();
        
        treeBuilder.rootNode(rootNode);
        
        // ==================== 第1层：基础进化 ====================
        // 硬化皮肤 - 左
        SkillNode hardenedSkinNode = new SkillNode.Builder(id("node_hardened_skin"))
                .tier(1)
                .position(-2, 1)
                .skill(CorpseSkills.HARDENED_SKIN)
                .parent(rootNode.getId())
                .build();
        
        // 锐利爪牙 - 中左
        SkillNode sharpClawsNode = new SkillNode.Builder(id("node_sharp_claws"))
                .tier(1)
                .position(-1, 1)
                .skill(CorpseSkills.SHARP_CLAWS)
                .parent(rootNode.getId())
                .build();
        
        // 吞噬强化 - 中右
        SkillNode devourNode = new SkillNode.Builder(id("node_devour"))
                .tier(1)
                .position(0, 1)
                .skill(CorpseSkills.DEVOUR_ENHANCEMENT)
                .parent(rootNode.getId())
                .build();
        
        // 进化感知 - 右
        SkillNode senseNode = new SkillNode.Builder(id("node_sense"))
                .tier(1)
                .position(1, 1)
                .skill(CorpseSkills.EVOLUTION_SENSE)
                .parent(rootNode.getId())
                .build();
        
        // ==================== 第2层：分支进化 ====================
        // 力量分支
        SkillNode giantStrengthNode = new SkillNode.Builder(id("node_giant_strength"))
                .tier(2)
                .position(-2, 2)
                .skill(CorpseSkills.GIANT_STRENGTH)
                .parent(sharpClawsNode.getId())
                .build();
        
        // 敏捷分支
        SkillNode swiftNode = new SkillNode.Builder(id("node_swift"))
                .tier(2)
                .position(0, 2)
                .skill(CorpseSkills.SWIFT_MOVEMENT)
                .parent(rootNode.getId())
                .build();
        
        // 特殊分支 - 再生
        SkillNode regenNode = new SkillNode.Builder(id("node_regen"))
                .tier(2)
                .position(2, 2)
                .skill(CorpseSkills.REGENERATION)
                .parent(rootNode.getId())
                .build();
        
        // 特殊分支 - 毒液
        SkillNode venomNode = new SkillNode.Builder(id("node_venom"))
                .tier(2)
                .position(3, 2)
                .skill(CorpseSkills.VENOM)
                .parent(rootNode.getId())
                .build();
        
        // ==================== 第3层：进阶技能 ====================
        // 力量 - 狂暴
        SkillNode berserkNode = new SkillNode.Builder(id("node_berserk"))
                .tier(3)
                .position(-2, 3)
                .skill(CorpseSkills.BERSERK)
                .parent(giantStrengthNode.getId())
                .build();
        
        // 力量 - 重击
        SkillNode heavyStrikeNode = new SkillNode.Builder(id("node_heavy_strike"))
                .tier(3)
                .position(-1, 3)
                .skill(CorpseSkills.HEAVY_STRIKE)
                .parent(giantStrengthNode.getId())
                .build();
        
        // 敏捷 - 跳跃
        SkillNode leapNode = new SkillNode.Builder(id("node_leap"))
                .tier(3)
                .position(0, 3)
                .skill(CorpseSkills.LEAP)
                .parent(swiftNode.getId())
                .build();
        
        // 敏捷 - 闪避
        SkillNode evasionNode = new SkillNode.Builder(id("node_evasion"))
                .tier(3)
                .position(1, 3)
                .skill(CorpseSkills.EVASION)
                .parent(swiftNode.getId())
                .build();
        
        // 特殊 - 恐惧光环
        SkillNode fearNode = new SkillNode.Builder(id("node_fear"))
                .tier(3)
                .position(3, 3)
                .skill(CorpseSkills.FEAR_AURA)
                .parent(venomNode.getId())
                .build();
        
        // 特殊 - 伪装
        SkillNode disguiseNode = new SkillNode.Builder(id("node_disguise"))
                .tier(3)
                .position(4, 3)
                .skill(CorpseSkills.DISGUISE)
                .parent(rootNode.getId())
                .build();
        
        // ==================== 第4层：神级能力 ====================
        // 不死之身
        SkillNode immortalNode = new SkillNode.Builder(id("node_immortal"))
                .tier(4)
                .position(2, 4)
                .skill(CorpseSkills.IMMORTAL_BODY)
                .parent(regenNode.getId())
                .build();
        
        // ==================== 第5层：终极能力 ====================
        // 尸王之力
        SkillNode corpseKingNode = new SkillNode.Builder(id("node_corpse_king"))
                .tier(5)
                .position(-1, 5)
                .skill(CorpseSkills.CORPSE_KING_POWER)
                .parent(berserkNode.getId())
                .parent(heavyStrikeNode.getId())
                .build();
        
        // 影袭
        SkillNode shadowNode = new SkillNode.Builder(id("node_shadow"))
                .tier(5)
                .position(0, 5)
                .skill(CorpseSkills.SHADOW_STRIKE)
                .parent(evasionNode.getId())
                .parent(leapNode.getId())
                .build();
        
        // 添加所有节点到技能树
        treeBuilder.node(hardenedSkinNode)
                .node(sharpClawsNode)
                .node(devourNode)
                .node(senseNode)
                .node(giantStrengthNode)
                .node(swiftNode)
                .node(regenNode)
                .node(venomNode)
                .node(berserkNode)
                .node(heavyStrikeNode)
                .node(leapNode)
                .node(evasionNode)
                .node(fearNode)
                .node(disguiseNode)
                .node(immortalNode)
                .node(corpseKingNode)
                .node(shadowNode);
        
        // 构建并注册技能树
        SkillTree skillTree = treeBuilder.build();
        SkillManager.getInstance().registerTree(skillTree);
        
        // 注册所有节点
        for (ISkillNode node : skillTree.getAllNodes()) {
            SkillManager.getInstance().registerNode(node);
        }
        
        CorpseOrigin.LOGGER.info("尸兄进化技能树已初始化，共 {} 个节点", skillTree.getAllNodes().size());
    }
    
    /**
     * 创建资源位置
     */
    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
