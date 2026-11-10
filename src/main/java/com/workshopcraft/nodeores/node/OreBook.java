package com.workshopcraft.nodeores.node;

import com.workshopcraft.nodeores.NodeOres;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class OreBook {
    private OreBook() {}

    public static String resolve(String spec) {
        if (spec == null) return null;
        String trimmed = spec.trim();
        if (trimmed.isEmpty()) return null;
        try {
            if (trimmed.startsWith("#")) return fromTag(trimmed.substring(1));
            Identifier id = NodeOres.parse(trimmed);
            if (id != null && NodeOres.block(id) != null) return id.toString();
            String tagged = fromTag(trimmed);
            if (tagged != null) return tagged;
            String fallback = fallback(trimmed);
            if (fallback == null) return null;
            Identifier fallbackId = NodeOres.parse(fallback);
            if (fallbackId != null && NodeOres.block(fallbackId) != null) return fallback;
            return null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static String fromTag(String tagId) {
        Identifier id = NodeOres.parse(tagId);
        if (id == null) return null;
        TagKey<Block> key = TagKey.create(Registries.BLOCK, id);
        String buried = null;
        //? if >=26.1 {
        Iterable<Holder<Block>> named = BuiltInRegistries.BLOCK.getTagOrEmpty(key);
        //?} else {
        /*java.util.Optional<net.minecraft.core.HolderSet.Named<Block>> found = BuiltInRegistries.BLOCK.getTag(key);
        if (found.isEmpty()) return null;
        Iterable<Holder<Block>> named = found.get();
        *///?}
        for (Holder<Block> holder : named) {
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(holder.value());
            if (blockId == null) continue;
            String path = blockId.getPath();
            if (path.contains("deepslate") || path.contains("nether") || path.contains("_end_")) {
                if (buried == null) buried = blockId.toString();
                continue;
            }
            return blockId.toString();
        }
        return buried;
    }

    private static String fallback(String spec) {
        int colon = spec.indexOf(':');
        if (colon <= 0) return null;
        String path = spec.substring(colon + 1);
        if (!path.startsWith("ores/")) return null;
        String material = path.substring("ores/".length());
        if (material.isEmpty() || material.indexOf('/') >= 0 || material.indexOf(':') >= 0) return null;
        if (material.equals("quartz")) return "minecraft:nether_quartz_ore";
        return "minecraft:" + material + "_ore";
    }
}
