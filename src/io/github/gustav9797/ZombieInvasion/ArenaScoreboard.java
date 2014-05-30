package io.github.gustav9797.ZombieInvasion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import ostkaka34.OstEconomyPlugin.OstEconomyPlugin;

public class ArenaScoreboard
{
	protected Arena arena;
	protected int tickTaskId;
	protected Map<String, Objective> objectives = new HashMap<String, Objective>();
	// protected Scoreboard scoreboard;
	protected Map<Player, Scoreboard> scoreboards = new HashMap<Player, Scoreboard>();

	public ArenaScoreboard(Arena arena)
	{
		this.arena = arena;
		this.tickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ZombieInvasion.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				Tick();
			}
		}, 0L, 10L);
	}

	@SuppressWarnings("deprecation")
	private void UpdatePlayerInfo()
	{
		OstEconomyPlugin economy = (OstEconomyPlugin) ZombieInvasion.getEconomyPlugin();
		List<Player> players = new ArrayList<Player>(arena.players);
		for (Player player : players)
		{
			Objective title = player.getScoreboard().getObjective("stats");
			title.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "XP:")).setScore((int) economy.getXp(player));
			title.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "Money:")).setScore((int) economy.getMoney(player));
			title.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "Game time:")).setScore((int) Math.round(arena.getTotalGameTicks() / 20));
			title.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "Current wave:")).setScore(((arena instanceof ZombieArena ? ((ZombieArena) arena).getCurrentWave() : 0)));
			title.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "Monsters left:")).setScore(((arena instanceof ZombieArena ? ((ZombieArena) arena).monsters.size() : 0)));
		}
	}

	private void Tick()
	{
		this.UpdatePlayerInfo();
	}

	public void RemovePlayerScoreboard(Player player)
	{
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	public void AddPlayerScoreboard(Player player)
	{
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective stats = scoreboard.registerNewObjective("stats", "dummy");
		stats.setDisplaySlot(DisplaySlot.SIDEBAR);
		stats.setDisplayName("Stats");
		player.setScoreboard(scoreboard);
	}

}
