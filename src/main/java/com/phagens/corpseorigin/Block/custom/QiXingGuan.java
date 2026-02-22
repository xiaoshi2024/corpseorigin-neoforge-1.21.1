package com.phagens.corpseorigin.Block.custom;


import com.phagens.corpseorigin.Block.entity.QiXingGuanBlockEntity;
import com.phagens.corpseorigin.data.InfectionData;
import com.phagens.corpseorigin.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.*;

public class QiXingGuan extends Block implements EntityBlock {
    private final EntityType<?> ENTITY;

    public static final BooleanProperty SUMMONED = BooleanProperty.create("summoned");

    public QiXingGuan(EntityType<?> entity) {
        super(BlockBehaviour.Properties.of()
                .strength(1.5f,6.0f).//硬度抗性
                sound(SoundType.WOOD)//声音
                .mapColor(MapColor.WOOD)//地图颜色
                .noOcclusion()//如有透明
                .randomTicks()

        );//shengy

        ENTITY = entity;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SUMMONED,false ));
    }

    //碰撞箱
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getCollisionShape(state, level, pos, context);
    }
    //方块状态注册
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SUMMONED);
    }
    //检测是否在水中
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            spreadWaterInfection(level, pos);
        }
        level.scheduleTick(pos, this, 20);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!level.isClientSide && state.is(state.getBlock())) {
            clearInfection(pos, level);
        }

    }


    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);


        if (!state.getValue(SUMMONED)) {
            // 检测实体
            detectEntitiesInInfectedArea(level, pos);
            // 重新安排下次检测
        }
        level.scheduleTick(pos, this, 20);


    }

    private static final int MAX_ENERGY = 15; // 最大能量值（类似红石强度）
    private static final int ENERGY_DECAY = 1; // 每格距离能量衰减值

    private void spreadWaterInfection(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        
        InfectionData data = InfectionData.get(serverLevel);
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> infectedPositions = new HashSet<>();
        queue.offer(pos);
        visited.add(pos);
        data.setWaterEnergy(pos, MAX_ENERGY); // 棺材位置能量最高
        final int MAX_DISTANCE = 32;

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            int currentEnergy = data.getWaterEnergy(current);
            int distance = Math.max(
                    Math.abs(current.getX() - pos.getX()),
                    Math.max(
                            Math.abs(current.getY() - pos.getY()),
                            Math.abs(current.getZ() - pos.getZ())
                    )
            );
            if (distance >= MAX_DISTANCE || currentEnergy <= 0) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (visited.contains(neighbor)) {
                    continue;
                }
                BlockState neighborState = level.getBlockState(neighbor);
                if (neighborState.getFluidState().is(FluidTags.WATER)) {
                    int neighborEnergy = currentEnergy - ENERGY_DECAY;
                    if (neighborEnergy > 0) {
                        markInfectedWithEnergy(data, pos, neighbor, neighborEnergy, infectedPositions);
                        visited.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
        }
    }
    
    //标记感染的水位置并设置能量
    private void markInfectedWithEnergy(InfectionData data, BlockPos coffinPos, BlockPos waterPos, int energy, Set<BlockPos> infectedPositions) {
        infectedPositions.add(waterPos);
        data.addInfectedWater(coffinPos, waterPos, energy);
    }

    //标记水为感染状态（静态方法，供外部调用）
    public static void markWaterInfected(BlockPos pos) {
        // 这个方法现在需要 ServerLevel，但由于是静态方法，我们暂时保留原逻辑
        // 实际使用时会通过事件处理器传入 ServerLevel
    }

    //检查水是否被感染
    public static boolean isWaterInfected(BlockPos pos) {
        // 这个方法现在需要 ServerLevel，但由于是静态方法，我们暂时保留原逻辑
        // 实际使用时会通过事件处理器传入 ServerLevel
        return false;
    }
    
    //清除棺材的感染
    private void clearInfection(BlockPos coffinPos, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        
        if (!isInOcean(level, coffinPos)) {
            return;
        }
        
        InfectionData data = InfectionData.get(serverLevel);
        data.removeCoffinInfections(coffinPos);
    }
    
    //检查棺材是否在海洋中（16x16范围内水方块数量超过阈值）
    private boolean isInOcean(Level level, BlockPos pos) {
        final int RANGE = 8; // 16x16范围，半径8格
        final int WATER_THRESHOLD = 100; // 水方块数量阈值
        
        int waterCount = 0;
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-RANGE, -RANGE, -RANGE), pos.offset(RANGE, RANGE, RANGE))) {
            if (level.getBlockState(checkPos).getFluidState().is(FluidTags.WATER)) {
                waterCount++;
                if (waterCount >= WATER_THRESHOLD) {
                    return true;
                }
            }
        }
        return false;
    }

    //召唤逻辑
    private void detectEntitiesInInfectedArea(Level level, BlockPos posE) {
        final int DETECTION_RADIUS = 32;  // 检测半径32格
        final int REQUIRED_ENTITY_COUNT = 3;
        // 检测指定范围内的所有实体
        List<Entity> entities = level.getEntitiesOfClass(
                Entity.class,  // 可以改为特定生物类型
                new AABB(posE).inflate(DETECTION_RADIUS)
        );
        // 如果达到所需数量且未召唤过
        if (entities.size() >= REQUIRED_ENTITY_COUNT && !level.getBlockState(posE).getValue(SUMMONED)) {
            triggerAction((ServerLevel) level, posE);
            level.setBlock(posE, this.stateDefinition.any().setValue(SUMMONED, true), 3);
        }

    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    // 实现 EntityBlock 接口的方法
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QiXingGuanBlockEntity(pos, state);
    }

    private void triggerAction(ServerLevel level,BlockPos pos) {
        ENTITY.spawn(level, null, null, pos.above(), MobSpawnType.EVENT, false, false);
    }


}
