package com.workshopcraft.nodeores.logic;

import java.util.Locale;

public final class Kinds {
    private Kinds() {}

    public static String ofBlockId(String blockId) {
        if (blockId == null) return "rocky";
        String path = blockId.toLowerCase(Locale.ROOT);
        if (path.contains("copper")) return "copper";
        if (path.contains("iron")) return "iron";
        if (path.contains("diamond") || path.contains("emerald") || path.contains("amethyst") || path.contains("quartz")) {
            return "crystal";
        }
        return "rocky";
    }
}
