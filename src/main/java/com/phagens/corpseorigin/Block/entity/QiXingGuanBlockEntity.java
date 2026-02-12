package com.phagens.corpseorigin.Block.entity;

import com.phagens.corpseorigin.register.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class QiXingGuanBlockEntity extends BlockEntity implements GeoBlockEntity {
    protected static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public QiXingGuanBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.QI_XING_GUANS.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, this::OPENAnimController));
    }

    protected <E extends QiXingGuanBlockEntity> PlayState OPENAnimController(final AnimationState<E> state) {
        return state.setAndContinue(OPEN);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
