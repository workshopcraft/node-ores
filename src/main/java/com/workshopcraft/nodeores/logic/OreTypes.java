package com.workshopcraft.nodeores.logic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class OreTypes {
    private OreTypes() {}

    public static String explicitSpec(String token) {
        if (token == null) return null;
        String trimmed = token.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.startsWith("#") || trimmed.contains(":")) return trimmed;
        return "c:ores/" + trimmed.toLowerCase(Locale.ROOT);
    }

    public static String matchWord(List<String> resolvedIds, String token) {
        if (token == null || resolvedIds == null) return null;
        String word = token.trim().toLowerCase(Locale.ROOT);
        if (word.isEmpty() || word.startsWith("#") || word.contains(":") || word.indexOf('/') >= 0) return null;
        for (String id : resolvedIds) {
            if (id != null && names(id, word)) return id;
        }
        return null;
    }

    public static List<String> suggestions(List<String> ores) {
        Set<String> out = new LinkedHashSet<>();
        if (ores == null) return List.of();
        for (String ore : ores) {
            if (ore == null || ore.isBlank()) continue;
            String trimmed = ore.trim();
            out.add(trimmed);
            String word = materialWord(trimmed);
            if (word != null) out.add(word);
        }
        return new ArrayList<>(out);
    }

    static String materialWord(String spec) {
        if (spec == null) return null;
        String body = spec.startsWith("#") ? spec.substring(1) : spec;
        int colon = body.lastIndexOf(':');
        String path = colon >= 0 ? body.substring(colon + 1) : body;
        int slash = path.lastIndexOf('/');
        if (slash >= 0) path = path.substring(slash + 1);
        if (path.endsWith("_ores")) path = path.substring(0, path.length() - 5);
        else if (path.endsWith("_ore")) path = path.substring(0, path.length() - 4);
        if (path.startsWith("nether_")) path = path.substring("nether_".length());
        if (path.startsWith("deepslate_")) path = path.substring("deepslate_".length());
        if (path.isEmpty() || path.indexOf('/') >= 0 || path.indexOf(':') >= 0) return null;
        return path.toLowerCase(Locale.ROOT);
    }

    private static boolean names(String blockId, String word) {
        int colon = blockId.indexOf(':');
        String path = (colon >= 0 ? blockId.substring(colon + 1) : blockId).toLowerCase(Locale.ROOT);
        return path.equals(word) || path.equals(word + "_ore") || path.endsWith("_" + word + "_ore");
    }
}
