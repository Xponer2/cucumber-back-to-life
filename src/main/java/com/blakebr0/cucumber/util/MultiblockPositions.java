package com.blakebr0.cucumber.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.util.math.BlockPos;

public class MultiblockPositions {
    private final List<BlockPos> positions;

    private MultiblockPositions(List<BlockPos> positions) {
        this.positions = positions;
    }

    public List<BlockPos> get(BlockPos pos) {
        return this.positions.stream().map(p -> pos.add(p)).collect(Collectors.toList());
    }

    public static MultiblockPositions of(List<BlockPos> positions) {
        return new MultiblockPositions(positions);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<BlockPos> positions = new ArrayList<>();

        public Builder pos(int x, int y, int z) {
            this.positions.add(new BlockPos(x, y, z));
            return this;
        }

        public MultiblockPositions build() {
            return new MultiblockPositions(this.positions);
        }
    }
}
