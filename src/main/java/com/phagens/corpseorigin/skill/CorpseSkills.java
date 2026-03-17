package com.phagens.corpseorigin.skill;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * 尸兄技能定义
 * 基于《尸兄》原作中的各种能力
 */
public class CorpseSkills {
    
    // 技能注册表
    private static final Map<ResourceLocation, ISkill> SKILLS = new HashMap<>();
    
    // ==================== 基础进化技能 ====================
    
    /**
     * 硬化皮肤 - 增加护甲值
     */
    public static final ISkill HARDENED_SKIN = register(new SimpleSkill(
            new BaseSkill.Builder(id("hardened_skin"))
                    .name(Component.translatable("skill.corpseorigin.hardened_skin"))
                    .description(Component.translatable("skill.corpseorigin.hardened_skin.desc"))
                    .cost(1)
                    .skillType(ISkill.SkillType.BASIC_EVOLUTION)
                    .requiredLevel(1)
                    .passive(true)
                    .attributeModifier(Attributes.ARMOR, 
                            new AttributeModifier(id("hardened_skin"), 4.0, AttributeModifier.Operation.ADD_VALUE))
    ) {
        @Override
        public void onActivate(Player player) {
            // 被动技能，无需激活
        }
    });
    
    /**
     * 利爪 - 增加攻击伤害
     */
    public static final ISkill SHARP_CLAWS = register(new SimpleSkill(
            new BaseSkill.Builder(id("sharp_claws"))
                    .name(Component.translatable("skill.corpseorigin.sharp_claws"))
                    .description(Component.translatable("skill.corpseorigin.sharp_claws.desc"))
                    .cost(1)
                    .skillType(ISkill.SkillType.BASIC_EVOLUTION)
                    .requiredLevel(1)
                    .passive(true)
                    .attributeModifier(Attributes.ATTACK_DAMAGE, 
                            new AttributeModifier(id("sharp_claws"), 3.0, AttributeModifier.Operation.ADD_VALUE))
    ) {
        @Override
        public void onActivate(Player player) {
            // 被动技能
        }
    });
    
    /**
     * 吞噬强化 - 攻击时恢复更多生命值
     */
    public static final ISkill DEVOUR_ENHANCEMENT = register(new SimpleSkill(
            new BaseSkill.Builder(id("devour_enhancement"))
                    .name(Component.translatable("skill.corpseorigin.devour_enhancement"))
                    .description(Component.translatable("skill.corpseorigin.devour_enhancement.desc"))
                    .cost(2)
                    .skillType(ISkill.SkillType.BASIC_EVOLUTION)
                    .requiredLevel(2)
                    .passive(true)
    ) {
        @Override
        public void onActivate(Player player) {
            // 被动效果在攻击事件处理
        }
    });
    
    /**
     * 进化感知 - 获得夜视能力
     */
    public static final ISkill EVOLUTION_SENSE = register(new SimpleSkill(
            new BaseSkill.Builder(id("evolution_sense"))
                    .name(Component.translatable("skill.corpseorigin.evolution_sense"))
                    .description(Component.translatable("skill.corpseorigin.evolution_sense.desc"))
                    .cost(2)
                    .skillType(ISkill.SkillType.BASIC_EVOLUTION)
                    .requiredLevel(2)
                    .passive(true)
    ) {
        @Override
        public void onActivate(Player player) {
            // 被动效果在tick事件中处理
        }
    });
    
    // ==================== 力量型变异技能 ====================
    
    /**
     * 巨力 - 大幅增加攻击伤害
     */
    public static final ISkill GIANT_STRENGTH = register(new SimpleSkill(
            new BaseSkill.Builder(id("giant_strength"))
                    .name(Component.translatable("skill.corpseorigin.giant_strength"))
                    .description(Component.translatable("skill.corpseorigin.giant_strength.desc"))
                    .cost(3)
                    .skillType(ISkill.SkillType.POWER_MUTATION)
                    .requiredLevel(3)
                    .prerequisite(id("sharp_claws"))
                    .passive(true)
                    .attributeModifier(Attributes.ATTACK_DAMAGE, 
                            new AttributeModifier(id("giant_strength"), 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
    ) {
        @Override
        public void onActivate(Player player) {
            // 被动技能
        }
    });
    
    /**
     * 狂暴 - 临时增加攻击速度
     */
    public static final ISkill BERSERK = register(new SimpleSkill(
            new BaseSkill.Builder(id("berserk"))
                    .name(Component.translatable("skill.corpseorigin.berserk"))
                    .description(Component.translatable("skill.corpseorigin.berserk.desc"))
                    .cost(3)
                    .skillType(ISkill.SkillType.POWER_MUTATION)
                    .requiredLevel(3)
                    .prerequisite(id("giant_strength"))
                    .cooldown(600) // 30秒
                    .duration(200) // 10秒
    ) {
        @Override
        public void onActivate(Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
        }
    });
    
    /**
     * 重击 - 攻击带有击退效果
     */
    public static final ISkill HEAVY_STRIKE = register(new SimpleSkill(
            new BaseSkill.Builder(id("heavy_strike"))
                    .name(Component.translatable("skill.corpseorigin.heavy_strike"))
                    .description(Component.translatable("skill.corpseorigin.heavy_strike.desc"))
                    .cost(2)
                    .skillType(ISkill.SkillType.POWER_MUTATION)
                    .requiredLevel(4)
                    .prerequisite(id("giant_strength"))
                    .passive(true)
    ) {
        @Override
        public void onActivate(Player player) {
            // 被动效果在攻击事件中处理
        }
    });
    
    // ==================== 敏捷型变异技能 ====================
    
    /**
     * 疾行 - 增加移动速度
     */
    public static final ISkill SWIFT_MOVEMENT = register(new SimpleSkill(
            new BaseSkill.Builder(id("swift_movement"))
                    .name(Component.translatable("skill.corpseorigin.swift_movement"))
                    .description(Component.translatable("skill.corpseorigin.swift_movement.desc"))
                    .cost(2)
                    .skillType(ISkill.SkillType.AGILITY_MUTATION)
                    .requiredLevel(2)
                    .passive(true)
                    .attributeModifier(Attributes.MOVEMENT_SPEED, 
                            new AttributeModifier(id("swift_movement"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
    ) {
        @Override
        public void onActivate(Player player) {
            // 被动技能
        }
    });
    
    /**
     * 跳跃强化 - 增加跳跃高度
     */
    public static final ISkill LEAP = register(new SimpleSkill(
            new BaseSkill.Builder(id("leap"))
                    .name(Component.translatable("skill.corpseorigin.leap"))
                    .description(Component.translatable("skill.corpseorigin.leap.desc"))
                    .cost(2)
                    .skillType(ISkill.SkillType.AGILITY_MUTATION)
                    .requiredLevel(3)
                    .prerequisite(id("swift_movement"))
                    .passive(true)
    ) {
        @Override
        public void onActivate(Player player) {
            // 被动效果在跳跃事件中处理
        }
    });
    
    /**
     * 闪避 - 几率闪避攻击
     */
    public static final ISkill EVASION = register(new SimpleSkill(
            new BaseSkill.Builder(id("evasion"))
                    .name(Component.translatable("skill.corpseorigin.evasion"))
                    .description(Component.translatable("skill.corpseorigin.evasion.desc"))
                    .cost(3)
                    .skillType(ISkill.SkillType.AGILITY_MUTATION)
                    .requiredLevel(4)
                    .prerequisite(id("swift_movement"))
                    .passive(true)
    ) {
        @Override
        public void onActivate(Player player) {
            // 被动效果在受到伤害事件中处理
        }
    });
    
    // ==================== 特殊型变异技能 ====================
    
    /**
     * 毒液 - 攻击带毒
     */
    public static final ISkill VENOM = register(new SimpleSkill(
            new BaseSkill.Builder(id("venom"))
                    .name(Component.translatable("skill.corpseorigin.venom"))
                    .description(Component.translatable("skill.corpseorigin.venom.desc"))
                    .cost(3)
                    .skillType(ISkill.SkillType.SPECIAL_MUTATION)
                    .requiredLevel(3)
                    .passive(true)
    ) {
        @Override
        public void onActivate(Player player) {
            // 被动效果在攻击事件中处理
        }
    });
    
    /**
     * 快速再生 - 加快生命恢复
     */
    public static final ISkill REGENERATION = register(new SimpleSkill(
            new BaseSkill.Builder(id("regeneration"))
                    .name(Component.translatable("skill.corpseorigin.regeneration"))
                    .description(Component.translatable("skill.corpseorigin.regeneration.desc"))
                    .cost(2)
                    .skillType(ISkill.SkillType.SPECIAL_MUTATION)
                    .requiredLevel(2)
                    .passive(true)
    ) {
        @Override
        public void onActivate(Player player) {
            // 被动效果在tick事件中处理
        }
    });
    
    /**
     * 恐惧光环 - 使附近敌人恐惧
     */
    public static final ISkill FEAR_AURA = register(new SimpleSkill(
            new BaseSkill.Builder(id("fear_aura"))
                    .name(Component.translatable("skill.corpseorigin.fear_aura"))
                    .description(Component.translatable("skill.corpseorigin.fear_aura.desc"))
                    .cost(4)
                    .skillType(ISkill.SkillType.SPECIAL_MUTATION)
                    .requiredLevel(4)
                    .prerequisite(id("venom"))
                    .cooldown(400) // 20秒
                    .duration(100) // 5秒
    ) {
        @Override
        public void onActivate(Player player) {
            // 激活时给附近敌人添加虚弱和缓慢效果
            player.level().getEntities(player, player.getBoundingBox().inflate(5.0), 
                    entity -> entity instanceof net.minecraft.world.entity.LivingEntity && 
                             !(entity instanceof Player))
                    .forEach(entity -> {
                        if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
                        }
                    });
        }
    });
    
    // ==================== 神级能力 ====================
    
    /**
     * 不死之身 - 死亡时复活一次
     */
    public static final ISkill IMMORTAL_BODY = register(new SimpleSkill(
            new BaseSkill.Builder(id("immortal_body"))
                    .name(Component.translatable("skill.corpseorigin.immortal_body"))
                    .description(Component.translatable("skill.corpseorigin.immortal_body.desc"))
                    .cost(5)
                    .skillType(ISkill.SkillType.DIVINE_ABILITY)
                    .requiredLevel(5)
                    .prerequisite(id("regeneration"))
                    .cooldown(12000) // 10分钟
                    .passive(true)
    ) {
        @Override
        public void onActivate(Player player) {
            // 被动效果在死亡事件中处理
        }
    });
    
    /**
     * 尸王之力 - 终极力量技能
     */
    public static final ISkill CORPSE_KING_POWER = register(new SimpleSkill(
            new BaseSkill.Builder(id("corpse_king_power"))
                    .name(Component.translatable("skill.corpseorigin.corpse_king_power"))
                    .description(Component.translatable("skill.corpseorigin.corpse_king_power.desc"))
                    .cost(5)
                    .skillType(ISkill.SkillType.SUPREME_ABILITY)
                    .requiredLevel(5)
                    .prerequisites(id("berserk"), id("heavy_strike"))
                    .cooldown(1200) // 1分钟
                    .duration(300) // 15秒
    ) {
        @Override
        public void onActivate(Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
        }
    });
    
    /**
     * 影袭 - 终极敏捷技能
     */
    public static final ISkill SHADOW_STRIKE = register(new SimpleSkill(
            new BaseSkill.Builder(id("shadow_strike"))
                    .name(Component.translatable("skill.corpseorigin.shadow_strike"))
                    .description(Component.translatable("skill.corpseorigin.shadow_strike.desc"))
                    .cost(5)
                    .skillType(ISkill.SkillType.SUPREME_ABILITY)
                    .requiredLevel(5)
                    .prerequisites(id("evasion"), id("leap"))
                    .cooldown(600) // 30秒
    ) {
        @Override
        public void onActivate(Player player) {
            // 向前瞬移并对路径上的敌人造成伤害
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 2));
        }
    });
    
    /**
     * 注册技能
     */
    private static ISkill register(ISkill skill) {
        SKILLS.put(skill.getId(), skill);
        SkillManager.getInstance().registerSkill(skill);
        return skill;
    }
    
    /**
     * 创建资源位置
     */
    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, path);
    }
    
    /**
     * 获取所有技能
     */
    public static Map<ResourceLocation, ISkill> getAllSkills() {
        return new HashMap<>(SKILLS);
    }
    
    /**
     * 简单技能实现类
     */
    public static abstract class SimpleSkill extends BaseSkill {
        public SimpleSkill(Builder builder) {
            super(builder);
        }
    }
    
    /**
     * 初始化所有技能
     */
    public static void init() {
        CorpseOrigin.LOGGER.info("注册了 {} 个尸兄技能", SKILLS.size());
    }
}
