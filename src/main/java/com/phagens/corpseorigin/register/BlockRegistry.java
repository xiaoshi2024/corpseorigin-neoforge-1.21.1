package com.phagens.corpseorigin.register;

import com.phagens.corpseorigin.Block.custom.QiXingGuan;
import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockRegistry {
    public static final DeferredRegister.Blocks Blocks =
            DeferredRegister.createBlocks(CorpseOrigin.MODID);
    public static final DeferredBlock<QiXingGuan> QI_XING_GUAN = Blocks.register("qi_xing_guan",
            () -> new QiXingGuan(EntityType.ZOMBIE)); // 假设召唤的是僵尸

}
