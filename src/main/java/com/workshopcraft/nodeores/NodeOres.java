package com.workshopcraft.nodeores;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class NodeOres {
    public static final String MOD_ID = "nodeores";
    public static final Logger LOG = LoggerFactory.getLogger("Node Ores");

    private NodeOres() {}

    public static void init(Path configDir) {
        com.workshopcraft.nodeores.logic.NodeSettings.init(configDir);
    }

    public static Identifier id(String path) {
        //? if >=1.21 {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
        //?} else {
        /*return new Identifier(MOD_ID, path);
        *///?}
    }

    public static Identifier mc(String path) {
        //? if >=1.21 {
        return Identifier.fromNamespaceAndPath("minecraft", path);
        //?} else {
        /*return new Identifier("minecraft", path);
        *///?}
    }

    public static Identifier parse(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            //? if >=1.21 {
            return Identifier.parse(raw);
            //?} else {
            /*return new Identifier(raw);
            *///?}
        } catch (RuntimeException ex) {
            return null;
        }
    }

    public static Block block(Identifier id) {
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) return null;
        //? if >=1.21.2 {
        return BuiltInRegistries.BLOCK.getValue(id);
        //?} else {
        /*return BuiltInRegistries.BLOCK.get(id);
        *///?}
    }

    public static Item item(Identifier id) {
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return null;
        //? if >=1.21.2 {
        return BuiltInRegistries.ITEM.getValue(id);
        //?} else {
        /*return BuiltInRegistries.ITEM.get(id);
        *///?}
    }

    public static Level level(Entity entity) {
        return entity.level();
    }
}
