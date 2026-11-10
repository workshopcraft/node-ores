package com.workshopcraft.nodeores.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeSettingsTest {
    @Test
    void emptyObjectUsesDefaults() {
        NodeSettings.Data data = NodeSettings.parse("{}");
        assertEquals(List.of("c:ores/iron", "c:ores/copper"), data.ores);
        assertEquals(600, data.spawnIntervalSeconds);
        assertEquals(600, data.growthSeconds);
        assertEquals(1, data.clustersPerSpawnMin);
        assertEquals(2, data.clustersPerSpawnMax);
        assertEquals(4, data.maxClusters);
        assertEquals(32, data.worldgenOneIn);
        assertEquals(List.of("minecraft:cobblestone|4"), data.baseBonus);
    }

    @Test
    void secondsClampAndBlankOresStayEmpty() {
        NodeSettings.Data data = NodeSettings.parse("{\"growthSeconds\":5,\"spawnIntervalSeconds\":100000,\"ores\":[\"  \",\"minecraft:coal_ore\"]}");
        assertEquals(10, data.growthSeconds);
        assertEquals(86400, data.spawnIntervalSeconds);
        assertEquals(List.of("minecraft:coal_ore"), data.ores);
    }

    @Test
    void invalidJsonFallsBack() {
        assertEquals(600, NodeSettings.parse("{").growthSeconds);
    }

    @Test
    void bonusesParseCountAndSkipBrokenLines() {
        List<NodeSettings.Bonus> bonuses = NodeSettings.bonuses(List.of("minecraft:stone|2", "bad", "minecraft:dirt", "minecraft:sand|0", "minecraft:gravel|10000"));
        assertEquals(3, bonuses.size());
        assertEquals("minecraft:stone", bonuses.get(0).itemId());
        assertEquals(2, bonuses.get(0).count());
        assertEquals(1, bonuses.get(1).count());
        assertEquals(64 * 16, bonuses.get(2).count());
        assertTrue(NodeSettings.bonuses(List.of()).isEmpty());
    }
}
