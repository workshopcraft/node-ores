package com.workshopcraft.nodeores.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.workshopcraft.nodeores.NodeOres;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class NodeSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configDir = Path.of("config");
    private static volatile Data current = new Data().normalized();

    private NodeSettings() {}

    public static final class Data {
        public List<String> ores = new ArrayList<>(List.of("c:ores/iron", "c:ores/copper"));
        public int spawnIntervalSeconds = 600;
        public int growthSeconds = 600;
        public int clustersPerSpawnMin = 1;
        public int clustersPerSpawnMax = 2;
        public int maxClusters = 4;
        public int worldgenOneIn = 32;
        public List<String> baseBonus = new ArrayList<>(List.of("minecraft:cobblestone|4"));

        public Data normalized() {
            if (ores == null) ores = new ArrayList<>(List.of("c:ores/iron", "c:ores/copper"));
            else {
                List<String> cleaned = new ArrayList<>();
                for (String ore : ores) {
                    if (ore != null && !ore.isBlank()) cleaned.add(ore.trim());
                }
                ores = cleaned;
            }
            if (baseBonus == null) baseBonus = new ArrayList<>(List.of("minecraft:cobblestone|4"));
            else {
                List<String> cleaned = new ArrayList<>();
                for (String line : baseBonus) {
                    if (line != null && !line.isBlank()) cleaned.add(line.trim());
                }
                baseBonus = cleaned;
            }
            spawnIntervalSeconds = seconds(spawnIntervalSeconds, 600);
            growthSeconds = seconds(growthSeconds, 600);
            if (clustersPerSpawnMin <= 0) clustersPerSpawnMin = 1;
            if (clustersPerSpawnMax <= 0) clustersPerSpawnMax = 2;
            clustersPerSpawnMin = Math.min(4, clustersPerSpawnMin);
            clustersPerSpawnMax = Math.min(4, clustersPerSpawnMax);
            if (clustersPerSpawnMin > clustersPerSpawnMax) {
                int swap = clustersPerSpawnMin;
                clustersPerSpawnMin = clustersPerSpawnMax;
                clustersPerSpawnMax = swap;
            }
            if (maxClusters <= 0) maxClusters = 4;
            maxClusters = Math.min(4, Math.max(1, maxClusters));
            if (worldgenOneIn <= 0) worldgenOneIn = 32;
            worldgenOneIn = Math.min(100000, worldgenOneIn);
            return this;
        }
    }

    public record Bonus(String itemId, int count) {}

    public static Data get() {
        return current;
    }

    public static void init(Path dir) {
        configDir = dir == null ? Path.of("config") : dir;
        reload();
    }

    public static void reload() {
        try {
            Files.createDirectories(configDir);
            Path file = configDir.resolve("nodeores.json");
            if (Files.notExists(file)) {
                current = new Data().normalized();
                Files.writeString(file, GSON.toJson(current));
                return;
            }
            current = parse(Files.readString(file));
        } catch (IOException ex) {
            NodeOres.LOG.warn("Could not read config/nodeores.json, using defaults");
            current = new Data().normalized();
        }
    }

    public static Data parse(String json) {
        if (json == null || json.isBlank()) return new Data().normalized();
        try {
            Data parsed = GSON.fromJson(json, Data.class);
            if (parsed == null) return new Data().normalized();
            return parsed.normalized();
        } catch (RuntimeException ex) {
            return new Data().normalized();
        }
    }

    public static List<Bonus> bonuses(List<String> raw) {
        List<Bonus> out = new ArrayList<>();
        if (raw == null) return out;
        for (String line : raw) {
            if (line == null || line.isBlank()) continue;
            String[] parts = line.split("\\|", 2);
            String id = parts[0].trim();
            if (!id.contains(":")) continue;
            int count = 1;
            if (parts.length == 2) {
                try {
                    count = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ex) {
                    continue;
                }
            }
            if (count <= 0) continue;
            if (count > 64 * 16) count = 64 * 16;
            out.add(new Bonus(id, count));
        }
        return out;
    }

    private static int seconds(int value, int fallback) {
        if (value <= 0) return fallback;
        return Math.min(86400, Math.max(10, value));
    }
}
