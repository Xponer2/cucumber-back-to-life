package com.blakebr0.cucumber.crafting.ingredient;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import io.github.xponer2.translator.network.ByteBufCodecs;
import io.github.xponer2.translator.network.RegistryFriendlyByteBuf;
import io.github.xponer2.translator.network.StreamCodec;
import io.github.xponer2.translator.recipe.ingredient.ICustomIngredient;
import io.github.xponer2.translator.recipe.ingredient.IngredientType;
import io.github.xponer2.translator.util.Codecs;
import com.blakebr0.cucumber.init.ModIngredientTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

/**
 * An item or a tag with a minimum count ({@code {"item": ..., "count": n}} or {@code {"tag": ..., "count": n}}, the
 * 1.21 Ingredient.Value map plus a count).
 */
public class IngredientWithCount implements ICustomIngredient, Predicate<ItemStack> {
    public static final IngredientWithCount EMPTY = new IngredientWithCount(Ingredient.EMPTY, 0);
    public static final MapCodec<IngredientWithCount> MAP_CODEC = RecordCodecBuilder.<IngredientWithCount>mapCodec(builder ->
            builder.group(
                    Codecs.ITEM.optionalFieldOf("item").forGetter(ingredient -> Optional.ofNullable(ingredient.item)),
                    Codecs.RESOURCE_LOCATION.optionalFieldOf("tag").forGetter(ingredient -> Optional.ofNullable(ingredient.tag)),
                    Codec.INT.fieldOf("count").forGetter(ingredient -> ingredient.count)
            ).apply(builder, (item, tag, count) -> item.isPresent()
                    ? new IngredientWithCount(item.get(), count)
                    : new IngredientWithCount(tag.orElse(null), count))
    ).flatXmap(i -> i.item == null && i.tag == null && i != EMPTY ? DataResult.error("An ingredient with count needs an item or a tag") : DataResult.success(i), DataResult::success);
    public static final Codec<IngredientWithCount> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientWithCount> STREAM_CODEC = StreamCodec.of(
            (buffer, ingredient) -> {
                var items = ingredient.getItems().toArray(ItemStack[]::new);
                buffer.writeVarInt(items.length);
                for (var item : items) {
                    ByteBufCodecs.ITEM_STACK.encode(buffer, item);
                }
                buffer.writeVarInt(ingredient.count);
            },
            buffer -> {
                var size = buffer.readVarInt();
                var items = NonNullList.withSize(size, ItemStack.EMPTY);
                for (var i = 0; i < size; i++) {
                    items.set(i, ByteBufCodecs.ITEM_STACK.decode(buffer));
                }
                var count = buffer.readVarInt();
                return new IngredientWithCount(Ingredient.fromStacks(items.toArray(new ItemStack[0])), count);
            }
    );

    private final Ingredient ingredient;
    private final int count;
    @Nullable
    private final Item item;
    @Nullable
    private final ResourceLocation tag;

    public IngredientWithCount(Ingredient ingredient, int count) {
        this(ingredient, count, null, null);
    }

    public IngredientWithCount(Item item, int count) {
        this(Ingredient.fromItem(item), count, item, null);
    }

    public IngredientWithCount(@Nullable ResourceLocation tag, int count) {
        this(tag == null ? Ingredient.EMPTY : Codecs.tagIngredient(tag), count, null, tag);
    }

    private IngredientWithCount(Ingredient ingredient, int count, @Nullable Item item, @Nullable ResourceLocation tag) {
        this.ingredient = ingredient;
        this.count = count;
        this.item = item;
        this.tag = tag;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null)
            return false;

        return stack.getCount() >= this.count && this.ingredient.apply(stack);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Arrays.stream(this.ingredient.getMatchingStacks());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return ModIngredientTypes.WITH_COUNT.get();
    }

    public int getCount() {
        return this.count;
    }

    public static Ingredient of(ItemStack item, int count) {
        return new IngredientWithCount(Ingredient.fromStacks(item), count).toVanilla();
    }
}
