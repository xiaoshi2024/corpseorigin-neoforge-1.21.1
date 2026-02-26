package com.phagens.corpseorigin.Item;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class ByWaterBucketItem extends BucketItem {
    public ByWaterBucketItem(Item.Properties properties) {
        super(Fluids.WATER, properties);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return Items.BUCKET.getDefaultInstance();
    }
}