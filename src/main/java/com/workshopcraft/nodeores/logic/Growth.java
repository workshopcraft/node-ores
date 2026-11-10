package com.workshopcraft.nodeores.logic;

import com.workshopcraft.nodeores.node.NodeStage;

public final class Growth {
    private Growth() {}

    public static NodeStage stage(int ticks, int total) {
        if (total <= 0) return NodeStage.MATURE;
        long t = Math.max(0, ticks);
        if (t * 3 < total) return NodeStage.NUB;
        if (t * 3 < total * 2L) return NodeStage.PARTIAL;
        return NodeStage.MATURE;
    }

    public static boolean harvestable(int ticks, int total) {
        return total > 0 && ticks >= total;
    }
}
