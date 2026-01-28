package com.yousefhut.createcoincraft.network;

import com.yousefhut.createcoincraft.CoinCraft;
import com.yousefhut.createcoincraft.core.ModDataComponents;
import com.yousefhut.createcoincraft.core.ModItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record WithdrawPacket(int amount, int pouchSlotIndex) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CoinCraft.MODID, "withdraw");
    public static final Type<WithdrawPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, WithdrawPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            WithdrawPacket::amount,
            ByteBufCodecs.VAR_INT,
            WithdrawPacket::pouchSlotIndex,
            WithdrawPacket::new
    );

    private static final TreeMap<Integer, ResourceLocation> COIN_VALUES_REVERSE = new TreeMap<>(Comparator.reverseOrder());

    static {
        COIN_VALUES_REVERSE.put(4096, ResourceLocation.parse("numismatics:sun"));
        COIN_VALUES_REVERSE.put(512, ResourceLocation.parse("numismatics:crown"));
        COIN_VALUES_REVERSE.put(64, ResourceLocation.parse("numismatics:cog"));
        COIN_VALUES_REVERSE.put(16, ResourceLocation.parse("numismatics:sprocket"));
        COIN_VALUES_REVERSE.put(8, ResourceLocation.parse("numismatics:bevel"));
        COIN_VALUES_REVERSE.put(1, ResourceLocation.parse("numismatics:spur"));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(WithdrawPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ItemStack pouchStack = player.getInventory().getItem(payload.pouchSlotIndex());

            if (!pouchStack.is(ModItems.MONEY_POUCH.get())) {
                return;
            }

            int currentBalance = pouchStack.getOrDefault(ModDataComponents.BALANCE, 0);

            if (payload.amount() <= 0 || payload.amount() > currentBalance) {
                return;
            }

            pouchStack.set(ModDataComponents.BALANCE, currentBalance - payload.amount());

            int remainingAmount = payload.amount();
            List<ItemStack> coinsToGive = new ArrayList<>();

            for (Map.Entry<Integer, ResourceLocation> entry : COIN_VALUES_REVERSE.entrySet()) {
                int coinValue = entry.getKey();
                ResourceLocation coinId = entry.getValue();

                if (remainingAmount >= coinValue) {
                    int count = remainingAmount / coinValue;
                    if (count > 0) {
                        Item coinItem = BuiltInRegistries.ITEM.get(coinId);
                        if (coinItem != null) {
                            while (count > 0) {
                                int stackSize = Math.min(count, coinItem.getMaxStackSize(new ItemStack(coinItem)));
                                coinsToGive.add(new ItemStack(coinItem, stackSize));
                                count -= stackSize;
                            }
                        }
                        remainingAmount %= coinValue;
                    }
                }
            }

            for (ItemStack coinStack : coinsToGive) {
                if (!player.getInventory().add(coinStack)) {
                    player.drop(coinStack, false);
                }
            }
        });
    }
}
