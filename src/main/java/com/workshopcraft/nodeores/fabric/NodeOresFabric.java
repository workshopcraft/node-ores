package com.workshopcraft.nodeores.fabric;

import com.workshopcraft.nodeores.NodeOres;
import com.workshopcraft.nodeores.command.NodeCommands;
import com.workshopcraft.nodeores.registry.NodeContent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;

public final class NodeOresFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NodeOres.init(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir());
        NodeContent.registerAllDirect();
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                ResourceKey.create(Registries.PLACED_FEATURE, NodeOres.id("ore_node"))
        );
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> NodeCommands.register(dispatcher));
    }
}
