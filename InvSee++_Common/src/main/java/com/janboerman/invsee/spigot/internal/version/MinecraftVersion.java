package com.janboerman.invsee.spigot.internal.version;

public enum MinecraftVersion {

    _1_8, _1_8_1, _1_8_2, _1_8_3, _1_8_4, _1_8_5, _1_8_6, _1_8_7, _1_8_8, _1_8_9,
    _1_9, _1_9_1, _1_9_2, _1_9_3, _1_9_4,
    _1_10, _1_10_1, _1_10_2,
    _1_11, _1_11_1, _1_11_2,
    _1_12, _1_12_1, _1_12_2,
    _1_13, _1_13_1, _1_13_2,
    _1_14, _1_14_1, _1_14_2, _1_14_3, _1_14_4,
    _1_15, _1_15_1, _1_15_2,
    _1_16, _1_16_1, _1_16_2, _1_16_3, _1_16_4, _1_16_5,
    _1_17, _1_17_1,
    _1_18, _1_18_1, _1_18_2,
    _1_19, _1_19_1, _1_19_2, _1_19_3, _1_19_4,
    _1_20, _1_20_1, _1_20_2;

    @Override
    public String toString() {
        return name().substring(1).replace('_', '.');
    }

    static MinecraftVersion fromString(String version) {
        try {
            return valueOf("_" + version.replace('.', '_'));
        } catch (Exception e) {
            return null;
        }
    }
}
