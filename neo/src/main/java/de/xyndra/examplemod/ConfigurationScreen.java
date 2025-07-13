/*
 * Originally:
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 *
 * Modified (AI-generated except for some small fixes): Xyndra
 */

package de.xyndra.examplemod;

import com.google.common.collect.ImmutableList;
import de.xyndra.examplemod.utils.ProjectProps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.config.NeoForgeClientConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ConfigurationScreen extends OptionsSubScreen {
    public static class TranslationChecker {
        private static final Logger LOGGER = LogManager.getLogger();
        private final Set<String> untranslatables = new HashSet<>();
        private final Set<String> untranslatablesWithFallback = new HashSet<>();

        public String check(final String translationKey) {
            if (!I18n.exists(translationKey)) {
                untranslatables.add(translationKey);
            }
            return translationKey;
        }

        public String check(final String translationKey, final String fallback) {
            if (!I18n.exists(translationKey)) {
                untranslatablesWithFallback.add(translationKey);
                return check(fallback);
            }
            return translationKey;
        }

        public void finish() {
            if (NeoForgeClientConfig.INSTANCE.logUntranslatedConfigurationWarnings.get() && !FMLLoader.isProduction() && (!untranslatables.isEmpty() || !untranslatablesWithFallback.isEmpty())) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("""
                        \n	Dev warning - Untranslated configuration keys encountered. Please translate your configuration keys so users can properly configure your mod.
                        """);
                if (!untranslatables.isEmpty()) {
                    stringBuilder.append("\nUntranslated keys:");
                    for (String key : untranslatables) {
                        stringBuilder.append("\n  \"").append(key).append("\": \"\",");
                    }
                }
                if (!untranslatablesWithFallback.isEmpty()) {
                    stringBuilder.append("\nThe following keys have fallbacks. Please check if those are suitable, and translate them if they're not.");
                    for (String key : untranslatablesWithFallback) {
                        stringBuilder.append("\n  \"").append(key).append("\": \"\",");
                    }
                }

                LOGGER.warn(stringBuilder);
            }
            untranslatables.clear();
        }
    }

    private static final String LANG_PREFIX = "neoforge.configuration.uitext.";
    private static final MutableComponent EMPTY_LINE = Component.literal("\n\n");

    public static final Component UNSUPPORTED_ELEMENT = Component.translatable(LANG_PREFIX + "unsupportedelement").withStyle(ChatFormatting.RED);
    public static final Component UNDO = Component.translatable(LANG_PREFIX + "undo");
    public static final Component UNDO_TOOLTIP = Component.translatable(LANG_PREFIX + "undo.tooltip");
    public static final Component RESET = Component.translatable(LANG_PREFIX + "reset");
    public static final Component RESET_TOOLTIP = Component.translatable(LANG_PREFIX + "reset.tooltip");

    private static final TranslationChecker translationChecker = new TranslationChecker();

    private boolean changed = false;
    @Nullable
    private Button undoButton, resetButton;
    private final Button doneButton = Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(Button.SMALL_WIDTH).build();
    private final UndoManager undoManager = new UndoManager();

    public record Element(@Nullable Component name, @Nullable Component tooltip, @Nullable AbstractWidget widget, @Nullable OptionInstance<?> option, boolean undoable) {
        public Element(@Nullable final Component name, @Nullable final Component tooltip, final AbstractWidget widget) {
            this(name, tooltip, widget, null, true);
        }

        public Element(@Nullable final Component name, @Nullable final Component tooltip, final AbstractWidget widget, boolean undoable) {
            this(name, tooltip, widget, null, undoable);
        }

        public Element(final Component name, final Component tooltip, final OptionInstance<?> option) {
            this(name, tooltip, null, option, true);
        }

        public Element(final Component name, final Component tooltip, final OptionInstance<?> option, boolean undoable) {
            this(name, tooltip, null, option, undoable);
        }

        public AbstractWidget getWidget(final Options options) {
            return widget != null ? widget : option.createButton(options);
        }
    }

    public ConfigurationScreen(final ModContainer mod, final Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable(translationChecker.check(mod.getModId() + ".configuration.title", LANG_PREFIX + "title"), mod.getModInfo().getDisplayName()));
    }

    private MutableComponent getTranslationComponent(final String key) {
        return Component.translatable(translationChecker.check(ProjectProps.MOD_ID + ".configuration." + key));
    }

    private <T> OptionInstance.TooltipSupplier<T> getTooltip(final String key) {
        return OptionInstance.cachedConstantTooltip(getTooltipComponent(key));
    }

    private Component getTooltipComponent(final String key) {
        final String tooltipKey = ProjectProps.MOD_ID + ".configuration." + key + ".tooltip";
        MutableComponent component = Component.empty().append(getTranslationComponent(key).withStyle(ChatFormatting.BOLD));
        if (I18n.exists(tooltipKey)) {
            component = component.append(EMPTY_LINE).append(Component.translatable(tooltipKey));
        }
        return component;
    }

    private void onChanged(final String key) {
        changed = true;
    }

    @Override
    protected void addOptions() {
        rebuild();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void rebuild() {
        if (list != null) {
            list.children().clear();
            boolean hasUndoableElements = false;

            // Always get fresh values from globals
            final Map<String, ConfigOption<?>> configOptions = Globals.INSTANCE.getConfigOptions();

            final List<Element> elements = new ArrayList<>();
            for (Map.Entry<String, ConfigOption<?>> entry : configOptions.entrySet()) {
                final String key = entry.getKey();
                final ConfigOption<?> option = entry.getValue();

                var element = switch (option) {
                    case BooleanConfigOption boolOption -> createBooleanValue(key, boolOption);
                    case IntConfigOption intOption -> createIntegerValue(key, intOption);
                    default -> createOtherValue(key, option);
                };

                if (element != null) {
                    elements.add(element);
                }
            }

            for (final Element element : elements) {
                if (element != null) {
                    if (element.name() == null) {
                        list.addSmall(new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.empty(), font), element.getWidget(options));
                    } else {
                        final StringWidget label = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, element.name, font).alignLeft();
                        label.setTooltip(Tooltip.create(element.tooltip));
                        list.addSmall(label, element.getWidget(options));
                    }
                    hasUndoableElements |= element.undoable;
                }
            }

            if (hasUndoableElements && undoButton == null) {
                createUndoButton();
                createResetButton();
            }
        }
    }

    private boolean isNonDefault(ConfigOption<?> option) {
        return !Objects.equals(option.getValue(), option.getDefaultValue());
    }

    private boolean isAnyNondefault() {
        final Map<String, ConfigOption<?>> configOptions = Globals.INSTANCE.getConfigOptions();
        for (ConfigOption<?> option : configOptions.values()) {
            if (isNonDefault(option)) {
                return true;
            }
        }
        return false;
    }

    private Element createBooleanValue(final String key, final BooleanConfigOption option) {
        return new Element(getTranslationComponent(key), getTooltipComponent(key),
                new OptionInstance<>(key, getTooltip(key), OptionInstance.BOOLEAN_TO_STRING,
                        new Custom.BooleanValues(), option.getValue(), newValue -> {
                            if (!newValue.equals(option.getValue())) {
                                undoManager.add(v -> {
                                    option.setValue(v);
                                    onChanged(key);
                                }, newValue, v -> {
                                    option.setValue(v);
                                    onChanged(key);
                                }, option.getValue());
                            }
                        }));
    }

    private Element createIntegerValue(final String key, final IntConfigOption option) {
        final EditBox box = new EditBox(font, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, getTranslationComponent(key));
        box.setEditable(true);
        box.setTooltip(Tooltip.create(getTooltipComponent(key)));
        box.setMaxLength(10);
        box.setValue(option.getValue().toString());
        box.setResponder(newValue -> {
            try {
                int intValue = Integer.parseInt(newValue);
                if (option.predicate(intValue)) {
                    if (intValue != option.getValue()) {
                        undoManager.add(v -> {
                            option.setValue(v);
                            onChanged(key);
                        }, intValue, v -> {
                            option.setValue(v);
                            onChanged(key);
                        }, option.getValue());
                    }
                    box.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
                    return;
                }
            } catch (NumberFormatException e) {
                // Invalid number
            }
            box.setTextColor(0xFFFF0000);
        });
        return new Element(getTranslationComponent(key), getTooltipComponent(key), box);
    }

    @Nullable
    private Element createOtherValue(final String key, final ConfigOption<?> option) {
        final StringWidget label = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, Component.literal(Objects.toString(option.getValue())), font).alignLeft();
        label.setTooltip(Tooltip.create(UNSUPPORTED_ELEMENT));
        return new Element(getTranslationComponent(key), getTooltipComponent(key), label, false);
    }

    public static class Custom {
        public static class BooleanValues implements OptionInstance.ValueSet<Boolean> {
            private final List<Boolean> values = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);

            @Override
            public Function<OptionInstance<Boolean>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<Boolean> tooltip, Options options, int x, int y, int width, Consumer<Boolean> target) {
                return optionsInstance -> CycleButton.builder(optionsInstance.toString)
                        .withValues(CycleButton.ValueListSupplier.create(this.values))
                        .withTooltip(tooltip)
                        .displayOnlyValue()
                        .withInitialValue(optionsInstance.get())
                        .create(x, y, width, 20, optionsInstance.caption, (source, newValue) -> {
                            optionsInstance.set(newValue);
                            options.save();
                            target.accept(newValue);
                        });
            }

            @Override
            public java.util.Optional<Boolean> validateValue(Boolean value) {
                return values.contains(value) ? java.util.Optional.of(value) : java.util.Optional.empty();
            }

            @Override
            public com.mojang.serialization.Codec<Boolean> codec() {
                return null;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int p_281550_, int p_282878_, float p_282465_) {
        setUndoButtonstate(undoManager.canUndo());
        setResetButtonstate(isAnyNondefault());
        super.render(graphics, p_281550_, p_282878_, p_282465_);
    }

    @Override
    protected void addFooter() {
        if (undoButton != null || resetButton != null) {
            LinearLayout linearlayout = layout.addToFooter(LinearLayout.horizontal().spacing(8));
            if (undoButton != null) {
                linearlayout.addChild(undoButton);
            }
            if (resetButton != null) {
                linearlayout.addChild(resetButton);
            }
            linearlayout.addChild(doneButton);
        } else {
            super.addFooter();
        }
    }

    private void createUndoButton() {
        undoButton = Button.builder(UNDO, button -> {
            undoManager.undo();
            rebuild();
        }).tooltip(Tooltip.create(UNDO_TOOLTIP)).width(Button.SMALL_WIDTH).build();
        undoButton.active = false;
    }

    private void setUndoButtonstate(boolean state) {
        if (undoButton != null) {
            undoButton.active = state;
        }
    }

    private void createResetButton() {
        resetButton = Button.builder(RESET, button -> {
            final Map<String, ConfigOption<?>> configOptions = Globals.INSTANCE.getConfigOptions();
            List<UndoManager.Step<?>> steps = new ArrayList<>();
            for (Map.Entry<String, ConfigOption<?>> entry : configOptions.entrySet()) {
                final String key = entry.getKey();
                final ConfigOption<?> option = entry.getValue();
                if (isNonDefault(option)) {
                    steps.add(undoManager.step(v -> {
                        ((ConfigOption) option).setValue(v);
                        onChanged(key);
                    }, option.getDefaultValue(), v -> {
                        ((ConfigOption) option).setValue(v);
                        onChanged(key);
                    }, option.getValue()));
                }
            }
            undoManager.add(steps);
            rebuild();
        }).tooltip(Tooltip.create(RESET_TOOLTIP)).width(Button.SMALL_WIDTH).build();
    }

    private void setResetButtonstate(boolean state) {
        if (resetButton != null) {
            resetButton.active = state;
        }
    }

    @Override
    public void onClose() {
        if (changed) {
            saveConfig();
        }
        translationChecker.finish();
        super.onClose();
    }

    private void saveConfig() {
        try {
            final Map<String, ConfigOption<?>> configOptions = Globals.INSTANCE.getConfigOptions();
            String json = ConfigOptionsKt.serializeJson(configOptions);
            java.io.File configFile = new java.io.File(net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().toFile(), ProjectProps.MOD_ID + ".json");
            java.nio.file.Files.write(configFile.toPath(), json.getBytes());
        } catch (Exception e) {
            Globals.INSTANCE.getLogger().error("Failed to save config", e);
        }
    }

    public static final class UndoManager {
        public record Step<T>(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
            private void runUndo() {
                undo.accept(oldValue);
            }

            private void runRedo() {
                run.accept(newValue);
            }
        }

        private final List<Step<?>> undos = new ArrayList<>();

        public void undo() {
            if (canUndo()) {
                Step<?> step = undos.removeLast();
                step.runUndo();
            }
        }

        private void add(Step<?> step) {
            undos.add(step);
            step.runRedo();
        }

        public <T> Step<T> step(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
            return new Step<>(run, newValue, undo, oldValue);
        }

        public <T> void add(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
            add(step(run, newValue, undo, oldValue));
        }

        public void add(final List<Step<?>> steps) {
            add(new Step<>(n -> steps.forEach(Step::runRedo), null, n -> steps.forEach(Step::runUndo), null));
        }

        public boolean canUndo() {
            return !undos.isEmpty();
        }

    }
}
