package com.astralis.lowdurabilitywarning;

public class LowDurabilityConfig {
    public boolean enableWarnings = true;
    public boolean includeArmor = true;
    public boolean includeElytra = true;
    public boolean includeTools = true;
    public boolean onlyEnchanted = false;

    public int firstWarning = 20;
    public int secondWarning = 10;
    public int thirdWarning = 5;

    public float volume = 0.3f;
    public float pitch = 1.0f;
    public int soundDelayMillis = 150;
    public int soundRepeats = 4;

    public String alertText = "has low durability";
}