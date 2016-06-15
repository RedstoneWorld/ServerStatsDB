package de.themoep.serverstatsdb;

import de.themoep.serverstatsdb.storage.Storage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.logging.Level;

/**
 * Copyright 2016 Max Lee (https://github.com/Phoenix616/)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License as published by
 * the Mozilla Foundation, version 2.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v2.0 for more details.
 * <p/>
 * You should have received a copy of the Mozilla Public License v2.0
 * along with this program. If not, see <http://mozilla.org/MPL/2.0/>.
 */
public class StatsCollector extends BukkitRunnable {
    private final ServerStatsDB plugin;
    private long lastRun = System.currentTimeMillis();

    public StatsCollector(ServerStatsDB plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        final int playerCount = plugin.getServer().getOnlinePlayers().size();
        final double tps = (lastRun + plugin.getPeriod() * 50) / System.currentTimeMillis() * 20;
        lastRun = System.currentTimeMillis();
        if(plugin.getStorage() != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        plugin.getStorage().log(playerCount, tps);
                    } catch(Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Error while adding log entry to " + plugin.getStorage().getClass().getSimpleName() + "!", e);
                    }
                }
            }.runTaskAsynchronously(plugin);
        } else {
            plugin.getLogger().log(Level.INFO, "Playercount: " + playerCount + " - TPS: " + tps);
        }
    }
}
