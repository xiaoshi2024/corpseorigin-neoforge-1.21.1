package com.phagens.corpseorigin.Entity.EntityAI;

import com.phagens.corpseorigin.Entity.EntityAI.Vibrationsys.ModVibrationUser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class ModEntityJL extends Monster implements VibrationSystem {
    // 振动系统必需的字段
    private final DynamicGameEventListener<Listener> dynamicGameEventListener;
    private final VibrationSystem.User vibrationUser;
    private VibrationSystem.Data vibrationData;






    protected ModEntityJL(EntityType<? extends Monster> entityType, Level level, DynamicGameEventListener<Listener> dynamicGameEventListener, User vibrationUser) {
        super(entityType, level);
        this.vibrationUser=vibrationUser;
        this.vibrationData=new Data();
        this.dynamicGameEventListener=new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
    }
    @Override//和平是否消失
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }
    //AI
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
    }

    // 自定义属性
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
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
