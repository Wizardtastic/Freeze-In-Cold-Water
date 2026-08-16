package com.waterfreeze;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaterFreezeMod implements ModInitializer {
    public static final String MOD_ID = "waterfreeze";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayer(player);
            }
        });
        LOGGER.info("Water Freeze Mod initialized");
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        boolean shouldFreeze = FreezeHandler.shouldFreeze(player);

        if (shouldFreeze) {
            int threshold = player.getMinFreezeDamageTicks();
            player.setFrozenTicks(threshold + 2);

            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, 40, 0, false, false, true
            ));
        } else {
            if (player.getFrozenTicks() > 0) {
                player.setFrozenTicks(0);
            }
            player.removeStatusEffect(StatusEffects.SLOWNESS);
        }
    }
}
