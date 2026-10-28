package com.blakebr0.cucumber.command;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.blakebr0.cucumber.Cucumber;
import io.github.xponer2.translator.capabilities.Capabilities;
import com.blakebr0.cucumber.helper.BlockHelper;
import com.blakebr0.cucumber.util.Localizable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

/**
 * {@code /cucumber fillenergy block|hand}: fills the energy storage of the looked at block or the held item.
 * Registered in FMLServerStartingEvent (1.12.2 has no brigadier).
 */
public final class ModCommands extends CommandBase {
    @Override
    public String getName() {
        return Cucumber.MOD_ID;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/" + Cucumber.MOD_ID + " fillenergy <block|hand>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2 || !"fillenergy".equals(args[0])) {
            throw new WrongUsageException(this.getUsage(sender));
        }

        var player = getCommandSenderAsPlayer(sender);
        switch (args[1]) {
            case "block" -> {
                var level = player.world;
                var trace = BlockHelper.rayTraceBlocks(level, player);
                var energy = trace == null ? null
                        : Capabilities.EnergyStorage.BLOCK.getCapability(level, trace.getBlockPos(), trace.sideHit);
                this.fill(sender, energy, "block");
            }
            case "hand" -> {
                var stack = player.getHeldItem(EnumHand.MAIN_HAND);
                var energy = stack.isEmpty() ? null : Capabilities.EnergyStorage.ITEM.getCapability(stack);
                this.fill(sender, energy, "item");
            }
            default -> throw new WrongUsageException(this.getUsage(sender));
        }
    }

    private void fill(ICommandSender sender, @Nullable net.minecraftforge.energy.IEnergyStorage energy, String target) {
        if (energy != null) {
            if (energy.canReceive()) {
                energy.receiveEnergy(Integer.MAX_VALUE, false);
                sender.sendMessage(Localizable.of("message.cucumber.filled_energy").args(target).build());
            }
        } else {
            sender.sendMessage(Localizable.of("message.cucumber.filled_energy_error").args(target).build().withStyle(TextFormatting.RED));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "fillenergy");
        }
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, "block", "hand");
        }
        return Collections.emptyList();
    }
}
