// 游鲨刀法 - 灵活刀法功法
function activate(player, world, data) {
    // 1. 获取玩家朝向
    var look = player.getLookAngle();

    // 2. 创建游鲨游动效果
    for (var i = 0; i < 35; i++) {
        world.spawnParticle(
            "minecraft:bubble",
            player.getX() + look.x * i * 0.7,
            player.getY() + player.getEyeHeight() + look.y * i * 0.7,
            player.getZ() + look.z * i * 0.7,
            (Math.random() - 0.5) * 0.3,
            (Math.random() - 0.5) * 0.3,
            (Math.random() - 0.5) * 0.3
        );
    }

    // 3. 给玩家添加速度增益
    player.addEffect(new Packages.net.minecraft.world.effect.MobEffectInstance(
        Packages.net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
        150, // 7.5秒
        1,   // 等级：II级
        false, false
    ));

    // 4. 对前方生物造成快速连击
    var targets = world.getEntitiesOfClass(
        Java.type("net.minecraft.world.entity.LivingEntity").class,
        player.getBoundingBox().expandTowards(look.scale(12)).inflate(2.5)
    );

    for (var i = 0; i < targets.size(); i++) {
        var target = targets.get(i);
        if (target != player && !target.isAlliedTo(player)) {
            target.hurt(world.damageSources().playerAttack(player), 18.0);
            
            // 快速连击效果
            for (var j = 0; j < 2; j++) {
                target.hurt(world.damageSources().playerAttack(player), 8.0);
            }
        }
    }

    // 5. 播放水流音效
    world.playSound(
        null, player.getX(), player.getY(), player.getZ(),
        "minecraft:block.water.ambient",
        "players", 1.1, 1.0
    );

    return true;
}