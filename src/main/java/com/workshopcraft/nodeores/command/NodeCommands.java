package com.workshopcraft.nodeores.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.workshopcraft.nodeores.NodeOres;
import com.workshopcraft.nodeores.logic.NodeSettings;
import com.workshopcraft.nodeores.logic.OreTypes;
import com.workshopcraft.nodeores.node.NodeBaseBlockEntity;
import com.workshopcraft.nodeores.node.NodeClusterBlock;
import com.workshopcraft.nodeores.node.NodeClusterBlockEntity;
import com.workshopcraft.nodeores.node.NodeLogic;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NodeCommands {
    private NodeCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nodeores")
                .requires(NodeCommands::allowed)
                .then(Commands.literal("place")
                        .executes(ctx -> place(ctx, null))
                        .then(Commands.argument("ore", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(OreTypes.suggestions(NodeSettings.get().ores), builder))
                                .executes(ctx -> place(ctx, StringArgumentType.getString(ctx, "ore")))))
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

    private static int place(CommandContext<CommandSourceStack> ctx, String requested) {
        ServerPlayer player = player(ctx);
        if (player == null) return 0;
        ServerLevel level = (ServerLevel) NodeOres.level(player);
        if (requested != null && !requested.isBlank() && NodeLogic.selectOre(level.getRandom(), requested) == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown ore type \"" + requested.trim() + "\". Use a name like iron, a block id, or a block tag."));
            return 0;
        }
        BlockPos origin = player.blockPosition();
        if (!level.getBlockState(origin).isAir()) origin = origin.above();
        String ore = NodeLogic.place(level, origin, level.getRandom(), Block.UPDATE_ALL, false, requested);
        if (ore == null) {
            ctx.getSource().sendFailure(Component.literal("That spot is blocked. Stand in one corner of a clear 2x2 with air above and solid ground below."));
            return 0;
        }
        if (ore.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("Placed an ore node. config/nodeores.json has no ores, so it will not grow clusters."), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("Placed an ore node of " + ore + "."), false);
        }
        return 1;
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
        Map<Long, BlockPos> anchors = new LinkedHashMap<>();
        Map<Long, String> ores = new LinkedHashMap<>();
        Set<Long> withClusters = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockEntity(pos) instanceof NodeBaseBlockEntity base) {
                BlockPos anchor = base.anchorPos();
                long key = anchor.asLong();
                if (anchors.containsKey(key)) continue;
                String ore = base.oreId();
                if (level.getBlockEntity(anchor) instanceof NodeBaseBlockEntity anchorBe && !anchorBe.oreId().isEmpty()) {
                    ore = anchorBe.oreId();
                }
                anchors.put(key, anchor);
                ores.put(key, ore == null || ore.isEmpty() ? "none" : ore);
            }
        }
        List<String> clusters = new ArrayList<>();
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
                BlockPos anchor = cluster.anchorPos();
                long key = anchor.asLong();
                withClusters.add(key);
                anchors.putIfAbsent(key, anchor);
                ores.putIfAbsent(key, ore == null || ore.isEmpty() ? "none" : ore);
                clusters.add("Cluster " + x + " " + y + " " + z + " ore=" + ore + " stage=" + stage + " ready=" + ready + " ticks=" + ticks);
            }
        }
        if (anchors.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("No ore node within a few blocks."));
            return 0;
        }
        int lines = 0;
        for (Map.Entry<Long, BlockPos> entry : anchors.entrySet()) {
            BlockPos anchor = entry.getValue();
            int x = anchor.getX();
            int y = anchor.getY();
            int z = anchor.getZ();
            String ore = ores.get(entry.getKey());
            boolean bare = !withClusters.contains(entry.getKey());
            String text = "Node " + x + " " + y + " " + z + " ore=" + ore + (bare ? " with no clusters nearby." : "");
            ctx.getSource().sendSuccess(() -> Component.literal(text), false);
            lines++;
        }
        for (String text : clusters) {
            ctx.getSource().sendSuccess(() -> Component.literal(text), false);
            lines++;
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
