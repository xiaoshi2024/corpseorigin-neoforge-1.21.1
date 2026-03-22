/**
 * 尸兄追击AI目标类 - 自定义近战攻击逻辑
 *
 * 【功能说明】
 * 1. 目标追踪：持续追踪并接近目标实体
 * 2. 路径计算：智能重新计算路径，避免卡顿
 * 3. 攻击冷却：管理攻击间隔和冷却时间
 * 4. 寻路惩罚：寻路失败时增加延迟，避免频繁计算
 * 5. 视线检测：可选择是否追踪看不见的目标
 *
 * 【工作原理】
 * - canUse(): 每20tick检查一次是否可以开始追击
 * - canContinueToUse(): 每tick检查是否应该继续追击
 * - tick(): 每tick更新路径、检查攻击条件并执行攻击
 * - 寻路失败惩罚：连续寻路失败会增加攻击冷却时间
 *
 * 【重要参数】
 * - attackInterval: 20 tick = 1秒攻击间隔
 * - COOLDOWN_BETWEEN_CAN_USE_CHECKS: 20 tick检查间隔
 * - failedPathFindingPenalty: 寻路失败惩罚累计值
 *
 * 【关联系统】
 * - ModEntityJL: 尸兄基础实体类，使用此AI
 * - PathfinderMob: Minecraft寻路生物基类
 *
 * @author Phagens
 * @version 1.0
 */
package com.phagens.corpseorigin.Entity.EntityAI.JLAI;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

/**
 * 尸兄追击AI目标
 * 继承Goal实现自定义追击和攻击逻辑
 */
public class ModFollow extends Goal {
    /** 生物引用，指向要控制的生物实体 */
    protected final PathfinderMob mob;
    /** 移动速度倍率调节 */
    private final double spend;
    /** 控制是否追踪看不见的目标 */
    private final boolean followingTarget;
    /** 当前路径 */
    private Path path;
    /** 目标X坐标 */
    private double pathedTargetX;
    /** 目标Y坐标 */
    private double pathedTargetY;
    /** 目标Z坐标 */
    private double pathedTargetZ;
    /** 距离下次路径重新计算的tick数 */
    private int ticksUntilNextPathRecalculation;
    /** 攻击冷却倒计时 */
    private int ticksUntilNextAttack;
    /** 攻击间隔（tick） */
    private final int attackInterval = 20;
    /** 限制canUse检查的时间戳 */
    private long lastCanUseCheck;
    /** canUse检查的最小间隔 */
    private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;
    /** 寻路失败累计的延迟惩罚 */
    private int failedPathFindingPenalty = 0;
    /** 是否启用惩罚机制 */
    private boolean canPenalize = false;

    /**
     * 构造函数
     *
     * @param mob 要控制的生物实体
     * @param spend 移动速度倍率
     * @param followingTarget 是否追踪看不见的目标
     */
    public ModFollow(PathfinderMob mob, double spend, boolean followingTarget) {
        this.mob = mob;
        this.spend = spend;
        this.followingTarget = followingTarget;
        // 设置AI标志：允许移动和观察
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /**
     * 判断是否应继续追击
     * 检查目标是否仍然有效
     *
     * @return 是否继续追击
     */
    @Override
    public boolean canContinueToUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else if (!this.followingTarget) {
            return !this.mob.getNavigation().isDone();
        } else {
            // 检查目标是否超出限制范围，以及是否为创造/旁观模式玩家
            return !this.mob.isWithinRestriction(livingentity.blockPosition()) ? false : !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player) livingentity).isCreative();
        }
    }

    /**
     * 判断是否可以开始追击
     * 每20tick检查一次，避免频繁计算
     *
     * @return 是否可以开始追击
     */
    @Override
    public boolean canUse() {
        long i = this.mob.level().getGameTime();
        // 检查是否满足最小检查间隔
        if (i - this.lastCanUseCheck < 20L) {
            return false;
        } else {
            this.lastCanUseCheck = i;  // 更新检查时间戳
            LivingEntity livingentity = this.mob.getTarget();  // 获取目标
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else if (this.canPenalize) {
                // 启用惩罚机制时的路径计算
                if (--this.ticksUntilNextPathRecalculation <= 0) {
                    this.path = this.mob.getNavigation().createPath(livingentity, 0);
                    this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                    return this.path != null;
                } else {
                    return true;
                }
            } else {
                // 正常路径计算
                this.path = this.mob.getNavigation().createPath(livingentity, 0);
                return this.path != null || this.mob.isWithinMeleeAttackRange(livingentity);
            }
        }
    }


    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.spend);
        this.mob.setAggressive(true); //设置攻击状态
        this.ticksUntilNextPathRecalculation = 0;  //重置路径计算倒计时
        this.ticksUntilNextAttack = 0;//攻击冷却倒计时
    }
    public void stop() {
        LivingEntity livingentity = this.mob.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
            this.mob.setTarget((LivingEntity)null);
        }
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null){
            this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);  //调用生物视觉控制器 调整水平转动速度 or 垂直传动速度
            this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0); //路径计算倒计时减少一
           if ((this.followingTarget||this.mob.getSensing().hasLineOfSight(livingentity))
           &&this.ticksUntilNextAttack<=0&&(this.pathedTargetX==(double)0.0F &&this.pathedTargetY==(double)0.0F &&this.pathedTargetZ==(double)0.0F||
                   livingentity.distanceToSqr(this.pathedTargetX,this.pathedTargetY,this.pathedTargetZ)>=1.0D||this.mob.getRandom().nextFloat()<=0.05F)){
               this.pathedTargetX = livingentity.getX();
               this.pathedTargetY = livingentity.getY();
               this.pathedTargetZ = livingentity.getZ();
               this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
               double d0 = this.mob.distanceToSqr(livingentity);//计算与目标的距离平方
               if (this.canPenalize){//触发惩罚
                   this.ticksUntilNextAttack +=this.failedPathFindingPenalty;//引用累计失败
                   if(this.mob.getNavigation().getPath()!=null){
                       Node finalPathPoint = this.mob.getNavigation().getPath().getEndNode();//获取路径终点节点
                       if (finalPathPoint !=null &&   //计算目标与终点距离平方小于1格 意义为是否到了目标附近
                               livingentity.distanceToSqr((double)finalPathPoint.x,(double)finalPathPoint.y,(double)finalPathPoint.z)<(double) 1.0f){
                           this.failedPathFindingPenalty =0; //重置失败计时器
                       }else {
                           this.failedPathFindingPenalty +=10; //失败计时器增加
                       }
                   }else {
                       this.failedPathFindingPenalty +=10;
                   }

               }

               if (d0>(double)1024.0F){
                   this.ticksUntilNextPathRecalculation += 10;
               } else if (d0>(double)512.0F){
                   this.ticksUntilNextPathRecalculation += 5;
               }
               if (!this.mob.getNavigation().moveTo(livingentity,this.spend)){
                   this.ticksUntilNextPathRecalculation += 15;
               }
               this.ticksUntilNextPathRecalculation=this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);//难度调节减少寻路延迟
           }
            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
            this.checkAndPerformAttack(livingentity);


        }
    }
    //动画应用需重写
    private void checkAndPerformAttack(LivingEntity livingentity) {
      if(this.lenqueandfanweiandemubiao(livingentity)){
          this.resetAttackTick();
//          this.mob.swing(InteractionHand.MAIN_HAND);
          this.mob.doHurtTarget(livingentity);//执行伤害
      }
}

    private void resetAttackTick() {
        //重置
        this.ticksUntilNextAttack = this.adjustedTickDelay(this.attackInterval);
    }

    private boolean lenqueandfanweiandemubiao(LivingEntity livingentity) {
        //冷却 and 距离 and 看的见吗
        return this.istime()&&this.mob.isWithinMeleeAttackRange(livingentity)&&this.mob.getSensing().hasLineOfSight(livingentity);
    }

    private boolean istime() {
        return this.ticksUntilNextAttack <= 0;
    }

    public int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }
}
