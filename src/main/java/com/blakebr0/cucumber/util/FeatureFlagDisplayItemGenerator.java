package com.blakebr0.cucumber.util;

import java.util.Arrays;
import java.util.function.Supplier;

import io.github.xponer2.translator.item.CreativeModeTab;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface FeatureFlagDisplayItemGenerator {
    void accept(CreativeModeTab.ItemDisplayParameters parameters, Output output);

    static CreativeModeTab.DisplayItemsGenerator create(FeatureFlagDisplayItemGenerator generator) {
        return (parameters, output) -> generator.accept(parameters, Output.from(output));
    }

    interface Output extends CreativeModeTab.Output {
        static Output from(CreativeModeTab.Output output) {
            return output::accept;
        }

        default void accept(ItemStack stack, FeatureFlag... flags) {
            if (Arrays.stream(flags).allMatch(FeatureFlag::isEnabled)) {
                this.accept(stack);
            }
        }

        /**
         * An item or block holder (1.21 ItemLike).
         */
        default void accept(Supplier<?> item) {
            var stack = toStack(item.get());
            if (!stack.isEmpty()) {
                this.accept(stack);
            }
        }

        default void accept(Supplier<?> item, FeatureFlag... flags) {
            if (Arrays.stream(flags).allMatch(FeatureFlag::isEnabled)) {
                this.accept(item);
            }
        }

        static ItemStack toStack(Object object) {
            if (object instanceof Item item)
                return new ItemStack(item);
            if (object instanceof Block block)
                return new ItemStack(block);
            return ItemStack.EMPTY;
        }
    }
}
