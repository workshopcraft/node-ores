package com.workshopcraft.nodeores.registry;

import com.workshopcraft.nodeores.NodeOres;
import com.workshopcraft.nodeores.node.NodeBaseBlock;
import com.workshopcraft.nodeores.node.NodeBaseBlockEntity;
import com.workshopcraft.nodeores.node.NodeClusterBlock;
import com.workshopcraft.nodeores.node.NodeClusterBlockEntity;
import com.workshopcraft.nodeores.world.OreNodeFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.BiConsumer;

public final class NodeContent {
    public static Block BASE;
    public static Block CLUSTER;
    public static BlockEntityType<NodeBaseBlockEntity> BASE_ENTITY;
    public static BlockEntityType<NodeClusterBlockEntity> CLUSTER_ENTITY;
    //? if <26.3 {
    public static Feature<?> ORE_NODE;
    //?}

    private NodeContent() {}

    public static void registerBlocks(BiConsumer<Identifier, Block> register) {
        BlockBehaviour.Properties base = BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .noOcclusion();
        BlockBehaviour.Properties cluster = BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0F, 3.0F)
                .sound(SoundType.STONE)
                .noOcclusion();
        //? if >=26.3 {
        /*base = base.pushReaction(PushReaction.IMMOVEABLE);
        cluster = cluster.pushReaction(PushReaction.IMMOVEABLE);
        *///?} else {
        base = base.pushReaction(PushReaction.BLOCK);
        cluster = cluster.pushReaction(PushReaction.BLOCK);
        //?}
        //? if >=1.21.2 {
        base = base.setId(ResourceKey.create(Registries.BLOCK, NodeOres.id("node_base")));
        cluster = cluster.setId(ResourceKey.create(Registries.BLOCK, NodeOres.id("node_cluster")));
        //?}
        BASE = new NodeBaseBlock(base);
        CLUSTER = new NodeClusterBlock(cluster);
        register.accept(NodeOres.id("node_base"), BASE);
        register.accept(NodeOres.id("node_cluster"), CLUSTER);
    }

    public static void registerBlockEntities(BiConsumer<Identifier, BlockEntityType<?>> register) {
        //? if >=1.21.2 {
        BASE_ENTITY = new BlockEntityType<>(NodeBaseBlockEntity::new, java.util.Set.of(BASE));
        CLUSTER_ENTITY = new BlockEntityType<>(NodeClusterBlockEntity::new, java.util.Set.of(CLUSTER));
        //?} else {
        /*BASE_ENTITY = BlockEntityType.Builder.of(NodeBaseBlockEntity::new, BASE).build(null);
        CLUSTER_ENTITY = BlockEntityType.Builder.of(NodeClusterBlockEntity::new, CLUSTER).build(null);
        *///?}
        register.accept(NodeOres.id("node_base"), BASE_ENTITY);
        register.accept(NodeOres.id("node_cluster"), CLUSTER_ENTITY);
    }

    //? if <26.3 {
    public static void registerFeatures(BiConsumer<Identifier, Feature<?>> register) {
        ORE_NODE = new OreNodeFeature();
        register.accept(NodeOres.id("ore_node"), ORE_NODE);
    }
    //?}

    //? if >=26.3 {
    /*public static void registerFeatureTypes(BiConsumer<Identifier, com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.levelgen.feature.Feature>> register) {
        register.accept(NodeOres.id("ore_node"), OreNodeFeature.CODEC);
    }
    *///?}

    public static <T> BiConsumer<Identifier, T> direct(Registry<? super T> registry) {
        return (id, value) -> Registry.register(registry, id, value);
    }

    public static void registerAllDirect() {
        registerBlocks(direct(BuiltInRegistries.BLOCK));
        registerBlockEntities(direct(BuiltInRegistries.BLOCK_ENTITY_TYPE));
        //? if <26.3 {
        registerFeatures(direct(BuiltInRegistries.FEATURE));
        //?} else {
        /*registerFeatureTypes(direct(BuiltInRegistries.FEATURE_TYPE));
        *///?}
    }
}
