package com.phagens.corpseorigin.Block.ModFluids;

import com.phagens.corpseorigin.register.BlockRegistry;
import com.phagens.corpseorigin.register.Moditems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class ByWaterZL extends FlowingFluid {


    @Override//返回流动水
    public Fluid getFlowing() {
        return Modfluid.FLOWING_BYWATER.get();
    }

    @Override//返回静态水
    public Fluid getSource() {
        return Modfluid.SOUREC_BYWATER.get();
    }

    @Override
    public FluidType getFluidType() {
        return BYWATERFLULDTYPE.BYWATERFLULDTYPE;
    }

    @Override//返回桶装物品
    public Item getBucket() {
        return Moditems.BYWATER_BUCKET.get();
    }


    public static class FlowingBY extends ByWaterZL {
        @Override//状态注册
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override//判断能否为源头
        protected boolean canConvertToSource(Level level) {
            return false;
        }

        @Override//流体破坏方块前调用 自定义破坏
        protected void beforeDestroyingBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {

        }

        @Override//流体水平上方流动距离
        protected int getSlopeFindDistance(LevelReader levelReader) {
            return 4;
        }

        @Override//流体每格下降等级
        protected int getDropOff(LevelReader levelReader) {
            return 1;
        }

        @Override//流体是否被其他流体替换
        protected boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
            return false;
        }

        @Override//流体更新时间
        public int getTickDelay(LevelReader levelReader) {
            return 5;
        }

        @Override//爆炸抗性
        protected float getExplosionResistance() {
            return 0;
        }

        @Override//流体转化为对应方块状态
        protected BlockState createLegacyBlock(FluidState fluidState) {
            return BlockRegistry.BYWATER_BLOCK.get().defaultBlockState();
        }

        @Override//判断流体是不是源头
        public boolean isSource(FluidState fluidState) {
            return false;
        }

        @Override//流体等级
        public int getAmount(FluidState fluidState) {
            return fluidState.getValue(LEVEL);
        }
    }
    public static class SourceBY extends ByWaterZL {
        @Override
        protected boolean canConvertToSource(Level level) {
            return false;
        }

        @Override
        protected void beforeDestroyingBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {

        }

        @Override
        protected int getSlopeFindDistance(LevelReader levelReader) {
            return 4;
        }

        @Override
        protected int getDropOff(LevelReader levelReader) {
            return 1;
        }

        @Override
        protected boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
            return false;
        }

        @Override
        public int getTickDelay(LevelReader levelReader) {
            return 5;
        }

        @Override
        protected float getExplosionResistance() {
            return 0;
        }

        @Override
        protected BlockState createLegacyBlock(FluidState fluidState) {
            return BlockRegistry.BYWATER_BLOCK.get().defaultBlockState();
        }

        @Override
        public boolean isSource(FluidState fluidState) {
            return true;
        }

        @Override
        public int getAmount(FluidState fluidState) {
            return 8;
        }
    }
    public static class BYWATERFLULDTYPE extends FluidType {
        public static final BYWATERFLULDTYPE BYWATERFLULDTYPE = new BYWATERFLULDTYPE();
        public BYWATERFLULDTYPE() {
            super(Properties.create() .density(1000)
                    .viscosity(1000)
                    .lightLevel(0)



            );

}
        }}

