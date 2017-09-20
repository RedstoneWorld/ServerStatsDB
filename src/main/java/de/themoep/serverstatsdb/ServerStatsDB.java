package de.themoep.serverstatsdb;

import de.themoep.serverstatsdb.sorter.BungeePermsSorter;
import de.themoep.serverstatsdb.sorter.LuckPermsSorter;
import de.themoep.serverstatsdb.sorter.PlayerSorter;
import de.themoep.serverstatsdb.storage.MySqlStorage;
import de.themoep.serverstatsdb.storage.Storage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.Comparator;
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
        if (getServer().getPluginManager().isPluginEnabled("BungeePerms")) {
            getLogger().log(Level.INFO, "Detected BungeePerms " + getServer().getPluginManager().getPlugin("BungeePerms").getDescription().getVersion());
        } else if (getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            getLogger().log(Level.INFO, "Detected LuckPerms " + getServer().getPluginManager().getPlugin("LuckPerms").getDescription().getVersion());
        }
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
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                if(getConfig().getString("storage.type").equalsIgnoreCase("mysql")) {
                    storage = new MySqlStorage(getConfig().getConfigurationSection("storage"));
                }
            } catch(SQLException e) {
                getLogger().log(Level.SEVERE, "Error while creating MySqlStorage! Falling back to logger!", e);
            }
        });
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

    public Comparator<Player> getPlayerSorter() {
        if (getServer().getPluginManager().isPluginEnabled("BungeePerms")) {
            return new BungeePermsSorter(this);
        } else if (getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            return new LuckPermsSorter(this);
        } else {
            return new PlayerSorter(this) {
                @Override
                public int compare(Player p, Player p2) {
                    return p.getName().compareTo(p2.getName());
                }
            };
        }
    }
}
