package com.workshopcraft.nodeores.node;

import com.workshopcraft.nodeores.NodeOres;
import com.workshopcraft.nodeores.logic.NodeSettings;
import com.workshopcraft.nodeores.logic.OreTypes;
import com.workshopcraft.nodeores.registry.NodeContent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NodeLogic {
    private static final Set<Long> BUSY = new HashSet<>();
    private static boolean warned;

    private NodeLogic() {}

    public static boolean place(LevelAccessor level, BlockPos origin, RandomSource random, int flags, boolean keepInChunk) {
        return place(level, origin, random, flags, keepInChunk, null) != null;
    }

    public static String place(LevelAccessor level, BlockPos origin, RandomSource random, int flags, boolean keepInChunk, String requested) {
        String ore = selectOre(random, requested);
        if (ore == null) return null;
        int[][] signs = shuffledSigns(random);
        for (int[] sign : signs) {
            if (fits(level, origin, sign[0], sign[1], keepInChunk)) {
                write(level, origin, sign[0], sign[1], random, flags, ore);
                return ore;
            }
        }
        return null;
    }

    public static String selectOre(RandomSource random, String requested) {
        List<String> resolved = resolvable(NodeSettings.get().ores);
        if (requested == null || requested.isBlank()) {
            if (resolved.isEmpty()) return "";
            return resolved.get(random.nextInt(resolved.size()));
        }
        String matched = OreTypes.matchWord(resolved, requested);
        if (matched != null) return matched;
        String spec = OreTypes.explicitSpec(requested);
        if (spec == null) return null;
        return OreBook.resolve(spec);
    }

    public static void destroy(Level level, NodeBaseBlockEntity any, boolean bonus) {
        BlockPos anchor = any.anchorPos();
        if (!BUSY.add(anchor.asLong())) return;
        try {
            BlockPos[] spots = footprint(anchor, any.stepX(), any.stepZ());
            for (BlockPos spot : spots) {
                BlockPos up = spot.above();
                if (level.getBlockEntity(up) instanceof NodeClusterBlockEntity cluster) cluster.suppress();
                if (level.getBlockState(up).getBlock() instanceof NodeClusterBlock) {
                    level.setBlock(up, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            int removed = 0;
            for (BlockPos spot : spots) {
                if (level.getBlockState(spot).getBlock() instanceof NodeBaseBlock) {
                    level.setBlock(spot, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    removed++;
                }
            }
            if (bonus && removed > 0) dropBonus(level, any.getBlockPos());
        } finally {
            BUSY.remove(anchor.asLong());
        }
    }

    public static void pulse(NodeBaseBlockEntity be) {
        Level level = be.getLevel();
        if (level == null || level.isClientSide() || !be.isAnchor()) return;
        BlockPos[] spots = footprint(be.anchorPos(), be.stepX(), be.stepZ());
        for (BlockPos spot : spots) {
            if (!level.hasChunkAt(spot)) return;
        }
        for (BlockPos spot : spots) {
            if (!(level.getBlockEntity(spot) instanceof NodeBaseBlockEntity part) || !part.anchorPos().equals(be.anchorPos())) {
                destroy(level, be, false);
                return;
            }
        }
        int wait = be.spawnWait() - 20;
        if (wait > 0) {
            be.setSpawnWait(wait);
            return;
        }
        spawnInto(level, spots, be.anchorPos(), level.getRandom());
        be.setSpawnWait(NodeSettings.get().spawnIntervalSeconds * 20);
    }

    public static void forceGrow(Level level, NodeBaseBlockEntity anchor) {
        if (!anchor.isAnchor()) {
            BlockEntity found = level.getBlockEntity(anchor.anchorPos());
            if (found instanceof NodeBaseBlockEntity real && real.isAnchor()) anchor = real;
            else return;
        }
        BlockPos[] spots = footprint(anchor.anchorPos(), anchor.stepX(), anchor.stepZ());
        int occupied = 0;
        List<BlockPos> empty = new ArrayList<>();
        for (BlockPos spot : spots) {
            BlockState above = level.getBlockState(spot.above());
            if (above.getBlock() instanceof NodeClusterBlock) occupied++;
            else if (above.isAir()) empty.add(spot.above());
        }
        int room = Math.min(empty.size(), Math.max(0, NodeSettings.get().maxClusters - occupied));
        String ore = lockedOre(level, anchor.anchorPos(), spots, level.getRandom());
        for (int i = 0; i < room && !ore.isEmpty(); i++) {
            placeCluster(level, empty.get(i), ore, anchor.anchorPos());
        }
        int total = NodeSettings.get().growthSeconds * 20;
        for (BlockPos spot : spots) {
            BlockPos up = spot.above();
            if (level.getBlockEntity(up) instanceof NodeClusterBlockEntity cluster) {
                cluster.setTicks(total);
                BlockState state = level.getBlockState(up);
                if (state.getBlock() instanceof NodeClusterBlock) {
                    level.setBlock(up, state.setValue(NodeClusterBlock.STAGE, NodeStage.MATURE).setValue(NodeClusterBlock.READY, true), Block.UPDATE_ALL);
                }
                cluster.sync();
            }
        }
        anchor.setSpawnWait(NodeSettings.get().spawnIntervalSeconds * 20);
    }

    public static BlockPos[] footprint(BlockPos anchor, int ox, int oz) {
        int sx = ox == 0 ? 1 : Integer.signum(ox);
        int sz = oz == 0 ? 1 : Integer.signum(oz);
        return new BlockPos[] {
                anchor,
                new BlockPos(anchor.getX() + sx, anchor.getY(), anchor.getZ()),
                new BlockPos(anchor.getX(), anchor.getY(), anchor.getZ() + sz),
                new BlockPos(anchor.getX() + sx, anchor.getY(), anchor.getZ() + sz)
        };
    }

    private static void spawnInto(Level level, BlockPos[] spots, BlockPos anchor, RandomSource random) {
        NodeSettings.Data cfg = NodeSettings.get();
        List<BlockPos> empty = new ArrayList<>();
        int occupied = 0;
        for (BlockPos spot : spots) {
            BlockState above = level.getBlockState(spot.above());
            if (above.getBlock() instanceof NodeClusterBlock) occupied++;
            else if (above.isAir()) empty.add(spot.above());
        }
        int room = Math.min(empty.size(), Math.max(0, cfg.maxClusters - occupied));
        if (room <= 0) return;
        int min = Math.min(cfg.clustersPerSpawnMin, cfg.clustersPerSpawnMax);
        int max = Math.max(cfg.clustersPerSpawnMin, cfg.clustersPerSpawnMax);
        int want = max > min ? min + random.nextInt(max - min + 1) : min;
        int count = Math.min(room, want);
        if (count <= 0) return;
        String ore = lockedOre(level, anchor, spots, random);
        if (ore.isEmpty()) {
            if (!warned) {
                warned = true;
                NodeOres.LOG.warn("None of the configured ores resolved to a block. Edit config/nodeores.json");
            }
            return;
        }
        for (int i = 0; i < count; i++) {
            BlockPos up = empty.remove(random.nextInt(empty.size()));
            placeCluster(level, up, ore, anchor);
        }
    }

    private static String lockedOre(Level level, BlockPos anchor, BlockPos[] spots, RandomSource random) {
        String ore = readOre(level, anchor);
        if (live(ore)) return ore;
        for (BlockPos spot : spots) {
            if (level.getBlockEntity(spot.above()) instanceof NodeClusterBlockEntity cluster && live(cluster.oreId())) {
                remember(level, spots, cluster.oreId());
                return cluster.oreId();
            }
        }
        String picked = selectOre(random, null);
        if (!live(picked)) return "";
        remember(level, spots, picked);
        return picked;
    }

    private static boolean live(String ore) {
        return ore != null && !ore.isEmpty() && NodeOres.block(NodeOres.parse(ore)) != null;
    }

    private static String readOre(Level level, BlockPos anchor) {
        if (level.getBlockEntity(anchor) instanceof NodeBaseBlockEntity be) return be.oreId();
        return "";
    }

    private static void remember(Level level, BlockPos[] spots, String ore) {
        for (BlockPos spot : spots) {
            if (level.getBlockEntity(spot) instanceof NodeBaseBlockEntity be) be.setOreId(ore);
        }
    }

    private static void placeCluster(Level level, BlockPos pos, String oreId, BlockPos anchor) {
        NodeKind kind = NodeKind.fromBlockId(oreId);
        BlockState state = NodeContent.CLUSTER.defaultBlockState()
                .setValue(NodeClusterBlock.KIND, kind)
                .setValue(NodeClusterBlock.STAGE, NodeStage.NUB)
                .setValue(NodeClusterBlock.READY, false);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof NodeClusterBlockEntity cluster) cluster.init(oreId, anchor);
    }

    private static List<String> resolvable(List<String> ores) {
        List<String> out = new ArrayList<>();
        if (ores == null) return out;
        for (String spec : ores) {
            String id = OreBook.resolve(spec);
            if (id != null && !out.contains(id)) out.add(id);
        }
        return out;
    }

    private static void write(LevelAccessor level, BlockPos origin, int ox, int oz, RandomSource random, int flags, String ore) {
        BlockPos[] spots = footprint(origin, ox, oz);
        int minX = spots[0].getX();
        int minZ = spots[0].getZ();
        for (BlockPos spot : spots) {
            minX = Math.min(minX, spot.getX());
            minZ = Math.min(minZ, spot.getZ());
        }
        int interval = NodeSettings.get().spawnIntervalSeconds * 20;
        int wait = 20 + random.nextInt(interval);
        for (BlockPos spot : spots) {
            NodeCorner corner = NodeCorner.of(spot.getX(), spot.getZ(), minX, minZ);
            BlockState state = NodeContent.BASE.defaultBlockState().setValue(NodeBaseBlock.CORNER, corner);
            level.setBlock(spot, state, flags);
            if (level.getBlockEntity(spot) instanceof NodeBaseBlockEntity be) {
                be.configure(origin, ox, oz, spot.equals(origin), wait, ore);
            }
        }
    }

    private static boolean fits(LevelAccessor level, BlockPos origin, int ox, int oz, boolean keepInChunk) {
        BlockPos[] spots = footprint(origin, ox, oz);
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        for (BlockPos spot : spots) {
            if (keepInChunk && ((spot.getX() >> 4) != chunkX || (spot.getZ() >> 4) != chunkZ)) return false;
            //? if >=26.1 {
            int minY = level.getMinY();
            //?} else {
            /*int minY = level.getMinBuildHeight();
            *///?}
            if (spot.getY() <= minY || spot.getY() + 1 >= minY + level.getHeight()) return false;
            if (!columnOk(level, spot)) return false;
        }
        return true;
    }

    private static boolean columnOk(LevelAccessor level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) return false;
        BlockPos below = pos.below();
        BlockState ground = level.getBlockState(below);
        if (ground.isAir() || !ground.getFluidState().isEmpty()) return false;
        if (ground.getBlock() instanceof NodeBaseBlock || ground.getBlock() instanceof NodeClusterBlock) return false;
        VoxelShape shape = ground.getCollisionShape(level, below);
        return !shape.isEmpty() && shape.max(net.minecraft.core.Direction.Axis.Y) >= 0.75;
    }

    private static int[][] shuffledSigns(RandomSource random) {
        int[][] signs = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int i = signs.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] swap = signs[i];
            signs[i] = signs[j];
            signs[j] = swap;
        }
        return signs;
    }

    private static void dropBonus(Level level, BlockPos pos) {
        for (NodeSettings.Bonus bonus : NodeSettings.bonuses(NodeSettings.get().baseBonus)) {
            Item item = NodeOres.item(NodeOres.parse(bonus.itemId()));
            if (item == null) continue;
            int left = bonus.count();
            while (left > 0) {
                int count = Math.min(64, left);
                Block.popResource(level, pos, new ItemStack(item, count));
                left -= count;
            }
        }
    }
}
