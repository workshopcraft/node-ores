package com.workshopcraft.nodeores.neoforge;

import com.workshopcraft.nodeores.NodeOres;
import com.workshopcraft.nodeores.command.NodeCommands;
import com.workshopcraft.nodeores.registry.NodeContent;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(NodeOres.MOD_ID)
public final class NodeOresNeoForge {
    public NodeOresNeoForge(IEventBus modBus) {
        NodeOres.init(net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get());
        modBus.addListener((RegisterEvent event) -> {
            event.register(Registries.BLOCK, helper -> NodeContent.registerBlocks(helper::register));
            event.register(Registries.BLOCK_ENTITY_TYPE, helper -> NodeContent.registerBlockEntities(helper::register));
            //? if <26.3 {
            event.register(Registries.FEATURE, helper -> NodeContent.registerFeatures(helper::register));
            //?} else {
            /*event.register(Registries.FEATURE_TYPE, helper -> NodeContent.registerFeatureTypes(helper::register));
            *///?}
        });
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> NodeCommands.register(event.getDispatcher()));
    }
}
