// 奥特曼光线攻击
function activate(player, world, data) {
    // 1. 获取玩家朝向
    var look = player.getLookAngle();

    // 2. 创建光粒子效果
    for (var i = 0; i < 50; i++) {
        world.spawnParticle(
            "minecraft:end_rod",
            player.getX(),
            player.getY() + player.getEyeHeight(),
            player.getZ(),
            look.x * 2,
            look.y * 2,
            look.z * 2
        );
    }

    // 3. 对前方生物造成伤害
    var targets = world.getEntitiesOfClass(
        Java.type("net.minecraft.world.entity.LivingEntity").class,
        player.getBoundingBox().expandTowards(look.scale(20)).inflate(2)
    );

    for (var i = 0; i < targets.size(); i++) {
        var target = targets.get(i);
        if (target != player && !target.isAlliedTo(player)) {
            target.hurt(world.damageSources().magic(), 20.0);
            target.push(look.x * 3, look.y * 1, look.z * 3);
        }
    }

    // 4. 播放音效
    world.playSound(
        null, player.getX(), player.getY(), player.getZ(),
        "minecraft:entity.ender_dragon.growl",
        "players", 1.0, 0.8
    );

    return true;
}