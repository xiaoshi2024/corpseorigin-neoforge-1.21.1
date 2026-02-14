package com.phagens.corpseorigin.register;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Item.ByWaterBucketItem;
import com.phagens.corpseorigin.Item.ByWaterBottleItem;
import com.phagens.corpseorigin.Item.JuQue;
import com.phagens.corpseorigin.Item.tier.Modtiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
}




















