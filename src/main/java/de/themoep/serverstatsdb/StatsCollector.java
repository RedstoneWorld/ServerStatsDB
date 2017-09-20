package de.themoep.serverstatsdb;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.Iterator;
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
    private long lastRun = 0;

    public StatsCollector(ServerStatsDB plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if(lastRun == 0) {
            lastRun = System.currentTimeMillis();
            return;
        }
        final int playerCount = plugin.getServer().getOnlinePlayers().size();
        double tps = 20.0 * plugin.getPeriod() * 50 / (System.currentTimeMillis() - lastRun);
        if (tps == 0) {
            return;
        }
        lastRun = System.currentTimeMillis();
        final double fTps = tps > 20 ? 20 : new BigDecimal(tps).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        StringBuilder pib = new StringBuilder("[");
        for (Iterator<? extends Player> i = plugin.getServer().getOnlinePlayers().stream().sorted(plugin.getPlayerSorter()).iterator(); i.hasNext();) {
            pib.append(i.next().getUniqueId());
            if (i.hasNext()) {
                pib.append(",");
            }
        }
        pib.append("]");
        final String playerIds = pib.toString();
        if(plugin.getStorage() != null) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    plugin.getStorage().log(playerCount, fTps, playerIds);
                } catch(Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error while adding log entry to " + plugin.getStorage().getClass().getSimpleName() + "!", e);
                }
            });
        } else {
            plugin.getLogger().log(Level.INFO, "Playercount: " + playerCount + " - TPS: " + fTps + " - Players: " + playerIds);
        }
    }
}
