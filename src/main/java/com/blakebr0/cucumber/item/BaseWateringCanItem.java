package com.blakebr0.cucumber.item;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import io.github.xponer2.translator.component.StackComponents;
import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.TooltipContext;
import com.blakebr0.cucumber.init.ModDataComponentTypes;
import com.blakebr0.cucumber.lib.Tooltips;
import com.blakebr0.cucumber.util.WateringCanUtil;

import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public class BaseWateringCanItem extends BaseItem {
    private static final Random RANDOM = new Random();

    protected final int range;
    protected final double chance;

    public BaseWateringCanItem(int range, double chance) {
        this(range, chance, p -> p);
    }

    public BaseWateringCanItem(int range, double chance, Function<ItemProperties, ItemProperties> properties) {
        super(properties.compose(p -> p.stacksTo(1).component(ModDataComponentTypes.WATERING_CAN_FILLED, false)));
        this.range = range;
        this.chance = chance;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 200;
    }

    // fill from a water source
    @Override
    public ActionResult<ItemStack> onItemRightClick(World level, EntityPlayer player, EnumHand hand) {
        var stack = player.getHeldItem(hand);

        if (isFilled(stack)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        var trace = this.rayTrace(level, player, true);
        if (trace == null || trace.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        var pos = trace.getBlockPos();
        var direction = trace.sideHit;
        if (level.isBlockModifiable(player, pos) && player.canPlayerEdit(pos.offset(direction), direction, stack)) {
            var state = level.getBlockState(pos);
            if (state.getMaterial() == Material.WATER && state.getValue(BlockLiquid.LEVEL) == 0) {
                setFilled(stack, true);
                player.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    // start watering
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World level, BlockPos pos, EnumHand hand, EnumFacing direction, float hitX, float hitY, float hitZ) {
        var stack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos.offset(direction), direction, stack))
            return EnumActionResult.FAIL;

        if (!isFilled(stack))
            return EnumActionResult.PASS;

        player.setActiveHand(hand);

        return EnumActionResult.PASS;
    }

    // machines that use items (fake players) water at once
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World level, BlockPos pos, EnumFacing direction, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (player instanceof FakePlayer) {
            return this.doWater(player.getHeldItem(hand), level, player, pos, direction);
        }

        return EnumActionResult.PASS;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entity, int remainingTicks) {
        if (remainingTicks >= 0 && entity instanceof EntityPlayer player) {
            var trace = this.rayTrace(player.world, player, true);
            if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK) {
                this.doWater(stack, player.world, player, trace.getBlockPos(), trace.sideHit);
            } else {
                entity.stopActiveHand();
            }
        } else {
            entity.stopActiveHand();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (isFilled(stack)) {
            tooltip.add(Tooltips.FILLED.build());
        } else {
            tooltip.add(Tooltips.EMPTY.build());
        }
    }

    public static boolean isFilled(ItemStack stack) {
        return StackComponents.getOrDefault(stack, ModDataComponentTypes.WATERING_CAN_FILLED, false);
    }

    public static void setFilled(ItemStack stack, boolean filled) {
        StackComponents.set(stack, ModDataComponentTypes.WATERING_CAN_FILLED, filled);
    }

    protected EnumActionResult doWater(ItemStack stack, World level, EntityPlayer player, BlockPos pos, EnumFacing direction) {
        if (player == null)
            return EnumActionResult.FAIL;

        if (!player.canPlayerEdit(pos.offset(direction), direction, stack))
            return EnumActionResult.FAIL;

        if (!isFilled(stack))
            return EnumActionResult.FAIL;

        if (!this.allowFakePlayerWatering() && player instanceof FakePlayer)
            return EnumActionResult.FAIL;

        if (!level.isRemote) {
            var cooldowns = player.getCooldownTracker();
            var item = stack.getItem();

            if (!cooldowns.hasCooldown(item)) {
                cooldowns.setCooldown(item, getThrottleTicks(player));
            } else {
                return EnumActionResult.FAIL;
            }
        }

        int range = (this.range - 1) / 2;

        for (var aoePos : BlockPos.getAllInBoxMutable(pos.add(-range, -range, -range), pos.add(range, range, range))) {
            var aoeState = level.getBlockState(aoePos);
            if (aoeState.getBlock() instanceof BlockFarmland) {
                int moisture = aoeState.getValue(BlockFarmland.MOISTURE);
                if (moisture < 7) {
                    level.setBlockState(aoePos.toImmutable(), aoeState.withProperty(BlockFarmland.MOISTURE, 7), 3);
                }
            }
        }

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                double d0 = pos.getX() + x + RANDOM.nextFloat();
                double d1 = pos.getY() + 1.0D;
                double d2 = pos.getZ() + z + RANDOM.nextFloat();

                var state = level.getBlockState(pos);
                if (state.isOpaqueCube() || state.getBlock() instanceof BlockFarmland)
                    d1 += 0.3D;

                level.spawnParticle(EnumParticleTypes.WATER_DROP, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }

        startPlayingSound(player);

        if (!level.isRemote && Math.random() <= this.chance) {
            for (var aoePos : BlockPos.getAllInBoxMutable(pos.add(-range, -range, -range), pos.add(range, range, range))) {
                var state = level.getBlockState(aoePos);
                var plantBlock = state.getBlock();

                if (plantBlock instanceof IGrowable || plantBlock == Blocks.MYCELIUM || plantBlock == Blocks.CHORUS_FLOWER) {
                    plantBlock.randomTick(level, aoePos.toImmutable(), state, RANDOM);
                }
            }
        }

        return EnumActionResult.FAIL;
    }

    protected boolean allowFakePlayerWatering() {
        return true;
    }

    /**
     * Starts playing the watering can sound for the given player on the client side
     * @param player the player
     */
    public static void startPlayingSound(EntityPlayer player) {
        if (player.world.isRemote) {
            WateringCanUtil.startPlayingSound(player);
        }
    }

    /**
     * Stops playing the watering can sound for the given player on the client side
     * @param player the player
     */
    public static void stopPlayingSound(EntityPlayer player) {
        if (player.world.isRemote) {
            WateringCanUtil.stopPlayingSound(player);
        }
    }

    private static int getThrottleTicks(EntityPlayer player) {
        return player instanceof FakePlayer ? 10 : 5;
    }
}
