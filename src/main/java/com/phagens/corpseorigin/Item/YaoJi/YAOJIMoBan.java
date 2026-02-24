package com.phagens.corpseorigin.Item.YaoJi;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class YAOJIMoBan extends Item {
    private final List<AttributeData> attributeModifiers;

    public YAOJIMoBan(Properties properties) {
        super(properties);
        this.attributeModifiers = new ArrayList<>();
    }

    public static class AttributeData {
        public final Holder<Attribute> attribute;
        public final AttributeModifier.Operation operation;
        public final double amount;
        public final String name;
        private final ResourceLocation modifierId;

        public AttributeData(Holder<Attribute> attribute, AttributeModifier.Operation operation, double amount, String name) {
            this.attribute = attribute;
            this.operation = operation;
            this.amount = amount;
            this.name = name;
            this.modifierId =ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, name);
        }
    }




    public YAOJIMoBan addAttributeModifier(Holder<Attribute> attribute, AttributeModifier.Operation operation, double amount, String name) {
        this.attributeModifiers.add(new AttributeData(attribute, operation, amount, name));
        return this;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        if (!level.isClientSide) {

            ItemStack powerPotion = new ItemStack(Items.AIR); //.get()
            if (!player.getInventory().add(powerPotion)) {

                player.drop(powerPotion, false);
            }
            appAttrid(player);

            saveModifiersToPlayerData(player);

            itemStack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    private void appAttrid(Player player){
        for (AttributeData attributeData : attributeModifiers){
            AttributeInstance attributeInstance = player.getAttribute(attributeData.attribute);
            if (attributeInstance != null){
                AttributeModifier modifier =
                        new AttributeModifier(attributeData.modifierId,attributeData.amount,attributeData.operation);
                attributeInstance.addTransientModifier(modifier);
            }

        }
    }

    // 保存修饰符信息到玩家数据中
    private void saveModifiersToPlayerData(Player player) {
        CompoundTag playerData = player.getPersistentData();///获取玩家持久化数据
        ListTag modifiersList = new ListTag();  ///创建一个列表标签
        ///将每个修饰符的信息序列化
        for (AttributeData data : attributeModifiers) {
            CompoundTag modifierTag = new CompoundTag();
            modifierTag.putString("AttributeName", data.attribute.unwrapKey().orElseThrow().location().toString());
            modifierTag.putString("ModifierId", data.modifierId.toString());
            modifierTag.putString("ModifierName", data.name);
            modifiersList.add(modifierTag);
        }
        //。保存到玩家数据中
        playerData.put("CorpseOrigin_Attributes", modifiersList);
    }

    // 静态方法：移除玩家的所有CorpseOrigin属性修饰符
    public static void removeAllPlayerAttributes(Player player) {
        CompoundTag playerData = player.getPersistentData();

        if (playerData.contains("CorpseOrigin_Attributes")) {
            ListTag modifiersList = playerData.getList("CorpseOrigin_Attributes", 10);

            // 遍历所有保存的修饰符并移除
            for (int i = 0; i < modifiersList.size(); i++) {
                CompoundTag modifierTag = modifiersList.getCompound(i);
                String modifierIdStr = modifierTag.getString("ModifierId");
                ResourceLocation modifierId = ResourceLocation.tryParse(modifierIdStr);

                if (modifierId != null) {
                    // 移除所有可能属性上的修饰符
                    removeModifierFromAllAttributes(player, modifierId);
                }
            }

            // 清除保存的数据
            playerData.remove("CorpseOrigin_Attributes");
        }
    }
    // 从所有属性中移除指定ID的修饰符
    private static void removeModifierFromAllAttributes(Player player, ResourceLocation modifierId) {
        // 获取常见的属性并尝试移除修饰符
        tryRemoveModifier(player, net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, modifierId);
        tryRemoveModifier(player, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, modifierId);
        tryRemoveModifier(player, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, modifierId);
        tryRemoveModifier(player, net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, modifierId);
        tryRemoveModifier(player, net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE, modifierId);
    }

    // 尝试从指定属性中移除修饰符
    private static void tryRemoveModifier(Player player, Holder<Attribute> attribute, ResourceLocation modifierId) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            AttributeModifier modifier = instance.getModifier(modifierId);
            if (modifier != null) {
                instance.removeModifier(modifier);
            }
        }
    }
}
