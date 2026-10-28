package com.blakebr0.cucumber.util;

import java.text.NumberFormat;

import io.github.xponer2.translator.text.LiteralComponent;
import io.github.xponer2.translator.text.MutableComponent;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public final class Formatting {
    public static MutableComponent number(Object value) {
        return new LiteralComponent(NumberFormat.getInstance().format(value));
    }

    public static MutableComponent percent(Object value) {
        return number(value).append("%");
    }

    public static MutableComponent energy(Object value) {
        return number(value).append(" FE");
    }

    public static MutableComponent perTick(Object value) {
        return number(value).append(" /t");
    }

    public static MutableComponent energyPerTick(Object value) {
        return number(value).append(" FE/t");
    }

    public static MutableComponent itemWithCount(ItemStack stack) {
        return itemWithCount(stack, stack.getCount());
    }

    public static MutableComponent itemWithCount(ItemStack stack, int count) {
        return new LiteralComponent(count + "x " + stack.getDisplayName());
    }
}
