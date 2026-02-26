package com.phagens.corpseorigin.Item.YaoJi;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.client.Renderer.item.SagentRenderer;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Sagent extends Item implements GeoItem {
    private static final RawAnimation PRESS = RawAnimation.begin().thenPlay("press");
    private final List<AttributeData> attributeModifiers;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String variant;

    public Sagent(Properties properties) {
        this(properties, "null");
    }

    public Sagent(Properties properties, String variant) {
        super(properties);
        // Register our item as server-side handled.
        // This enables both animation data syncing and server-side animation triggering
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        this.attributeModifiers = new ArrayList<>();
        this.variant = variant;
    }

    public String getVariant() {
        return variant;
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




    public Sagent addAttributeModifier(Holder<Attribute> attribute, AttributeModifier.Operation operation, double amount, String name) {
        this.attributeModifiers.add(new AttributeData(attribute, operation, amount, name));
        return this;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        
        // 如果是 null 变种，不能使用
        if (this.variant.equals("null")) {
            return InteractionResultHolder.fail(itemStack);
        }
        
        // 触发动画（在服务器端触发）
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            try {
                triggerAnim(player, GeoItem.getOrAssignId(itemStack, serverLevel), "press_controller", "press");
            } catch (Exception e) {
                // 忽略动画错误，继续执行
            }
        }

        if (!level.isClientSide) {
            // 应用属性效果
            appAttrid(player);
            saveModifiersToPlayerData(player);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<Sagent>(this, "press_controller", 0, this::predicate)
                .triggerableAnim("press", PRESS));
    }

    private PlayState predicate(AnimationState<Sagent> SagentAnimationState) {
        return PlayState.CONTINUE;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private SagentRenderer renderer;

            @Override
            public GeoItemRenderer<Sagent> getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new SagentRenderer();

                return this.renderer;
            }
        });
    }

    public int getDefaultAnimationSpeed() {
        return 30;
    }

    private void appAttrid(Player player){
        for (AttributeData attributeData : attributeModifiers){
            AttributeInstance attributeInstance = player.getAttribute(attributeData.attribute);
            if (attributeInstance != null){
                // 检查修饰符是否已经存在
                if (attributeInstance.getModifier(attributeData.modifierId) == null) {
                    AttributeModifier modifier = new AttributeModifier(attributeData.modifierId,attributeData.amount,attributeData.operation);
                    attributeInstance.addTransientModifier(modifier);
                }
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
