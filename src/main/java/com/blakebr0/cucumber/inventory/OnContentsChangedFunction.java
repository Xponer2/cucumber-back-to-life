package com.blakebr0.cucumber.inventory;

@FunctionalInterface
public interface OnContentsChangedFunction {
    void apply(int slot);
}
