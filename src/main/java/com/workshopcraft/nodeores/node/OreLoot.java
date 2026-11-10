package com.workshopcraft.nodeores.node;

import com.workshopcraft.nodeores.NodeOres;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class OreLoot {
    private OreLoot() {}

    public static void drop(Level level, BlockPos pos, Player player, ItemStack tool, String blockId) {
        if (!(level instanceof ServerLevel server)) return;
        Block block = NodeOres.block(NodeOres.parse(blockId));
        if (block == null) return;
        BlockState ore = block.defaultBlockState();
        ItemStack used = tool == null ? ItemStack.EMPTY : tool;
        if (player != null && ore.requiresCorrectToolForDrops() && !player.hasCorrectToolForDrops(ore)) return;
        Block.dropResources(ore, server, pos, null, player, used);
    }

    public static float progress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (!state.getValue(NodeClusterBlock.READY)) return 0.0F;
        String id = oreId(level, pos, state);
        Block block = NodeOres.block(NodeOres.parse(id));
        if (block == null) return 0.01F;
        return block.defaultBlockState().getDestroyProgress(player, level, pos);
    }

    private static String oreId(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof NodeClusterBlockEntity be && !be.oreId().isEmpty()) return be.oreId();
        return switch (state.getValue(NodeClusterBlock.KIND)) {
            case IRON -> "minecraft:iron_ore";
            case COPPER -> "minecraft:copper_ore";
            case CRYSTAL -> "minecraft:diamond_ore";
            case ROCKY -> "minecraft:coal_ore";
        };
    }
}
