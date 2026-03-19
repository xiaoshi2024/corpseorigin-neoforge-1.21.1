package com.phagens.corpseorigin.skill;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 技能基础实现类
 */
public abstract class BaseSkill implements ISkill {

    protected final ResourceLocation id;
    protected final Component name;
    protected final Component description;
    protected final ResourceLocation icon;
    protected final int cost;
    protected final SkillType skillType;
    protected final int requiredLevel;
    protected final List<ResourceLocation> prerequisites;
    protected final boolean passive;
    protected final int cooldown;
    protected final int duration;

    // 属性修饰符 - 使用Holder<Attribute>
    protected final Map<Holder<Attribute>, AttributeModifier> attributeModifiers = new HashMap<>();

    public BaseSkill(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.icon = builder.icon;
        this.cost = builder.cost;
        this.skillType = builder.skillType;
        this.requiredLevel = builder.requiredLevel;
        this.prerequisites = new ArrayList<>(builder.prerequisites);
        this.passive = builder.passive;
        this.cooldown = builder.cooldown;
        this.duration = builder.duration;
        this.attributeModifiers.putAll(builder.attributeModifiers);

        CorpseOrigin.LOGGER.info("技能 [{}] 的图标路径: {}",
                builder.id.getPath(), this.icon);
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
    @Nullable
    public Component getDescription() {
        return description;
    }

    @Override
    public ResourceLocation getIcon() {
        return icon;
    }

    @Override
    public int getCost() {
        return cost;
    }

    @Override
    public SkillType getSkillType() {
        return skillType;
    }

    @Override
    public int getRequiredLevel() {
        return requiredLevel;
    }

    @Override
    public List<ResourceLocation> getPrerequisites() {
        return Collections.unmodifiableList(prerequisites);
    }

    @Override
    public boolean isPassive() {
        return passive;
    }

    @Override
    public int getCooldown() {
        return cooldown;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public boolean canLearn(Player player) {
        // 基础检查，子类可以覆盖
        return true;
    }

    @Override
    public void onLearn(Player player) {
        CorpseOrigin.LOGGER.debug("玩家 {} 学习了技能 {}", player.getName().getString(), id);

        // 应用属性修饰符
        applyAttributeModifiers(player);
    }

    @Override
    public void onForget(Player player) {
        CorpseOrigin.LOGGER.debug("玩家 {} 遗忘了技能 {}", player.getName().getString(), id);

        // 移除属性修饰符
        removeAttributeModifiers(player);
    }

    @Override
    public void onActivate(Player player) {
        // 默认实现，子类覆盖
        CorpseOrigin.LOGGER.debug("玩家 {} 激活了技能 {}", player.getName().getString(), id);
    }

    /**
     * 应用属性修饰符 - 修复重复应用问题
     */
    protected void applyAttributeModifiers(Player player) {
        for (Map.Entry<Holder<Attribute>, AttributeModifier> entry : attributeModifiers.entrySet()) {
            var instance = player.getAttribute(entry.getKey());
            if (instance != null) {
                // 先移除已有的修饰符，再添加新的
                instance.removeModifier(entry.getValue().id());
                instance.addTransientModifier(entry.getValue());
                CorpseOrigin.LOGGER.debug("应用属性修饰符: {} 到玩家 {}",
                        entry.getValue().id(), player.getName().getString());
            }
        }
    }

    /**
     * 移除属性修饰符
     */
    protected void removeAttributeModifiers(Player player) {
        for (Map.Entry<Holder<Attribute>, AttributeModifier> entry : attributeModifiers.entrySet()) {
            var instance = player.getAttribute(entry.getKey());
            if (instance != null) {
                instance.removeModifier(entry.getValue().id());
            }
        }
    }

    /**
     * 获取属性修饰符
     */
    public Map<Holder<Attribute>, AttributeModifier> getAttributeModifiers() {
        return Collections.unmodifiableMap(attributeModifiers);
    }

    /**
     * 构建器类
     */
    public static class Builder {
        private ResourceLocation id;
        private Component name = Component.empty();
        private Component description;
        private ResourceLocation icon;
        private int cost = 1;
        private SkillType skillType = SkillType.BASIC_EVOLUTION;
        private int requiredLevel = 1;
        private List<ResourceLocation> prerequisites = new ArrayList<>();
        private boolean passive = false;
        private int cooldown = 0;
        private int duration = -1;
        private Map<Holder<Attribute>, AttributeModifier> attributeModifiers = new HashMap<>();

        public Builder(ResourceLocation id) {
            this.id = id;
            this.icon = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                    "textures/skills/" + id.getPath() + ".png");
        }

        public Builder name(Component name) {
            this.name = name;
            return this;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder icon(ResourceLocation icon) {
            this.icon = icon;
            return this;
        }

        public Builder cost(int cost) {
            this.cost = cost;
            return this;
        }

        public Builder skillType(SkillType skillType) {
            this.skillType = skillType;
            return this;
        }

        public Builder requiredLevel(int requiredLevel) {
            this.requiredLevel = requiredLevel;
            return this;
        }

        public Builder prerequisite(ResourceLocation prerequisite) {
            this.prerequisites.add(prerequisite);
            return this;
        }

        public Builder prerequisites(ResourceLocation... prerequisites) {
            this.prerequisites.addAll(Arrays.asList(prerequisites));
            return this;
        }

        public Builder passive(boolean passive) {
            this.passive = passive;
            return this;
        }

        public Builder cooldown(int cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder attributeModifier(Holder<Attribute> attribute, AttributeModifier modifier) {
            this.attributeModifiers.put(attribute, modifier);
            return this;
        }

        public ResourceLocation getId() {
            return id;
        }
    }
}