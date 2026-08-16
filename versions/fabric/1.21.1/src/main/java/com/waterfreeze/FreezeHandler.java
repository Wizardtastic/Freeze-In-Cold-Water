package com.waterfreeze;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class FreezeHandler {

    public static boolean shouldFreeze(PlayerEntity player) {
        boolean touchingWater = player.isTouchingWater();
        WaterFreezeMod.LOGGER.info("shouldFreeze check for {}: touchingWater={}, inBoat={}, leatherArmor={}, worldIsServer={}",
            player.getName().getString(),
            touchingWater,
            isInBoat(player),
            hasLeatherArmor(player),
            player.getWorld() instanceof ServerWorld);

        if (!touchingWater) {
            return false;
        }

        if (isInBoat(player) || hasLeatherArmor(player)) {
            return false;
        }

        if (player.getWorld() instanceof ServerWorld serverWorld) {
            BlockPos playerPos = player.getBlockPos();
            boolean snowing = isSnowingAt(serverWorld, playerPos);
            boolean iceNearby = hasIceNearby(serverWorld, playerPos);
            WaterFreezeMod.LOGGER.info("  pos={}, snowing={}, iceNearby={}, biome={}, raining={}",
                playerPos, snowing, iceNearby,
                serverWorld.getBiome(playerPos).value().getPrecipitation(playerPos),
                serverWorld.isRaining());
            return snowing || iceNearby;
        }

        return false;
    }

    private static boolean isSnowingAt(ServerWorld world, BlockPos pos) {
        if (!world.isRaining()) {
            return false;
        }
        Biome biome = world.getBiome(pos).value();
        return biome.getPrecipitation(pos) == Biome.Precipitation.SNOW;
    }

    private static boolean hasIceNearby(ServerWorld world, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.iterateOutwards(pos, 1, 1, 1)) {
            if (world.getBlockState(checkPos).isOf(Blocks.ICE)) {
                return true;
            }
        }
        for (BlockPos checkPos : BlockPos.iterateOutwards(pos, 3, 3, 3)) {
            if (world.getBlockState(checkPos).isOf(Blocks.PACKED_ICE)) {
                return true;
            }
        }
        for (BlockPos checkPos : BlockPos.iterateOutwards(pos, 5, 5, 5)) {
            if (world.getBlockState(checkPos).isOf(Blocks.BLUE_ICE)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasLeatherArmor(PlayerEntity player) {
        for (ItemStack stack : player.getArmorItems()) {
            if (stack.isOf(Items.LEATHER_HELMET) ||
                stack.isOf(Items.LEATHER_CHESTPLATE) ||
                stack.isOf(Items.LEATHER_LEGGINGS) ||
                stack.isOf(Items.LEATHER_BOOTS)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInBoat(PlayerEntity player) {
        Entity vehicle = player.getVehicle();
        return vehicle instanceof BoatEntity;
    }
}
