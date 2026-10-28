package com.blakebr0.cucumber.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import io.github.xponer2.translator.menu.ContainerData;

public class ContainerDataBuilder {
    private final List<DataSlotGetterSetter> data;

    private ContainerDataBuilder() {
        this.data = new ArrayList<>();
    }

    public static ContainerDataBuilder builder() {
        return new ContainerDataBuilder();
    }

    public ContainerDataBuilder sync(IntSupplier getter, IntConsumer setter) {
        this.data.add(new DataSlotGetterSetter(getter, setter));
        return this;
    }

    public ContainerData build() {
        return new ContainerData() {
            private final List<DataSlotGetterSetter> data = ContainerDataBuilder.this.data;

            @Override
            public int get(int index) {
                return this.data.get(index).getter.getAsInt();
            }

            @Override
            public void set(int index, int value) {
                this.data.get(index).setter.accept(value);
            }

            @Override
            public int getCount() {
                return this.data.size();
            }
        };
    }

    final static class DataSlotGetterSetter {
        private final IntSupplier getter;
        private final IntConsumer setter;

        public DataSlotGetterSetter(IntSupplier getter, IntConsumer setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public IntSupplier getter() {
            return getter;
        }

        public IntConsumer setter() {
            return setter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DataSlotGetterSetter)) return false;
            DataSlotGetterSetter that = (DataSlotGetterSetter) o;
            return java.util.Objects.equals(getter, that.getter)
                    && java.util.Objects.equals(setter, that.setter);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(getter, setter);
        }

        @Override
        public String toString() {
            return "DataSlotGetterSetter[" + "getter=" + getter + ", " + "setter=" + setter + "]";
        }
 }
}
