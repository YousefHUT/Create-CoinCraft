package com.yousefhut.createcoincraft.core;

import com.yousefhut.createcoincraft.CoinCraft;
import com.yousefhut.createcoincraft.item.MoneyPouchItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CoinCraft.MODID);

    public static final DeferredItem<Item> MONEY_POUCH = ITEMS.register("money_pouch", () -> new MoneyPouchItem(new Item.Properties().stacksTo(1)));
}
