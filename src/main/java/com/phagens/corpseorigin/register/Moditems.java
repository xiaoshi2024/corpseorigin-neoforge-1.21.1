package com.phagens.corpseorigin.register;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Item.ByWaterBottleItem;
import com.phagens.corpseorigin.Item.ByWaterBucketItem;
import com.phagens.corpseorigin.Item.JuQue;
import com.phagens.corpseorigin.Item.LieGongFa;
import com.phagens.corpseorigin.Item.YaoJi.Sagent;
import com.phagens.corpseorigin.Item.tier.Modtiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Moditems {
    public static final ResourceLocation BASE_ATTACK_GRA_ID = ResourceLocation.withDefaultNamespace("base_attack_gra");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CorpseOrigin.MODID);

    public static final DeferredItem<Item> MING_JUQUE = ITEMS.register("ming_juque", () -> new JuQue(Modtiers.MingJian,3,-2.4f, new Item.Properties()));

    public static final DeferredItem<BlockItem> QI_XING_GUAN_ITEM = ITEMS.register("qi_xings_guan_item",
            () -> new BlockItem(BlockRegistry.QI_XING_GUAN.get(), new Item.Properties()));

    public static final DeferredItem<Item> BYWATER_BUCKET = ITEMS.register("bywater_bucket",
            () -> new ByWaterBucketItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BYWATER_BOTTLE = ITEMS.register("bywater_bottle",
            () -> new ByWaterBottleItem(new Item.Properties().stacksTo(1)));
    //药剂
    public static final DeferredItem<Item> S_AGENT= ITEMS.register("s_agent",
            () -> new Sagent(new Item.Properties(), "yellow")
                    .addAttributeModifier(Attributes.MAX_HEALTH,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    , 0.5,"yellow"));

    // 其他变种药剂
    public static final DeferredItem<Item> NULL_S_AGENT= ITEMS.register("null_s_agent",
            () -> new Sagent(new Item.Properties(), "null")
                    .addAttributeModifier(Attributes.MAX_HEALTH,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    , 0.3,"null"));
    //这里 你去看item包里的Yaoji  然后 你看addAttributeModifier方法里面 传入参数 即可



    //功法暂时
    public static final DeferredItem<Item> LEI_XI_GONG_FA = ITEMS.register("lei_xi_gong_fa",
            () -> new LieGongFa(new Item.Properties(), "REX"));

    // 刷怪蛋
    public static final DeferredItem<Item> LOWER_LEVEL_ZB_SPAWN_EGG = ITEMS.register("lower_level_zb_spawn_egg",
            () -> new SpawnEggItem(EntityRegistry.LOWER_LEVEL_ZB.get(), 0x8B4513, 0xFF0000, new Item.Properties()));

    public static final DeferredItem<Item> LONGYOU_SPAWN_EGG = ITEMS.register("longyou_spawn_egg",
            () -> new SpawnEggItem(EntityRegistry.LONGYOU.get(), 0x000000, 0xFFD700, new Item.Properties()));

    public static final DeferredItem<Item> ZBR_FISH_SPAWN_EGG = ITEMS.register("zbr_fish_spawn_egg",
            () -> new SpawnEggItem(EntityRegistry.ZBR_FISH.get(), 0x4682B4, 0x8B0000, new Item.Properties()));

    // 开胃奶刷怪蛋
    public static final DeferredItem<Item> KAIWEINAI_SPAWN_EGG = ITEMS.register("kaiweinai_spawn_egg",
            () -> new SpawnEggItem(EntityRegistry.KAIWEINAI.get(), 0xFF0000, 0x8B0000, new Item.Properties()));
}




















