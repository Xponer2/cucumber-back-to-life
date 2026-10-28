package com.blakebr0.cucumber.energy;

import net.minecraftforge.energy.EnergyStorage;

public class BaseEnergyStorage extends EnergyStorage {
    private final Runnable onContentsChanged;

    public BaseEnergyStorage(int capacity, Runnable onContentsChanged) {
        this(capacity, capacity, capacity, onContentsChanged);
    }

    public BaseEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onContentsChanged) {
        this(capacity, maxReceive, maxExtract, 0, onContentsChanged);
    }

    public BaseEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy, Runnable onContentsChanged) {
        super(capacity, maxReceive, maxExtract, energy);
        this.onContentsChanged = onContentsChanged;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        var received = super.receiveEnergy(maxReceive, simulate);

        if (!simulate && received != 0 && this.onContentsChanged != null)
            this.onContentsChanged.run();

        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        var extracted = super.extractEnergy(maxExtract, simulate);

        if (!simulate && extracted != 0 && this.onContentsChanged != null)
            this.onContentsChanged.run();

        return extracted;
    }

    // NeoForge's EnergyStorage saves its energy as an int tag (1.12.2 Forge's doesn't save itself)
    public net.minecraft.nbt.NBTBase serializeNBT() {
        return new net.minecraft.nbt.NBTTagInt(this.getEnergyStored());
    }

    public void deserializeNBT(net.minecraft.nbt.NBTBase tag) {
        if (tag instanceof net.minecraft.nbt.NBTTagInt value) {
            this.energy = value.getInt();
        }
    }

    public void setEnergyStored(int energy) {
        this.energy = energy;

        if (this.onContentsChanged != null)
            this.onContentsChanged.run();
    }
}
