package com.yousefhut.createcoincraft;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(CreateCoinCraft.MODID)
public class CreateCoinCraft {
    public static final String MODID = "coincraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateCoinCraft(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("--- CreateCoinCraft ---");

        checkModPresence("create", "Create");
        checkModPresence("numismatics", "Numismatics");

        event.enqueueWork(() -> {
            checkTagPresence("c", "plates/zinc");
        });

        LOGGER.info("--- Dependent mods are checked ---");
    }

    private void checkModPresence(String modId, String displayName) {
        if (ModList.get().isLoaded(modId)) {
            LOGGER.info("[CHECK] {} mod is loaded.", displayName);
        } else {
            LOGGER.warn("[WARN] {} mod not found. Some recipes may cant be show", displayName);
        }
    }

    private void checkTagPresence(String namespace, String path) {
        TagKey<Item> zincPlates = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath(namespace, path));
        
        long count = BuiltInRegistries.ITEM.getTag(zincPlates)
                .map(named -> named.size())
                .orElse(0);

        if (count > 0) {
            LOGGER.info("[TAG KONTROL] #c:plates/zinc tag has items. (Founded item count: {})", count);
        } else {
            LOGGER.error("[TAG KONTROL] #c:plates/zinc tag is empty.");
        }
    }
}