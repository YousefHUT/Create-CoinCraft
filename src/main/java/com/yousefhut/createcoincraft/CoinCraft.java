package com.yousefhut.createcoincraft;

import com.yousefhut.createcoincraft.core.ModDataComponents;
import com.yousefhut.createcoincraft.core.ModItems;
import com.yousefhut.createcoincraft.core.ModMenuTypes;
import com.yousefhut.createcoincraft.network.PacketHandler;
import com.yousefhut.createcoincraft.screen.MoneyPouchScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(CoinCraft.MODID)
public class CoinCraft {
    public static final String MODID = "coincraft";

    public CoinCraft(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(PacketHandler::register);
        modEventBus.addListener(this::onRegisterMenuScreens);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Client setup tasks
        });
    }

    private void onRegisterMenuScreens(final RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.MONEY_POUCH_MENU.get(), MoneyPouchScreen::new);
    }
}