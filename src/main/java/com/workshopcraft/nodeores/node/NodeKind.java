package com.workshopcraft.nodeores.node;

import com.workshopcraft.nodeores.logic.Kinds;
import net.minecraft.util.StringRepresentable;

public enum NodeKind implements StringRepresentable {
    IRON("iron"),
    COPPER("copper"),
    ROCKY("rocky"),
    CRYSTAL("crystal");

    private final String name;

    NodeKind(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static NodeKind fromBlockId(String blockId) {
        return byName(Kinds.ofBlockId(blockId));
    }

    public static NodeKind byName(String name) {
        for (NodeKind kind : values()) {
            if (kind.name.equals(name)) return kind;
        }
        return ROCKY;
    }
}
