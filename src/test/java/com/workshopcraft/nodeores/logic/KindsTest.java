package com.workshopcraft.nodeores.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KindsTest {
    @Test
    void mapsKnownOresAndLeavesTheRestRocky() {
        assertEquals("iron", Kinds.ofBlockId("minecraft:iron_ore"));
        assertEquals("iron", Kinds.ofBlockId("minecraft:deepslate_iron_ore"));
        assertEquals("copper", Kinds.ofBlockId("minecraft:copper_ore"));
        assertEquals("crystal", Kinds.ofBlockId("minecraft:diamond_ore"));
        assertEquals("crystal", Kinds.ofBlockId("minecraft:emerald_ore"));
        assertEquals("crystal", Kinds.ofBlockId("minecraft:amethyst_block"));
        assertEquals("crystal", Kinds.ofBlockId("minecraft:nether_quartz_ore"));
        assertEquals("rocky", Kinds.ofBlockId("minecraft:coal_ore"));
        assertEquals("rocky", Kinds.ofBlockId("minecraft:gold_ore"));
        assertEquals("rocky", Kinds.ofBlockId(null));
    }
}
