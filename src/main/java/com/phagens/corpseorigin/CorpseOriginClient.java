package com.phagens.corpseorigin;

import com.phagens.corpseorigin.client.Renderer.block.QiXingGuanRenderer;
import com.phagens.corpseorigin.client.Renderer.entity.ZbrFishRenderer;
import com.phagens.corpseorigin.register.BlockEntityRegistry;
import com.phagens.corpseorigin.register.EntityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CorpseOrigin.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = CorpseOrigin.MODID, value = Dist.CLIENT)
public class CorpseOriginClient {
    public CorpseOriginClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        CorpseOrigin.LOGGER.info("HELLO FROM CLIENT SETUP");
        CorpseOrigin.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityRegistry.QI_XING_GUANS.get(), QiXingGuanRenderer::new);
        event.registerEntityRenderer((EntityType<com.phagens.corpseorigin.Entity.ZbrFishEntity>) EntityRegistry.ZBR_FISH.get(), ZbrFishRenderer::new);
    }
}
