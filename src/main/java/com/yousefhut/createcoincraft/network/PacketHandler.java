package com.yousefhut.createcoincraft.network;

import com.yousefhut.createcoincraft.CoinCraft;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CoinCraft.MODID);
        registrar.playToServer(WithdrawPacket.TYPE, WithdrawPacket.STREAM_CODEC, (payload, context) -> payload.handle(payload, context));
    }
}
