package com.astralis.lowdurabilitywarning;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class LowDurabilityConfigScreen {

    private static boolean prevEnableWarnings = LowDurabilityWarningMod.config.enableWarnings;

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Low Durability Warning Config"));

        builder.setSavingRunnable(() -> {
            ConfigManager.save(LowDurabilityWarningMod.config);
        });

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Enable Warnings"), LowDurabilityWarningMod.config.enableWarnings)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> LowDurabilityWarningMod.config.enableWarnings = newValue)
                .setYesNoTextSupplier(value -> {
                    if (value && !prevEnableWarnings) {
                        LowDurabilityWarningMod.triggerRealAlerts();
                    }
                    prevEnableWarnings = value;

                    return value
                            ? Text.literal("Yes").styled(style -> style.withColor(0xFFFF00))
                            : Text.literal("No").styled(style -> style.withColor(0xFF0000));
                })
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Include Tools"), LowDurabilityWarningMod.config.includeTools)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> LowDurabilityWarningMod.config.includeTools = newValue)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Include Armor"), LowDurabilityWarningMod.config.includeArmor)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> LowDurabilityWarningMod.config.includeArmor = newValue)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Include Elytra"), LowDurabilityWarningMod.config.includeElytra)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> LowDurabilityWarningMod.config.includeElytra = newValue)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Only Enchanted Items"), LowDurabilityWarningMod.config.onlyEnchanted)
                .setDefaultValue(false)
                .setSaveConsumer(newValue -> LowDurabilityWarningMod.config.onlyEnchanted = newValue)
                .build());

        general.addEntry(entryBuilder
                .startStrField(getStyledLabel("Alert Text",
                                LowDurabilityWarningMod.config.alertText, "has low durability"),
                        LowDurabilityWarningMod.config.alertText)
                .setDefaultValue("has low durability")
                .setSaveConsumer(newValue -> LowDurabilityWarningMod.config.alertText = newValue)
                .build());

        general.addEntry(entryBuilder
                .startFloatField(getStyledLabel("Alert Volume",
                                LowDurabilityWarningMod.config.volume, 0.3f),
                        LowDurabilityWarningMod.config.volume)
                .setDefaultValue(0.3f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> LowDurabilityWarningMod.config.volume = newValue)
                .build());

        general.addEntry(entryBuilder
                .startIntField(getStyledLabel("First Warning",
                                LowDurabilityWarningMod.config.firstWarning, 20),
                        LowDurabilityWarningMod.config.firstWarning)
                .setDefaultValue(20)
                .setMin(1)
                .setMax(100)
                .build());

        general.addEntry(entryBuilder
                .startIntField(getStyledLabel("Second Warning",
                                LowDurabilityWarningMod.config.secondWarning, 10),
                        LowDurabilityWarningMod.config.secondWarning)
                .setDefaultValue(10)
                .setMin(1)
                .setMax(100)
                .build());

        general.addEntry(entryBuilder
                .startIntField(getStyledLabel("Third Warning",
                                LowDurabilityWarningMod.config.thirdWarning, 5),
                        LowDurabilityWarningMod.config.thirdWarning)
                .setDefaultValue(5)
                .setMin(1)
                .setMax(100)
                .build());

        return builder.build();
    }

    private static Text getStyledLabel(String base, int current, int defaultValue) {
        if (current == defaultValue) {
            return Text.literal(base);
        } else {
            return Text.literal(base).styled(style -> style.withItalic(true));
        }
    }

    private static Text getStyledLabel(String base, float current, float defaultValue) {
        if (current == defaultValue) {
            return Text.literal(base);
        } else {
            return Text.literal(base).styled(style -> style.withItalic(true));
        }
    }

    private static Text getStyledLabel(String base, String current, String defaultValue) {
        if (current.equals(defaultValue)) {
            return Text.literal(base);
        } else {
            return Text.literal(base).styled(style -> style.withItalic(true));
        }
    }
}