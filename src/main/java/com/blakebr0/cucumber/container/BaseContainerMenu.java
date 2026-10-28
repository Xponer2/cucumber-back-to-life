package com.blakebr0.cucumber.container;

import io.github.xponer2.translator.menu.AbstractContainerMenu;
import io.github.xponer2.translator.menu.MenuType;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class BaseContainerMenu extends AbstractContainerMenu {
    private final BlockPos pos;

    protected BaseContainerMenu(MenuType<?> menu, int id, BlockPos pos) {
        super(menu, id);
        this.pos = pos;
    }

    @Override
    public ItemStack quickMoveStack(EntityPlayer player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(EntityPlayer player) {
        return player.getDistanceSq(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64;
    }

    public BlockPos getPos() {
        return this.pos;
    }
}
