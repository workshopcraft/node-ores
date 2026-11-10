package com.workshopcraft.nodeores.node;

import net.minecraft.util.StringRepresentable;

public enum NodeCorner implements StringRepresentable {
    NORTH_WEST("north_west"),
    NORTH_EAST("north_east"),
    SOUTH_WEST("south_west"),
    SOUTH_EAST("south_east");

    private final String name;

    NodeCorner(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static NodeCorner of(int x, int z, int minX, int minZ) {
        boolean north = z == minZ;
        boolean west = x == minX;
        if (north && west) return NORTH_WEST;
        if (north) return NORTH_EAST;
        if (west) return SOUTH_WEST;
        return SOUTH_EAST;
    }
}
