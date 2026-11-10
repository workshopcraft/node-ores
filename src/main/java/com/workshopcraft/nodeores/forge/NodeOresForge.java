package com.workshopcraft.nodeores.forge;

import com.workshopcraft.nodeores.NodeOres;
import com.workshopcraft.nodeores.command.NodeCommands;
import com.workshopcraft.nodeores.registry.NodeContent;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

@Mod(NodeOres.MOD_ID)
public final class NodeOresForge {
    public NodeOresForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        NodeOres.init(net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
        modBus.addListener((RegisterEvent event) -> {
            event.register(Registries.BLOCK, helper -> NodeContent.registerBlocks(helper::register));
            event.register(Registries.BLOCK_ENTITY_TYPE, helper -> NodeContent.registerBlockEntities(helper::register));
            event.register(Registries.FEATURE, helper -> NodeContent.registerFeatures(helper::register));
        });
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> NodeCommands.register(event.getDispatcher()));
    }
}
