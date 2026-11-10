package com.workshopcraft.nodeores.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.workshopcraft.nodeores.NodeOres;
import com.workshopcraft.nodeores.logic.NodeSettings;
import com.workshopcraft.nodeores.node.NodeBaseBlock;
import com.workshopcraft.nodeores.node.NodeBaseBlockEntity;
import com.workshopcraft.nodeores.node.NodeClusterBlock;
import com.workshopcraft.nodeores.node.NodeClusterBlockEntity;
import com.workshopcraft.nodeores.node.NodeLogic;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public final class NodeCommands {
    private NodeCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nodeores")
                .requires(NodeCommands::allowed)
                .then(Commands.literal("place").executes(NodeCommands::place))
                .then(Commands.literal("grow").executes(NodeCommands::grow))
                .then(Commands.literal("inspect").executes(NodeCommands::inspect))
                .then(Commands.literal("reload").executes(NodeCommands::reload)));
    }

    private static boolean allowed(CommandSourceStack source) {
        //? if >=26.1 {
        return source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
        //?} else {
        /*return source.hasPermission(2);
        *///?}
    }

    private static int place(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = player(ctx);
        if (player == null) return 0;
        ServerLevel level = (ServerLevel) NodeOres.level(player);
        BlockPos origin = player.blockPosition();
        if (!level.getBlockState(origin).isAir()) origin = origin.above();
        boolean placed = NodeLogic.place(level, origin, level.getRandom(), Block.UPDATE_ALL, false);
        if (placed) {
            ctx.getSource().sendSuccess(() -> Component.literal("Placed an ore node. Stand in open air on solid ground, with a clear 2x2 and air above."), false);
        } else {
            ctx.getSource().sendFailure(Component.literal("That spot is blocked. Stand in one corner of a clear 2x2 with air above and solid ground below."));
        }
        return placed ? 1 : 0;
    }

    private static int grow(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = player(ctx);
        if (player == null) return 0;
        ServerLevel level = (ServerLevel) NodeOres.level(player);
        BlockPos center = player.blockPosition();
        Set<Long> seen = new HashSet<>();
        int grown = 0;
        BlockPos min = new BlockPos(center.getX() - 3, center.getY() - 2, center.getZ() - 3);
        BlockPos max = new BlockPos(center.getX() + 3, center.getY() + 3, center.getZ() + 3);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockEntity(pos) instanceof NodeBaseBlockEntity base && seen.add(base.anchorPos().asLong())) {
                NodeLogic.forceGrow(level, base);
                grown++;
            }
        }
        int count = grown;
        if (count == 0) ctx.getSource().sendFailure(Component.literal("No ore node within a few blocks."));
        else ctx.getSource().sendSuccess(() -> Component.literal("Advanced " + count + " ore node(s) to harvestable clusters."), false);
        return count;
    }

    private static int inspect(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = player(ctx);
        if (player == null) return 0;
        ServerLevel level = (ServerLevel) NodeOres.level(player);
        BlockPos center = player.blockPosition();
        BlockPos min = new BlockPos(center.getX() - 3, center.getY() - 2, center.getZ() - 3);
        BlockPos max = new BlockPos(center.getX() + 3, center.getY() + 3, center.getZ() + 3);
        int lines = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockEntity(pos) instanceof NodeClusterBlockEntity cluster) {
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();
                var state = level.getBlockState(pos);
                String stage = state.getBlock() instanceof NodeClusterBlock ? state.getValue(NodeClusterBlock.STAGE).getSerializedName() : "?";
                boolean ready = state.getBlock() instanceof NodeClusterBlock && state.getValue(NodeClusterBlock.READY);
                String ore = cluster.oreId();
                int ticks = cluster.ticks();
                ctx.getSource().sendSuccess(() -> Component.literal("Cluster " + x + " " + y + " " + z
                        + " ore=" + ore + " stage=" + stage + " ready=" + ready + " ticks=" + ticks), false);
                lines++;
            }
        }
        if (lines == 0) {
            for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
                if (level.getBlockState(pos).getBlock() instanceof NodeBaseBlock) {
                    int x = pos.getX();
                    int y = pos.getY();
                    int z = pos.getZ();
                    ctx.getSource().sendSuccess(() -> Component.literal("Stone base at " + x + " " + y + " " + z + " with no clusters nearby."), false);
                    return 1;
                }
            }
            ctx.getSource().sendFailure(Component.literal("No ore node within a few blocks."));
            return 0;
        }
        return lines;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        NodeSettings.reload();
        ctx.getSource().sendSuccess(() -> Component.literal("Reloaded config/nodeores.json"), true);
        return 1;
    }

    private static ServerPlayer player(CommandContext<CommandSourceStack> ctx) {
        try {
            return ctx.getSource().getPlayerOrException();
        } catch (Exception ex) {
            ctx.getSource().sendFailure(Component.literal("That command needs a player."));
            return null;
        }
    }
}
