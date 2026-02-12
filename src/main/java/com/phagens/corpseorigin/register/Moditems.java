package com.phagens.corpseorigin.register;

import com.jcraft.jorbis.Block;
import com.phagens.corpseorigin.Block.QiXingGuan;
import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Item.JuQue;
import com.phagens.corpseorigin.Item.tier.Modtiers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Moditems {
    public static final ResourceLocation BASE_ATTACK_GRA_ID = ResourceLocation.withDefaultNamespace("base_attack_gra");
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CorpseOrigin.MODID);
    public static final DeferredRegister.Blocks Blocks =
            DeferredRegister.createBlocks(CorpseOrigin.MODID);
    public static final DeferredItem<Item> MING_JUQUE =
            ITEMS.register("ming_juque", () -> new JuQue(Modtiers.MingJian,3,-2.4f, new Item.Properties()));
    public static final DeferredBlock<QiXingGuan> QI_XING_GUAN = Blocks.register("qi_xing_guan",
            () -> new QiXingGuan(EntityType.ZOMBIE)); // 假设召唤的是僵尸
    public static final DeferredItem<BlockItem> QI_XING_GUAN_ITEM = ITEMS.register("qi_xings_guan",
            () -> new BlockItem(QI_XING_GUAN.get(), new Item.Properties()));























    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        Blocks.register(bus);
    }
}



















