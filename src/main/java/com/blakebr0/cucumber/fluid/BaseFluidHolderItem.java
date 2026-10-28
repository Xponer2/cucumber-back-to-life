package com.blakebr0.cucumber.fluid;

import javax.annotation.Nullable;

import com.blakebr0.cucumber.iface.IFluidHolder;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class BaseFluidHolderItem implements IFluidHandlerItem {
    private final IFluidHolder holder;
    private final ItemStack stack;

    public BaseFluidHolderItem(ItemStack stack, IFluidHolder holder) {
        this.stack = stack;
        this.holder = holder;
    }

    @Override
    public ItemStack getContainer() {
        return this.stack;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[] { new FluidTankProperties(this.holder.getFluid(this.stack), this.holder.getCapacity(this.stack)) };
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return this.holder.fill(this.stack, resource, doFill);
    }

    @Override
    @Nullable
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        var contained = this.holder.getFluid(this.stack);
        if (resource == null || contained == null || !resource.isFluidEqual(contained)) {
            return null;
        }

        return this.drain(resource.amount, doDrain);
    }

    @Override
    @Nullable
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return this.holder.drain(this.stack, maxDrain, doDrain);
    }
}
