package com.phagens.corpseorigin.Entity.EntityAI;

import com.phagens.corpseorigin.Entity.EntityAI.JLAI.ModFollow;
import com.phagens.corpseorigin.Entity.EntityAI.Vibrationsys.ModVibrationUser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class ModEntityJL extends Monster implements VibrationSystem {
    // 振动系统必需的字段
    private final DynamicGameEventListener<Listener> dynamicGameEventListener;
    private final VibrationSystem.User vibrationUser;
    private VibrationSystem.Data vibrationData;






    protected ModEntityJL(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.vibrationUser=new ModVibrationUser( this);
        this.vibrationData=new VibrationSystem.Data();
        this.dynamicGameEventListener=new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
    }
    @Override//和平是否消失
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }
    //AI
    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(2,new ModFollow(this,1.0D,true));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.addBehaviourGoals();


    }

    // 自定义属性
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()  //这里暂时复制的僵尸的
                .add(Attributes.FOLLOW_RANGE, 35.0)    // 跟随距离35格
                .add(Attributes.MOVEMENT_SPEED, 0.23)   // 移动速度
                .add(Attributes.ATTACK_DAMAGE, 3.0)     // 攻击伤害
                .add(Attributes.ARMOR, 2.0)             // 护甲值
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE); // 召唤援军几率

    }

    protected void addBehaviourGoals() {
        // 攻击目标
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false));

        // 攻击行为
        //暂时不屑


    }


    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide){
            VibrationSystem.Ticker.tick(this.level(),this.vibrationData, this.vibrationUser);
        }
    }

    @Override//获取振动数据
    public Data getVibrationData() {
        return this.vibrationData;
    }

    @Override//获取振动用户
    public User getVibrationUser() {
        return this.vibrationUser;
    }
}
