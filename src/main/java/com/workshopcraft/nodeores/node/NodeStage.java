package com.workshopcraft.nodeores.node;

import net.minecraft.util.StringRepresentable;

public enum NodeStage implements StringRepresentable {
    NUB("nub"),
    PARTIAL("partial"),
    MATURE("mature");

    private final String name;

    NodeStage(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static NodeStage byName(String name) {
        for (NodeStage stage : values()) {
            if (stage.name.equals(name)) return stage;
        }
        return NUB;
    }
}
