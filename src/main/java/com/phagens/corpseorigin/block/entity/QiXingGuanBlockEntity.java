/**
 * 七星棺方块实体类 - 处理棺材的动画渲染
 *
 * 【功能说明】
 * 1. 动画控制：当SUMMONED状态为true时播放开馆动画
 * 2. 使用GeckoLib实现复杂的3D动画效果
 *
 * 【动画系统】
 * - OPEN动画：棺材盖打开的动画
 * - 动画触发条件：方块状态的SUMMONED属性为true
 * - 使用GeckoLib的动画控制器管理动画状态
 *
 * 【关联系统】
 * - QiXingGuan: 控制方块状态和召唤逻辑
 * - GeckoLib: 提供动画渲染支持
 *
 * @author Phagens
 * @version 1.0
 */
package com.phagens.corpseorigin.block.entity;

import com.phagens.corpseorigin.block.custom.QiXingGuan;
import com.phagens.corpseorigin.register.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 七星棺方块实体
 * 实现GeoBlockEntity接口以支持GeckoLib动画
 */
public class QiXingGuanBlockEntity extends BlockEntity implements GeoBlockEntity {

    /** 开馆动画定义 */
    protected static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");

    /** GeckoLib动画实例缓存 */
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * 构造函数
     *
     * @param pos 方块位置
     * @param state 方块状态
     */
    public QiXingGuanBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.QI_XING_GUANS.get(), pos, state);
    }

    /**
     * 注册动画控制器
     * 实现GeoBlockEntity接口的方法
     *
     * @param controllers 动画控制器注册器
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, this::OPENAnimController));
    }

    /**
     * 开馆动画控制器
     * 根据方块状态控制动画播放
     *
     * @param state 动画状态
     * @return 动画播放状态
     */
    protected <E extends QiXingGuanBlockEntity> PlayState OPENAnimController(final AnimationState<E> state) {
        BlockState blockstate = getBlockState();
        if (blockstate.getValue(QiXingGuan.SUMMONED)) {
            return state.setAndContinue(OPEN); // 执行开馆动画
        } else {
            return PlayState.STOP; // 停止动画
        }
    }

    /**
     * 获取动画实例缓存
     * 实现GeoBlockEntity接口的方法
     *
     * @return 动画实例缓存
     */
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
