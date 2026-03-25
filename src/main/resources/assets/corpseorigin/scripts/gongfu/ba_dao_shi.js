// 霸刀势 - 强力刀法功法
function activate(player, world, data) {
    // 1. 获取玩家朝向
    var look = player.getLookAngle();

    // 2. 创建霸气刀光效果
    for (var i = 0; i < 25; i++) {
        world.spawnParticle(
            "minecraft:crit",
            player.getX() + look.x * i * 0.6,
            player.getY() + player.getEyeHeight() + look.y * i * 0.6,
            player.getZ() + look.z * i * 0.6,
            look.x * 0.4, look.y * 0.4, look.z * 0.4
        );
    }

    // 3. 对前方生物造成强力伤害
    var targets = world.getEntitiesOfClass(
        Java.type("net.minecraft.world.entity.LivingEntity").class,
        player.getBoundingBox().expandTowards(look.scale(18)).inflate(3.5)
    );

    for (var i = 0; i < targets.size(); i++) {
        var target = targets.get(i);
        if (target != player && !target.isAlliedTo(player)) {
            target.hurt(world.damageSources().playerAttack(player), 28.0);
            target.push(look.x * 3.5, look.y * 1.2, look.z * 3.5);
            
            // 添加击退效果
            target.addEffect(new Packages.net.minecraft.world.effect.MobEffectInstance(
                Packages.net.minecraft.world.effect.MobEffects.WEAKNESS,
                80, // 4秒
                1,   // 等级：II级
                false, false
            ));
        }
    }

    // 4. 播放霸气音效
    world.playSound(
        null, player.getX(), player.getY(), player.getZ(),
        "minecraft:entity.player.attack.strong",
        "players", 1.3, 0.7
    );

    return true;
}