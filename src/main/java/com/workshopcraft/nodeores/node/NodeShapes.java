package com.workshopcraft.nodeores.node;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class NodeShapes {
    private static final VoxelShape[] BASE = new VoxelShape[] {
            boxes(new int[][] {{0, 0, 0, 16, 14, 16}, {1, 14, 1, 16, 16, 16}}),
            boxes(new int[][] {{0, 0, 0, 16, 14, 16}, {0, 14, 1, 15, 16, 16}}),
            boxes(new int[][] {{0, 0, 0, 16, 14, 16}, {1, 14, 0, 16, 16, 15}}),
            boxes(new int[][] {{0, 0, 0, 16, 14, 16}, {0, 14, 0, 15, 16, 15}})
    };

    private static final VoxelShape[] CLUSTER = new VoxelShape[] {
            Block.box(6, 0, 6, 10, 4, 10),
            Block.box(4, 0, 4, 12, 6, 12),
            Block.box(2, 0, 2, 14, 8, 14)
    };

    private NodeShapes() {}

    public static VoxelShape base(NodeCorner corner) {
        return BASE[corner.ordinal()];
    }

    public static VoxelShape cluster(NodeKind kind, NodeStage stage) {
        return CLUSTER[stage.ordinal()];
    }

    private static VoxelShape boxes(int[][] coords) {
        VoxelShape shape = Block.box(coords[0][0], coords[0][1], coords[0][2], coords[0][3], coords[0][4], coords[0][5]);
        for (int i = 1; i < coords.length; i++) {
            int[] c = coords[i];
            shape = Shapes.or(shape, Block.box(c[0], c[1], c[2], c[3], c[4], c[5]));
        }
        return shape;
    }
}
