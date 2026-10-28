package com.blakebr0.cucumber.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * After years of struggling with configuring shift-clicking in container menus, it's time to make a more readable (to me)
 * abstraction for handling this.
 */
public class QuickMover {
    private final MoveFunction moveFunc;
    private final List<MoveAttempt> attempts;

    /**
     * The current minimum slot for any added attempts to run
     */
    private int after = -1;

    public QuickMover(MoveFunction moveFunc) {
        this.moveFunc = moveFunc;
        this.attempts = new ArrayList<>();
    }

    public QuickMover add(MoveCondition condition, int start, int slots) {
        this.attempts.add(new MoveAttempt(condition, this.after, start, slots));
        return this;
    }

    public QuickMover fallback(int start, int slots) {
        this.attempts.add(new MoveAttempt((slot, stack, player) -> true, -1, start, slots));
        return this;
    }

    /**
     * Set the slot requirement for subsequently registered attempts
     */
    public QuickMover after(int slot) {
        this.after = slot;
        return this;
    }

    public boolean run(int slot, ItemStack stack, EntityPlayer player) {
        for (var attempt : this.attempts) {
            if (slot >= attempt.minSlot && attempt.condition.apply(slot, stack, player)) {
                if (!this.moveFunc.apply(stack, attempt.start, attempt.end(), attempt.inverse)) {
                    return false;
                }
            }
        }

        return true;
    }

    /** {@link net.minecraft.world.inventory.AbstractContainerMenu#moveItemStackTo(ItemStack, int, int, boolean)} */
    @SuppressWarnings("JavadocReference")
    @FunctionalInterface
    public interface MoveFunction {
        boolean apply(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection);
    }

    @FunctionalInterface
    public interface MoveCondition {
        boolean apply(int slot, ItemStack stack, EntityPlayer player);
    }

    private final static class MoveAttempt {
        private final MoveCondition condition;
        private final int minSlot;
        private final int start;
        private final int slots;
        private final boolean inverse;

        public MoveAttempt(MoveCondition condition, int minSlot, int start, int slots, boolean inverse) {
            this.condition = condition;
            this.minSlot = minSlot;
            this.start = start;
            this.slots = slots;
            this.inverse = inverse;
        }

        public MoveCondition condition() {
            return condition;
        }

        public int minSlot() {
            return minSlot;
        }

        public int start() {
            return start;
        }

        public int slots() {
            return slots;
        }

        public boolean inverse() {
            return inverse;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MoveAttempt)) return false;
            MoveAttempt that = (MoveAttempt) o;
            return java.util.Objects.equals(condition, that.condition)
                    && minSlot == that.minSlot
                    && start == that.start
                    && slots == that.slots
                    && inverse == that.inverse;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(condition, minSlot, start, slots, inverse);
        }

        @Override
        public String toString() {
            return "MoveAttempt[" + "condition=" + condition + ", " + "minSlot=" + minSlot + ", " + "start=" + start + ", " + "slots=" + slots + ", " + "inverse=" + inverse + "]";
        }

        MoveAttempt(MoveCondition condition, int minSlot, int start, int slots) {
            this(condition, minSlot, start, slots, false);
        }

        public int end() {
            return this.start + this.slots;
        }
    }
}
