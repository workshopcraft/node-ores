package com.workshopcraft.nodeores.node;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class NodeShapes {
    private static final VoxelShape[] BASE = new VoxelShape[] {
            boxes(new int[][] {{0, 0, 0, 16, 4, 16}, {1, 4, 1, 14, 8, 14}, {3, 8, 3, 12, 16, 12}, {0, 4, 0, 6, 7, 6}}),
            boxes(new int[][] {{0, 0, 0, 16, 4, 16}, {2, 4, 1, 15, 8, 13}, {4, 8, 3, 13, 16, 12}, {10, 4, 0, 16, 8, 6}}),
            boxes(new int[][] {{0, 0, 0, 16, 5, 16}, {1, 5, 2, 13, 9, 15}, {3, 9, 4, 11, 16, 13}, {0, 4, 10, 7, 8, 16}}),
            boxes(new int[][] {{0, 0, 0, 16, 4, 16}, {2, 4, 2, 15, 9, 15}, {5, 9, 5, 14, 16, 14}, {9, 5, 9, 16, 10, 16}})
    };

    private static final VoxelShape[][] CLUSTER = new VoxelShape[][] {
            {
                    boxes(new int[][] {{5, 0, 6, 10, 4, 11}, {7, 3, 4, 12, 7, 9}}),
                    boxes(new int[][] {{3, 0, 4, 11, 5, 12}, {8, 4, 6, 14, 8, 13}, {5, 6, 7, 9, 10, 11}}),
                    boxes(new int[][] {{2, 0, 3, 12, 5, 13}, {6, 4, 2, 15, 8, 10}, {3, 5, 7, 9, 11, 14}, {9, 7, 8, 14, 12, 13}})
            },
            {
                    boxes(new int[][] {{4, 0, 4, 12, 3, 12}, {6, 2, 7, 11, 5, 14}}),
                    boxes(new int[][] {{2, 0, 3, 14, 3, 13}, {4, 3, 5, 12, 6, 14}, {7, 5, 2, 15, 8, 9}}),
                    boxes(new int[][] {{1, 0, 2, 15, 3, 14}, {3, 3, 4, 13, 6, 15}, {6, 6, 3, 14, 9, 11}, {2, 5, 8, 8, 8, 14}})
            },
            {
                    boxes(new int[][] {{6, 0, 5, 11, 5, 10}, {4, 2, 8, 8, 6, 13}}),
                    boxes(new int[][] {{3, 0, 3, 10, 6, 11}, {8, 3, 6, 14, 8, 14}, {5, 6, 4, 9, 10, 8}}),
                    boxes(new int[][] {{2, 0, 2, 11, 4, 12}, {7, 3, 5, 15, 7, 14}, {4, 5, 8, 10, 10, 15}, {9, 6, 3, 14, 11, 9}})
            },
            {
                    boxes(new int[][] {{7, 0, 7, 10, 7, 10}}),
                    boxes(new int[][] {{6, 0, 5, 9, 9, 8}, {10, 0, 9, 13, 6, 12}}),
                    boxes(new int[][] {{5, 0, 4, 8, 12, 7}, {9, 0, 6, 12, 8, 9}, {7, 0, 10, 10, 14, 13}, {11, 0, 11, 14, 5, 14}})
            }
    };

    private NodeShapes() {}

    public static VoxelShape base(NodeCorner corner) {
        return BASE[corner.ordinal()];
    }

    public static VoxelShape cluster(NodeKind kind, NodeStage stage) {
        return CLUSTER[kind.ordinal()][stage.ordinal()];
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
