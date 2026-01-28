package com.yousefhut.createcoincraft.network;

import com.yousefhut.createcoincraft.CoinCraft;
import com.yousefhut.createcoincraft.core.ModDataComponents;
import com.yousefhut.createcoincraft.core.ModItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record WithdrawPacket(long amount) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CoinCraft.MODID, "withdraw");
    public static final Type<WithdrawPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, WithdrawPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            WithdrawPacket::amount,
            WithdrawPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(WithdrawPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ItemStack pouchStack = player.getMainHandItem();

            if (!pouchStack.is(ModItems.MONEY_POUCH.get())) {
                return;
            }

            long currentBalance = pouchStack.getOrDefault(ModDataComponents.BALANCE, 0L);

            if (payload.amount() <= 0 || payload.amount() > currentBalance) {
                return;
            }

            pouchStack.set(ModDataComponents.BALANCE, currentBalance - payload.amount());

            // long remaining = amount;
            // List<ItemStack> coinsToGive = new ArrayList<>();
            //
            // for (Coin coin : Coin.valuesHighToLow()) {
            //     if (remaining >= coin.value) {
            //         long count = remaining / coin.value;
            //         if (count > 0) {
            //             coinsToGive.add(new ItemStack(NumismaticsItems.getCoin(coin).get(), (int) count));
            //             remaining %= coin.value;
            //         }
            //     }
            // }
            //
            // for (ItemStack coinStack : coinsToGive) {
            //     if (!player.getInventory().add(coinStack)) {
            //         player.drop(coinStack, false);
            //     }
            // }
        });
    }
}
