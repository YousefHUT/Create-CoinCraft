package com.yousefhut.createcoincraft.menu;

import com.yousefhut.createcoincraft.core.ModDataComponents;
import com.yousefhut.createcoincraft.core.ModItems;
import com.yousefhut.createcoincraft.core.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MoneyPouchMenu extends AbstractContainerMenu {
    private final Player player;
    private final int pouchSlotIndex;

    public MoneyPouchMenu(@Nullable MenuType<?> type, int windowId, Inventory playerInventory, int pouchSlotIndex) {
        super(type, windowId);
        this.player = playerInventory.player;
        this.pouchSlotIndex = pouchSlotIndex;
    }

    public static MoneyPouchMenu create(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        int pouchSlotIndex = data.readInt();
        return new MoneyPouchMenu(ModMenuTypes.MONEY_POUCH_MENU.get(), windowId, playerInventory, pouchSlotIndex);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        ItemStack pouchStack = playerIn.getInventory().getItem(this.pouchSlotIndex);
        return pouchStack.is(ModItems.MONEY_POUCH.get());
    }

    public int getBalance() {
        ItemStack pouchStack = player.getInventory().getItem(this.pouchSlotIndex);
        return pouchStack.getOrDefault(ModDataComponents.BALANCE, 0);
    }

    public int getPouchSlotIndex() {
        return this.pouchSlotIndex;
    }
}
