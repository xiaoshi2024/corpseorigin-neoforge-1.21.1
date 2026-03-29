package com.phagens.corpseorigin.Item.Organic;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.player.PlayerCorpseData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.List;
import java.util.function.Consumer;

/**
 * 穆博士的眼睛 - 稀有掉落物
 * 失去意识的尸兄食用后可恢复人类智慧
 *
 * 【功能说明】
 * 1. 只有失去意识的尸兄玩家才能食用并恢复意识
 * 2. 已有意识的尸兄食用后获得智慧增强效果
 * 3. 普通玩家食用会受到严重惩罚
 */
public class DrMuEyeItem extends ZbOrganItem {

    public DrMuEyeItem() {
        super(OrganType.EYE, 1.0f, 1, 6, 0.5f);
    }

    @Override
    protected void onOrganConsumed(Player player, ItemStack stack) {
        CorpseOrigin.LOGGER.info("尸兄玩家 {} 食用了穆博士的眼睛", player.getName().getString());

        boolean hadConsciousness = PlayerCorpseData.hasConsciousness(player);

        if (!hadConsciousness) {
            // 失去意识的尸兄恢复意识
            PlayerCorpseData.restoreConsciousness(player);

            player.sendSystemMessage(Component.literal(
                    "§a§l你的意识从混沌中苏醒！§r\n" +
                    "§7穆博士的智慧流入你的脑海，你重新获得了人类的智慧！\n" +
                    "§7你现在可以使用工作台、门等复杂物品了。"
            ).withStyle(ChatFormatting.GREEN));

            CorpseOrigin.LOGGER.info("尸兄玩家 {} 食用穆博士的眼睛后恢复了意识！", player.getName().getString());

            // 给予恢复意识后的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 0));
        } else {
            // 已有意识的尸兄获得智慧增强
            player.sendSystemMessage(Component.literal(
                    "§e§l智慧增强！§r\n" +
                    "§7穆博士的智慧与你的意识融合，你感觉思维更加清晰！"
            ).withStyle(ChatFormatting.YELLOW));

            // 给予智慧增强效果
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 12000, 0));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 0));

            CorpseOrigin.LOGGER.info("尸兄玩家 {} 食用穆博士的眼睛，获得智慧增强", player.getName().getString());
        }

        // 恢复饥饿值
        PlayerCorpseData.setHunger(player, Math.min(100, PlayerCorpseData.getHunger(player) + 20));
    }

    @Override
    protected boolean applyEvolution(Player player) {
        // 穆博士的眼睛直接恢复意识，不需要概率判定
        return true;
    }

    @Override
    protected void addEffectDescription(List<Component> tooltip) {
        tooltip.add(Component.translatable("item.corpseorigin.dr_mu_eye.effect1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.corpseorigin.dr_mu_eye.effect2")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.corpseorigin.dr_mu_eye.effect3")
                .withStyle(ChatFormatting.GRAY));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<DrMuEyeItem> renderer;

            @Override
            public GeoItemRenderer<DrMuEyeItem> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new com.phagens.corpseorigin.client.Renderer.item.DrMuEyeRenderer();
                }
                return this.renderer;
            }
        });
    }
}
