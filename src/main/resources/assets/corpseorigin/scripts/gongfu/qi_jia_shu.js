// 气甲术 - 防御型功法
function activate(player, world, data) {
    // 1. 创建气甲护盾效果
    for (var i = 0; i < 30; i++) {
        world.spawnParticle(
            "minecraft:cloud",
            player.getX() + (Math.random() - 0.5) * 3,
            player.getY() + player.getEyeHeight() + (Math.random() - 0.5) * 2,
            player.getZ() + (Math.random() - 0.5) * 3,
            0, 0, 0
        );
    }

    // 2. 给玩家添加临时护甲效果
    player.addEffect(new Packages.net.minecraft.world.effect.MobEffectInstance(
        Packages.net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
        200, // 持续时间：10秒
        1,   // 等级：II级
        false, false
    ));

    // 3. 播放音效
    world.playSound(
        null, player.getX(), player.getY(), player.getZ(),
        "minecraft:block.anvil.place",
        "players", 0.8, 1.2
    );

    return true;  // 返回 true 表示成功
}