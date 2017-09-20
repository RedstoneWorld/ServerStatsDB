package de.themoep.serverstatsdb.sorter;

import de.themoep.serverstatsdb.ServerStatsDB;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class BungeePermsSorter extends PlayerSorter {

    private final BungeePerms bp;
    private Map<UUID, Integer> rankMap = new HashMap<>();

    public BungeePermsSorter(ServerStatsDB plugin) {
        super(plugin);
        bp = BungeePerms.getInstance();
    }

    @Override
    public int compare(Player p1, Player p2) {
        int rank1 = getRank(p1);
        int rank2 = getRank(p2);

        if (rank1 == rank2) {
            return p1.getName().compareTo(p2.getName());
        }

        return -Integer.compare(rank1, rank2);
    }

    private int getRank(Player player) {
        if (rankMap.containsKey(player.getUniqueId())) {
            return rankMap.get(player.getUniqueId());
        }
        User user = bp.getPermissionsManager().getUser(player.getUniqueId(), false);
        if (user == null) {
            return Integer.MIN_VALUE;
        }
        int rank = Integer.MAX_VALUE;
        for (Group group : user.getGroups()) {
            if (group.getRank() < rank) {
                rank = group.getRank();
            }
        }
        if (rank < Integer.MAX_VALUE) {
            rankMap.put(player.getUniqueId(), rank);
        }
        return rank;
    }
}
