package de.themoep.serverstatsdb.sorter;

import de.themoep.serverstatsdb.ServerStatsDB;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Group;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class LuckPermsSorter extends PlayerSorter {

    private final LuckPermsApi lpApi;
    private Map<UUID, Integer> rankMap = new HashMap<>();

    public LuckPermsSorter(ServerStatsDB plugin) {
        super(plugin);
        lpApi = LuckPerms.getApi();
    }

    @Override
    public int compare(Player p1, Player p2) {
        int rank1 = getRank(p1);
        int rank2 = getRank(p2);

        if (rank1 == rank2) {
            return p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase());
        }

        return -Integer.compare(rank1, rank2);
    }

    private int getRank(Player player) {
        if (rankMap.containsKey(player.getUniqueId())) {
            return rankMap.get(player.getUniqueId());
        }
        User user = lpApi.getUser(player.getUniqueId());
        if (user == null) {
            return Integer.MIN_VALUE;
        }
        int rank = Integer.MIN_VALUE;
        for (Group group : lpApi.getGroups()) {
            if (player.hasPermission("group." + group.getName())
                    && group.getWeight().isPresent()
                    && group.getWeight().getAsInt() > rank) {
                rank = group.getWeight().getAsInt();
            }
        }
        rankMap.put(player.getUniqueId(), rank);
        return rank;
    }
}
