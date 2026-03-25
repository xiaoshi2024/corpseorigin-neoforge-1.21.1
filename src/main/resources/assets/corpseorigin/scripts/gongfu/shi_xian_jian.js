// 诗仙剑 - 高级剑法功法
function activate(player, world, data) {
    // 1. 获取玩家朝向
    var look = player.getLookAngle();

    // 2. 创建剑气效果
    for (var i = 0; i < 40; i++) {
        world.spawnParticle(
            "minecraft:enchanted_hit",
            player.getX() + look.x * i * 0.8,
            player.getY() + player.getEyeHeight() + look.y * i * 0.8,
            player.getZ() + look.z * i * 0.8,
            look.x * 0.3, look.y * 0.3, look.z * 0.3
        );
    }

    // 3. 对前方生物造成高额伤害
    var targets = world.getEntitiesOfClass(
        Java.type("net.minecraft.world.entity.LivingEntity").class,
        player.getBoundingBox().expandTowards(look.scale(25)).inflate(4)
    );

    for (var i = 0; i < targets.size(); i++) {
        var target = targets.get(i);
        if (target != player && !target.isAlliedTo(player)) {
            target.hurt(world.damageSources().magic(), 25.0);
            target.push(look.x * 4, look.y * 1, look.z * 4);
            
            // 添加缓慢效果
            target.addEffect(new Packages.net.minecraft.world.effect.MobEffectInstance(
                Packages.net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                100, // 5秒
                1,   // 等级：II级
                false, false
            ));
        }
    }

    // 4. 播放音效
    world.playSound(
        null, player.getX(), player.getY(), player.getZ(),
        "minecraft:entity.player.attack.crit",
        "players", 1.2, 0.8
    );

    return true;
}