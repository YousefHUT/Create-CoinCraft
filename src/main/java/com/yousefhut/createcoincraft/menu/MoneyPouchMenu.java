package com.yousefhut.createcoincraft.menu;

import com.yousefhut.createcoincraft.core.ModDataComponents;
import com.yousefhut.createcoincraft.core.ModItems;
import com.yousefhut.createcoincraft.core.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MoneyPouchMenu extends AbstractContainerMenu {
    public final ItemStack pouch;
    private final Player player;

    public MoneyPouchMenu(@Nullable MenuType<?> type, int windowId, Inventory playerInventory, ItemStack pouch) {
        super(type, windowId);
        this.pouch = pouch;
        this.player = playerInventory.player;

        // No slots for player inventory or hotbar
    }

    public static MoneyPouchMenu create(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        ItemStack pouch = ItemStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) data);
        return new MoneyPouchMenu(ModMenuTypes.MONEY_POUCH_MENU.get(), windowId, playerInventory, pouch);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY; // No quick move as there are no slots
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return this.pouch.is(ModItems.MONEY_POUCH.get());
    }

    public long getBalance() {
        return this.pouch.getOrDefault(ModDataComponents.BALANCE, 0L);
    }
}