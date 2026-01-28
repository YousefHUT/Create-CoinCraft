package com.yousefhut.createcoincraft.core;

import com.mojang.serialization.Codec;
import com.yousefhut.createcoincraft.CoinCraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, CoinCraft.MODID);

    public static final Supplier<DataComponentType<Long>> BALANCE = DATA_COMPONENTS.register(
            "balance",
            () -> DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build()
    );
}
