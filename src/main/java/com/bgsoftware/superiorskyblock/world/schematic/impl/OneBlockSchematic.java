package com.bgsoftware.superiorskyblock.world.schematic.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class OneBlockSchematic implements Schematic {

    private final String name;

    public OneBlockSchematic(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback) {
        pasteSchematic(island, location, callback, null);
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback, @Nullable Consumer<Throwable> onFailure) {
        BukkitExecutor.ensureMain(() -> {
            try {
                location.getBlock().setType(Material.DIRT);
                callback.run();
            } catch (Throwable error) {
                if (onFailure != null) {
                    onFailure.accept(error);
                }
            }
        });
    }

    @Override
    public Location adjustRotation(Location location) {
        return location;
    }

    @Override
    public Map<Key, Integer> getBlockCounts() {
        return Collections.emptyMap();
    }

}