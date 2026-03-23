package com.phagens.corpseorigin.Item;

import com.phagens.corpseorigin.register.EffectRegister;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ByWaterBottleItem extends Item {
    public ByWaterBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        // 玩家自己喝
        if (player.canEat(false)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 当用此物品攻击实体时触发
        if (!attacker.level().isClientSide && attacker instanceof Player player) {
            // 检查目标是否是村民
            if (target instanceof Villager) {
                // 给村民添加效果
                target.addEffect(new MobEffectInstance(
                        EffectRegister.QIANS,
                        200,                    // 10秒持续时间
                        0,                      // 等级1
                        false,                  // 不显示粒子
                        true,                   // 显示图标
                        true                    // 显示环境效果
                ));

                // 消耗一个物品（如果不是创造模式）
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    // 给玩家返回玻璃瓶
                    ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                    if (!player.getInventory().add(bottle)) {
                        // 如果背包满了，扔到地上
                        player.drop(bottle, false);
                    }
                }

                // 播放一个音效或粒子效果（可选）
                // player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                //     SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 1.0F, 1.0F);

                return true; // 表示造成了伤害（但实际上没伤害）
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            // 添加中毒效果
            player.addEffect(new MobEffectInstance(
                    MobEffects.POISON, 400, 1, false, true, true
            ));

            // 直接使用 Holder 添加自定义效果
            player.addEffect(new MobEffectInstance(
                    EffectRegister.QIANS, 200, 0, false, true, true
            ));
        }

        // 返回玻璃瓶
        return new ItemStack(Items.GLASS_BOTTLE);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 32;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return new ItemStack(Items.GLASS_BOTTLE);
    }
}