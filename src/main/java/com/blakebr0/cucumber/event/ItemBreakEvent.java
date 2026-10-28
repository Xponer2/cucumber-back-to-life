package com.blakebr0.cucumber.event;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ItemBreakEvent extends Event {
    private final ItemStack stack;
    private final int amount;
    private final WorldServer level;
    private final EntityLivingBase entity;

    public ItemBreakEvent(ItemStack stack, int amount, WorldServer level, @Nullable EntityLivingBase entity) {
        this.stack = stack;
        this.amount = amount;
        this.level = level;
        this.entity = entity;
    }

    /**
     * @return a COPY of the ItemStack before it is destroyed
     */
    public ItemStack getItemStack() {
        return this.stack;
    }

    public Item getItem() {
        return this.stack.getItem();
    }

    public int getAmount() {
        return this.amount;
    }

    public WorldServer getLevel() {
        return this.level;
    }

    public EntityLivingBase getEntity() {
        return this.entity;
    }
}
