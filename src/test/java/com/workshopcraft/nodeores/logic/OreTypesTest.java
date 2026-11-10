package com.workshopcraft.nodeores.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OreTypesTest {
    @Test
    void explicitSpecNormalizesABareWordAndLeavesIdsAlone() {
        assertEquals("c:ores/iron", OreTypes.explicitSpec(" iron "));
        assertEquals("c:ores/copper", OreTypes.explicitSpec("Copper"));
        assertEquals("minecraft:iron_ore", OreTypes.explicitSpec("minecraft:iron_ore"));
        assertEquals("c:ores/diamond", OreTypes.explicitSpec("c:ores/diamond"));
        assertEquals("#minecraft:iron_ores", OreTypes.explicitSpec("#minecraft:iron_ores"));
        assertNull(OreTypes.explicitSpec("  "));
        assertNull(OreTypes.explicitSpec(null));
    }

    @Test
    void matchWordUsesTheConfiguredBlock() {
        List<String> resolved = List.of("minecraft:iron_ore", "minecraft:copper_ore", "minecraft:nether_quartz_ore");
        assertEquals("minecraft:iron_ore", OreTypes.matchWord(resolved, "iron"));
        assertEquals("minecraft:copper_ore", OreTypes.matchWord(resolved, "COPPER"));
        assertEquals("minecraft:nether_quartz_ore", OreTypes.matchWord(resolved, "quartz"));
        assertNull(OreTypes.matchWord(resolved, "diamond"));
        assertNull(OreTypes.matchWord(resolved, "on"));
        assertNull(OreTypes.matchWord(resolved, "minecraft:iron_ore"));
    }

    @Test
    void suggestionsOfferTheSpecAndAShortName() {
        List<String> suggestions = OreTypes.suggestions(List.of("c:ores/iron", "c:ores/copper", "minecraft:nether_quartz_ore"));
        assertEquals(List.of("c:ores/iron", "iron", "c:ores/copper", "copper", "minecraft:nether_quartz_ore", "quartz"), suggestions);
        assertTrue(OreTypes.suggestions(List.of()).isEmpty());
        assertEquals("quartz", OreTypes.materialWord("#minecraft:nether_quartz_ores"));
        assertEquals("iron", OreTypes.materialWord("minecraft:deepslate_iron_ore"));
    }
}
