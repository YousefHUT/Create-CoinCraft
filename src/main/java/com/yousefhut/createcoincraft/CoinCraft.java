package com.yousefhut.createcoincraft;

import com.yousefhut.createcoincraft.core.ModDataComponents;
import com.yousefhut.createcoincraft.core.ModItems;
import com.yousefhut.createcoincraft.core.ModMenuTypes;
import com.yousefhut.createcoincraft.network.PacketHandler;
import com.yousefhut.createcoincraft.screen.MoneyPouchScreen;
import dev.ithundxr.createnumismatics.registry.NumismaticsCreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

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
        modEventBus.addListener(this::addCreative);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }

    private void onRegisterMenuScreens(final RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.MONEY_POUCH_MENU.get(), MoneyPouchScreen::new);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == NumismaticsCreativeModeTabs.Tabs.MAIN.getKey()) {
            event.accept(ModItems.MONEY_POUCH.get());
        }
    }
}
