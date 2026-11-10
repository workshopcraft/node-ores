package com.workshopcraft.nodeores.logic;

import com.workshopcraft.nodeores.node.NodeStage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrowthTest {
    @Test
    void stagesSplitTheGrowthWindowIntoThirds() {
        int total = 300;
        assertEquals(NodeStage.NUB, Growth.stage(0, total));
        assertEquals(NodeStage.NUB, Growth.stage(99, total));
        assertEquals(NodeStage.PARTIAL, Growth.stage(100, total));
        assertEquals(NodeStage.PARTIAL, Growth.stage(199, total));
        assertEquals(NodeStage.MATURE, Growth.stage(200, total));
        assertEquals(NodeStage.MATURE, Growth.stage(300, total));
    }

    @Test
    void harvestableOnlyWhenTheTimerFinishes() {
        assertFalse(Growth.harvestable(200, 300));
        assertTrue(Growth.harvestable(300, 300));
        assertFalse(Growth.harvestable(0, 0));
    }
}
