package de.themoep.serverstatsdb;

import de.themoep.serverstatsdb.storage.MySqlStorage;
import de.themoep.serverstatsdb.storage.Storage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.logging.Level;

/**
 * ServerStatsDB
 * Copyright 2016 Max Lee (https://github.com/Phoenix616/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License as published by
 * the Mozilla Foundation, version 2.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v2.0 for more details.
 *
 * You should have received a copy of the Mozilla Public License v2.0
 * along with this program. If not, see <http://mozilla.org/MPL/2.0/>.
 */

public class ServerStatsDB extends JavaPlugin {

    private ServerStatsDB plugin;
    private Storage storage;
    private int period;
    private BukkitTask collectorTask;

    public void onEnable() {
        plugin = this;
        loadConfig();
    }

    public void onDisable() {
        if(collectorTask != null) {
            collectorTask.cancel();
        }
        if(storage != null) {
            try {
                storage.close();
                storage = null;
            } catch(Exception e) {
                getLogger().log(Level.SEVERE, "Error while closing " + storage.getClass().getSimpleName() + "!", e);
            }
        }
    }

    public void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        onDisable();
        period = getConfig().getInt("period") * 20;
        collectorTask = new StatsCollector(plugin).runTaskTimer(plugin, 10 * 10, period);
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if(getConfig().getString("storage.type").equalsIgnoreCase("mysql")) {
                        storage = new MySqlStorage(getConfig().getConfigurationSection("storage"));
                    }
                } catch(SQLException e) {
                    getLogger().log(Level.SEVERE, "Error while creating MySqlStorage! Falling back to logger!", e);
                }
            }
        }.runTaskAsynchronously(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, final String[] args){
        if(args.length > 0 && "reload".equalsIgnoreCase(args[0])) {
            loadConfig();
            sender.sendMessage(ChatColor.YELLOW + "Config reloaded!");
            return true;
        }
        sender.sendMessage(ChatColor.AQUA + getName() + ChatColor.GRAY + " v" + getDescription().getVersion() + " by " + getDescription().getAuthors().get(0));
        return false;
    }

    public Storage getStorage() {
        return storage;
    }

    public int getPeriod() {
        return period;
    }
}
