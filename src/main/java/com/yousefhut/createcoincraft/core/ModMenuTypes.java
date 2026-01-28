package com.yousefhut.createcoincraft.core;

import com.yousefhut.createcoincraft.CoinCraft;
import com.yousefhut.createcoincraft.menu.MoneyPouchMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, CoinCraft.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MoneyPouchMenu>> MONEY_POUCH_MENU = MENUS.register(
            "money_pouch", () -> IMenuTypeExtension.create(MoneyPouchMenu::create));
}
