package com.phagens.corpseorigin.Block.custom;


import com.phagens.corpseorigin.Block.entity.QiXingGuanBlockEntity;
import com.phagens.corpseorigin.register.BlockRegistry;
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
    //召唤的小怪
    private final EntityType<?> ENTITY;

    //瘫痪
    public static final BooleanProperty SUMMONED = BooleanProperty.create("summoned");
    private final Set<BlockPos> infectedPositions = new HashSet<>();

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
        spreadWaterInfection(level, pos); // 感染周围水源
        System.out.println("QiXingGuan placed at: " + pos);


    }


    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        if (!state.getValue(SUMMONED)) {
            // 检测实体
            detectEntitiesInInfectedArea(level, pos);
            // 重新安排下次检测
            level.scheduleTick(pos, this, 20);
        }


    }
    //感染逻辑
    private void spreadWaterInfection(Level level, BlockPos pos) {
        //未感染 已感染标记
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.offer(pos);
        visited.add(pos);
        infectedPositions.add(pos);
        final int MAX_DISTANCE = 128;
        //寻路
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            int distance = Math.max(
                    Math.abs(current.getX() - pos.getX()),
                    Math.max(
                            Math.abs(current.getY() - pos.getY()),
                            Math.abs(current.getZ() - pos.getZ())
                    )
            );
            if (distance >= MAX_DISTANCE) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (neighbor.equals(pos) || visited.contains(neighbor)) {
                    continue;
                }
                if (level.getBlockState(neighbor).getFluidState().is(FluidTags.WATER)){
                        markInfected(level, neighbor);
                        visited.add( neighbor);
                        queue.offer(neighbor);
                }
            }
        }
    }
    //更换感染水方块  暂定
    private void markInfected(Level level, BlockPos neighbor) {
        level.setBlock(neighbor, BlockRegistry.BYWATER_BLOCK.get().defaultBlockState(), 3);
    }

    //召唤逻辑
    private void detectEntitiesInInfectedArea(Level level, BlockPos posE) {
        final int DETECTION_RADIUS = 32;  // 检测半径32格
        final int REQUIRED_ENTITY_COUNT = 30;
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
