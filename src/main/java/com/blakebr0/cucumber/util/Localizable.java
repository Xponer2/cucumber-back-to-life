package com.blakebr0.cucumber.util;

import io.github.xponer2.translator.text.LiteralComponent;
import io.github.xponer2.translator.text.MutableComponent;
import io.github.xponer2.translator.text.TranslatableComponent;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class Localizable {
    private final String key;
    private final TextFormatting defaultColor;

    protected Localizable(String key) {
        this(key, null);
    }

    protected Localizable(String key, TextFormatting defaultColor) {
        this.key = key;
        this.defaultColor = defaultColor;
    }

    public static Localizable of(String key) {
        return new Localizable(key);
    }

    public static Localizable of(String key, TextFormatting defaultColor) {
        return new Localizable(key, defaultColor);
    }

    public String getKey() {
        return this.key;
    }

    public TextFormatting getDefaultColor() {
        return this.defaultColor;
    }

    public LocalizableBuilder args(Object... args) {
        return this.builder().args(args);
    }

    public LocalizableBuilder color(TextFormatting color) {
        return this.builder().color(color);
    }

    public LocalizableBuilder prepend(String text) {
        return this.builder().prepend(text);
    }

    public MutableComponent build() {
        return this.builder().build();
    }

    public String buildString() {
        return this.builder().buildString();
    }

    private LocalizableBuilder builder() {
        return new LocalizableBuilder(this.key).color(this.defaultColor);
    }

    public static class LocalizableBuilder {
        private final String key;
        private Object[] args = new Object[0];
        private TextFormatting color;
        private String prependText = "";
        private String appendText = "";

        public LocalizableBuilder(String key) {
            this.key = key;
        }

        public LocalizableBuilder args(Object... args) {
            this.args = args;
            return this;
        }

        public LocalizableBuilder color(TextFormatting color) {
            this.color = color;
            return this;
        }

        public LocalizableBuilder prepend(String text) {
            this.prependText += text;
            return this;
        }

        public LocalizableBuilder append(String text) {
            this.appendText = text;
            return this;
        }

        public MutableComponent build() {
            MutableComponent component = new TranslatableComponent(this.key, this.args);

            if (!this.prependText.isEmpty()) {
                component = new LiteralComponent(this.prependText).append(component);
            }

            if (!this.appendText.isEmpty()) {
                component.append(this.appendText);
            }

            if (this.color != null) {
                component.withStyle(this.color);
            }

            return component;
        }

        public String buildString() {
            return this.build().getUnformattedText();
        }
    }
}
