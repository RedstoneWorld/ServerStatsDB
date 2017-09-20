package de.themoep.serverstatsdb.sorter;

import de.themoep.serverstatsdb.ServerStatsDB;
import org.bukkit.entity.Player;

import java.util.Comparator;

public abstract class PlayerSorter implements Comparator<Player> {
    protected final ServerStatsDB plugin;

    public PlayerSorter(ServerStatsDB plugin) {
        this.plugin = plugin;
    }
}
