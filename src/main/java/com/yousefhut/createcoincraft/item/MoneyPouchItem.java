package com.yousefhut.createcoincraft.item;

import com.yousefhut.createcoincraft.core.ModDataComponents;
import com.yousefhut.createcoincraft.core.ModMenuTypes;
import com.yousefhut.createcoincraft.menu.MoneyPouchMenu;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MoneyPouchItem extends Item {

    public static final int MAX_BALANCE = 262144;
    private static final Map<ResourceLocation, Integer> COIN_VALUES = new HashMap<>();

    static {
        COIN_VALUES.put(ResourceLocation.parse("numismatics:sun"), 4096);
        COIN_VALUES.put(ResourceLocation.parse("numismatics:crown"), 512);
        COIN_VALUES.put(ResourceLocation.parse("numismatics:cog"), 64);
        COIN_VALUES.put(ResourceLocation.parse("numismatics:sprocket"), 16);
        COIN_VALUES.put(ResourceLocation.parse("numismatics:bevel"), 8);
        COIN_VALUES.put(ResourceLocation.parse("numismatics:spur"), 1);
    }

    public MoneyPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int slotIndex = -1;
            if (hand == InteractionHand.MAIN_HAND) {
                slotIndex = player.getInventory().selected;
            } else if (hand == InteractionHand.OFF_HAND) {
                slotIndex = 40;
            }
            if (slotIndex == -1) {
                return InteractionResultHolder.fail(stack);
            }
            final int finalSlotIndex = slotIndex;

            ((ServerPlayer) player).openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return stack.getHoverName();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                    return new MoneyPouchMenu(ModMenuTypes.MONEY_POUCH_MENU.get(), windowId, playerInventory, finalSlotIndex);
                }
            }, buf -> buf.writeInt(finalSlotIndex));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pouchStack, ItemStack carriedStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || carriedStack.isEmpty()) {
            return false;
        }

        int coinValue = getCoinValue(carriedStack);
        if (coinValue > 0) {
            int currentBalance = Optional.ofNullable(pouchStack.get(ModDataComponents.BALANCE)).orElse(0);
            int valueToAdd = coinValue * carriedStack.getCount();

            if (currentBalance + valueToAdd <= MAX_BALANCE) {
                pouchStack.set(ModDataComponents.BALANCE, currentBalance + valueToAdd);
                playInsertSound(player);
                carriedStack.setCount(0);
                return true;
            } else {
                player.displayClientMessage(Component.translatable("item.coincraft.money_pouch.full"), true);
                return false;
            }
        }
        return false;
    }

    private int getCoinValue(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return COIN_VALUES.getOrDefault(id, 0);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int balance = Optional.ofNullable(stack.get(ModDataComponents.BALANCE)).orElse(0);

        if (balance >= MAX_BALANCE) {
            tooltip.add(Component.translatable("item.coincraft.money_pouch.tooltip.full"));
        } else if (balance > 0) {
            int cogs = balance / 64;
            int spurs = balance % 64;

            StringBuilder formattedBalance = new StringBuilder("Value: ");
            if (cogs > 0) {
                formattedBalance.append(cogs).append(" Cogs");
                if (spurs > 0) {
                    formattedBalance.append(", ");
                }
            }
            if (spurs > 0) {
                formattedBalance.append(spurs).append(" Spurs");
            }
            formattedBalance.append(" (").append(balance).append(" ¤)");

            tooltip.add(Component.literal(formattedBalance.toString()));
        } else {
            tooltip.add(Component.translatable("item.coincraft.money_pouch.tooltip.empty"));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    private void playInsertSound(Player player) {
        player.level().playSound(player, player.blockPosition(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.8F, 1.0F);
    }
}
