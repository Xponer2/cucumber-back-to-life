package com.blakebr0.cucumber.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;

import io.github.xponer2.translator.network.RegistryFriendlyByteBuf;
import io.github.xponer2.translator.network.ByteBufCodecs;

public interface OutputResolver {
    ItemStack resolve();

    static OutputResolver.Item create(RegistryFriendlyByteBuf buffer) {
        return new Item(ByteBufCodecs.OPTIONAL_ITEM_STACK.decode(buffer));
    }

    class Tag implements OutputResolver {
        public static final MapCodec<Tag> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Codec.STRING.fieldOf("tag").forGetter(result -> result.tag),
                        Codec.INT.fieldOf("count").forGetter(result -> result.count)
                ).apply(builder, Tag::new)
        );

        private final String tag;
        private final int count;

        public Tag(String tag, int count) {
            this.tag = tag;
            this.count = count;
        }

        @Override
        public ItemStack resolve() {
            return TagMapper.getItemStackForTag(this.tag, this.count);
        }
    }

    class Item implements OutputResolver {
        private final ItemStack stack;

        public Item(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public ItemStack resolve() {
            return this.stack;
        }
    }
}
