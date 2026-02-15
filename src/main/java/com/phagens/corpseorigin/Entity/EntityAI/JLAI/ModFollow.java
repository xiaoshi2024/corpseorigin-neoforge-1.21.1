//package com.phagens.corpseorigin.Entity.EntityAI.JLAI;
//
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.PathfinderMob;
//import net.minecraft.world.entity.ai.goal.Goal;
//import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
//
//import java.nio.file.Path;
//
//public class ModFollow extends Goal {
//    protected final PathfinderMob mob;   //生物引用 指向指向攻击的生物实体访问生物能力和状态
//    private final double spend; //生物的移动倍率调节
//    private final boolean followingTarget; //控制是否追踪看不见的目标
//    private Path path;//路径计算
//    private double pathedTargetX;//目标的xyz坐标
//    private double pathedTargetY;
//    private double pathedTargetZ;
//    private int ticksUntilNextPathRecalculation;//控制多久重新计算路径
//    private int ticksUntilNextAttack;   //冷却倒计时
//    private final int attackInterval = 20;  // 攻击间隔
//    private long lastCanUseCheck;       //限制canuse 检查的间隔
//    private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;
//    private int failedPathFindingPenalty = 0;  //寻路失败累计的延迟惩罚
//    private boolean canPenalize = false;      //是否启用惩罚
//
//
//
//
//
//
//    public ModFollow(PathfinderMob mob, double spend, boolean followingTarget) {
//        this.mob = mob;
//        this.spend = spend;
//        this.followingTarget = followingTarget;
//    }
//
//    @Override
//    public boolean canUse() {
//        long i =this.mob.level().getGameTime();
//        if (i - this.lastCanUseCheck<20L){
//            return false;
//        }else {
//            this.lastCanUseCheck = i;  // 1️⃣ 更新检查时间戳
//            LivingEntity livingentity = this.mob.getTarget();  // 2️ 获取目标
//
//
//        }
//    }
//}
