package com.phagens.corpseorigin.register;

import com.phagens.corpseorigin.Block.ModFluids.FliuidBlock;
import com.phagens.corpseorigin.Block.ModFluids.Modfluid;
import com.phagens.corpseorigin.Block.custom.QiXingGuan;
import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockRegistry {
    public static final DeferredRegister.Blocks Blocks =
            DeferredRegister.createBlocks(CorpseOrigin.MODID);
    public static final DeferredBlock<QiXingGuan> QI_XING_GUAN = Blocks.register("qi_xing_guan",
            () -> new QiXingGuan(EntityType.ZOMBIE)); // 假设召唤的是僵尸
    //流体注册
    public static final DeferredBlock<FliuidBlock> BYWATER_BLOCK = Blocks.register("bywater_block",()->new FliuidBlock(Modfluid.SOUREC_BYWATER.get(),BlockBehaviour.Properties.of()
            .replaceable()
            .noCollission()
            .strength(100.0F)
            .pushReaction(PushReaction.DESTROY)
            .noLootTable()
            .liquid()
            .sound(SoundType.EMPTY)
    ));
}
