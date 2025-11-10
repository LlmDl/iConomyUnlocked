package io.github.townyadvanced.iconomy.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PlayerNameCache {

	static long FIVE_MINUTES = 1000 * 60 * 5;
	static long time = 0;
	static List<String> names = new ArrayList<>();

	public static List<String> getPlayerNames() {
		boolean notOld = time + FIVE_MINUTES > System.currentTimeMillis();
		if (notOld)
			return names;

		names.clear();
		time = System.currentTimeMillis();
		for (OfflinePlayer player : Arrays.asList(Bukkit.getOfflinePlayers())) {
			if (player == null || player.getName() == null)
				continue;
			names.add(player.getName());
		}

		return names;
	}
}
