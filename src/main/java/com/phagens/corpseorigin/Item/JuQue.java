package com.phagens.corpseorigin.Item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.Objects;

public class JuQue extends SwordItem {
    public JuQue(Tier tier, int attackDamage, float attackSpeed,Properties properties) {
        super(tier, properties.component(DataComponents.ATTRIBUTE_MODIFIERS,createAttributes(tier,attackDamage,attackSpeed)));


    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 存活                  //最高生命              //最低生命
        if (target.isAlive() && target.getHealth() / target.getMaxHealth() <= 0.3F) {
            target.setHealth(0.0F);
        }

        if (Math.random()<=0.2){
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100,2));
        }


        if (Math.random() <= 0.2) {
            float currentDamage = (float) Objects.requireNonNull(attacker.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
            target.hurt(attacker.damageSources().generic(), currentDamage * 2);
        }


        return super.hurtEnemy(stack, target, attacker);
    }
    public static ItemAttributeModifiers createAttributes(Tier tier, int attackDamage, float attackSpeed) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                                BASE_ATTACK_DAMAGE_ID,
                                (double)(attackDamage + tier.getAttackDamageBonus()),
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(     //属性构造器
                                BASE_ATTACK_SPEED_ID,                   //属性标识符
                                (double)attackSpeed,                    //数值
                                AttributeModifier.Operation.ADD_VALUE),  //计算方式
                        EquipmentSlotGroup.HAND)                        //应用地
                .build();


    }
}
