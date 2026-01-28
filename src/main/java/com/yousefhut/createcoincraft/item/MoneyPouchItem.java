package com.yousefhut.createcoincraft.item;

import com.yousefhut.createcoincraft.CoinCraft;
import com.yousefhut.createcoincraft.core.ModDataComponents;
import com.yousefhut.createcoincraft.menu.MoneyPouchMenu;
import com.yousefhut.createcoincraft.core.ModMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MoneyPouchItem extends Item {

    public MoneyPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ((ServerPlayer) player).openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return stack.getHoverName();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                    return new MoneyPouchMenu(ModMenuTypes.MONEY_POUCH_MENU.get(), windowId, playerInventory, stack);
                }
            }, buf -> ItemStack.STREAM_CODEC.encode(buf, stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack pouchStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }

        ItemStack carriedStack = slot.getItem();
        if (carriedStack.isEmpty()) {
            return false;
        }

        long coinValue = getCoinValue(carriedStack);
        if (coinValue > 0) {
            long currentBalance = Optional.ofNullable(pouchStack.get(ModDataComponents.BALANCE)).orElse(0L);
            long valueToAdd = coinValue * carriedStack.getCount();

            pouchStack.set(ModDataComponents.BALANCE, currentBalance + valueToAdd);

            playInsertSound(player);
            carriedStack.setCount(0);
            return true;
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pouchStack, ItemStack carriedStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }

        if (carriedStack.isEmpty()) {
            return false;
        }

        long coinValue = getCoinValue(carriedStack);
        if (coinValue > 0) {
            long currentBalance = Optional.ofNullable(pouchStack.get(ModDataComponents.BALANCE)).orElse(0L);
            long valueToAdd = coinValue * carriedStack.getCount();

            pouchStack.set(ModDataComponents.BALANCE, currentBalance + valueToAdd);

            playInsertSound(player);
            carriedStack.setCount(0);
            return true;
        }

        return false;
    }

    private long getCoinValue(ItemStack stack) {
        ResourceLocation id = stack.getItem().builtInRegistryHolder().key().location();
        if (id.getPath().contains("spur")) {
            return 1;
        } else if (id.getPath().contains("bevel")) {
            return 10;
        } else if (id.getPath().contains("cog")) {
            return 100;
        }
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        long balance = Optional.ofNullable(stack.get(ModDataComponents.BALANCE)).orElse(0L);
        tooltip.add(Component.translatable("item.coincraft.money_pouch.tooltip", balance));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    private void playInsertSound(Player player) {
        player.level().playSound(player, player.blockPosition(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.8F, 1.0F);
    }
}