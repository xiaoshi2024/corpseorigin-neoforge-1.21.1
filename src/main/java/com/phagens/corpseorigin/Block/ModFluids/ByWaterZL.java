package com.phagens.corpseorigin.Block.ModFluids;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.register.BlockRegistry;
import com.phagens.corpseorigin.register.Moditems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class ByWaterZL extends FlowingFluid {


    @Override//返回流动水
    public Fluid getFlowing() {
        Fluid flowing = Modfluid.FLOWING_BYWATER.get();
        if (flowing == null) {
            CorpseOrigin.LOGGER.error("FLOWING_BYWATER is null!");
        }
        return flowing;
    }

    @Override//返回静态水
    public Fluid getSource() {
        Fluid source = Modfluid.SOUREC_BYWATER.get();
        if (source == null) {
            CorpseOrigin.LOGGER.error("SOUREC_BYWATER is null!");
        }
        return source;
    }

    @Override
    public FluidType getFluidType() {

        return ModFluidType.SOUREC_BYWATER.get();
    }

    @Override//返回桶装物品
    public Item getBucket() {
        return Moditems.BYWATER_BUCKET.get();
    }

    @Override
    protected BlockState createLegacyBlock(FluidState fluidState) {
        return BlockRegistry.BYWATER_BLOCK.get().defaultBlockState();
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
            return 2;
        }

        @Override//流体每格下降等级
        protected int getDropOff(LevelReader levelReader) {
            return 4;
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
            return 100;
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
        public void tick(Level level, BlockPos pos, FluidState state) {
            super.tick(level, pos, state);

        }
        //血海
        private void infectNeighbors(Level level, BlockPos center) {
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = center.relative(dir);
                BlockState neighborState = level.getBlockState(neighbor);
                // 检查是否为原版水方块
                if (!neighborState.is(Blocks.WATER)&&neighborState.getFluidState().is(FluidTags.WATER)) {
                    level.setBlock(neighbor, BlockRegistry.BYWATER_BLOCK.get().defaultBlockState(), 3);
                }
            }}

        @Override
        protected boolean canConvertToSource(Level level) {
            return false;
        }

        @Override
        protected void beforeDestroyingBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {

        }

        @Override
        protected int getSlopeFindDistance(LevelReader levelReader) {
            return 2;
        }

        @Override
        protected int getDropOff(LevelReader levelReader) {
            return 4;
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
            return 100;
        }



        @Override
        public boolean isSource(FluidState fluidState) {
            return true;
        }

        @Override
        public int getAmount(FluidState fluidState) {
            return 8;
        }




        //特殊

    }



}


