package com.phagens.corpseorigin.Entity.EntityAI.JLAI;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;


public class ModFollow extends Goal {
    protected final PathfinderMob mob;   //生物引用 指向指向攻击的生物实体访问生物能力和状态
    private final double spend; //生物的移动倍率调节
    private final boolean followingTarget; //控制是否追踪看不见的目标
    private Path path;//路径计算
    private double pathedTargetX;//目标的xyz坐标
    private double pathedTargetY;
    private double pathedTargetZ;
    private int ticksUntilNextPathRecalculation;//控制多久重新计算路径
    private int ticksUntilNextAttack;   //冷却倒计时
    private final int attackInterval = 20;  // 攻击间隔
    private long lastCanUseCheck;       //限制canuse 检查的间隔
    private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;
    private int failedPathFindingPenalty = 0;  //寻路失败累计的延迟惩罚
    private boolean canPenalize = false;      //是否启用惩罚


    public ModFollow(PathfinderMob mob, double spend, boolean followingTarget) {
        this.mob = mob;
        this.spend = spend;//移动速度调节
        this.followingTarget = followingTarget;  //是否追踪看不见目标
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); //设置AI  允许移动 与观察
    }

    //判断目标还在不 还打不
    public boolean canContinueToUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else if (!this.followingTarget) {
            return !this.mob.getNavigation().isDone();
        } else {
            return !this.mob.isWithinRestriction(livingentity.blockPosition()) ? false : !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player)livingentity).isCreative();
        }
    }

    @Override
    public boolean canUse() {
        long i =this.mob.level().getGameTime();
        if (i - this.lastCanUseCheck<20L){
            return false;
        }else {
            this.lastCanUseCheck = i;  // 1️更新检查时间戳
            LivingEntity livingentity = this.mob.getTarget();  // 获取目标
            if (livingentity == null) {//判断是否为空
                return false;
            } else if (!livingentity.isAlive()) {//判断是否死了
                return false;
            } else if (this.canPenalize) {
                if (--this.ticksUntilNextPathRecalculation <= 0) { //递减路径重计算倒计时 检查是不是要重写计算路
                    this.path = this.mob.getNavigation().createPath(livingentity, 0);  //getNavigation()火炮去生物导航 createpath 计算到实体路径 0精度
                    this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);//重置路径计算倒计时 4-10个刻
                    return this.path != null;  //如果存在路径就走
                }else {
                    return true;
              }
            }else {
                this.path = this.mob.getNavigation().createPath(livingentity, 0);
                return this.path != null ? true : this.mob.isWithinMeleeAttackRange(livingentity);
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
