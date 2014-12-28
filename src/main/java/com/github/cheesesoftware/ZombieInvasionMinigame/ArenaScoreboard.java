package com.github.cheesesoftware.ZombieInvasionMinigame;

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

import com.github.cheesesoftware.OstEconomyPlugin.OstEconomyPlugin;
import com.github.cheesesoftware.State.PlayingState;

public class ArenaScoreboard
{
	protected PlayingState playingState;
	protected int tickTaskId;
	protected Map<String, Objective> objectives = new HashMap<String, Objective>();
	// protected Scoreboard scoreboard;
	protected Map<Player, Scoreboard> scoreboards = new HashMap<Player, Scoreboard>();

	public ArenaScoreboard(PlayingState playingState)
	{
		this.playingState = playingState;
		this.tickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ZombieInvasionMinigame.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				Tick();
			}
		}, 0L, 10L);
	}

	private void UpdatePlayerInfo()
	{
		OstEconomyPlugin economy = (OstEconomyPlugin) ZombieInvasionMinigame.getEconomyPlugin();
		List<Player> players = new ArrayList<Player>(playingState.getPlayers());
		for (Player player : players)
		{
			Objective title = player.getScoreboard().getObjective("stats");
			
			title.getScore(ChatColor.GOLD + "XP:").setScore((int) economy.getXp(player));
			title.getScore(ChatColor.GOLD + "Money:").setScore((int) economy.getMoney(player));
			title.getScore(ChatColor.GOLD + "Game time:").setScore((int) Math.round(playingState.getTotalGameTicks() / 20));
			title.getScore(ChatColor.GOLD + "Current wave:").setScore(playingState.getCurrentWave());
			title.getScore(ChatColor.GOLD + "Monsters left:").setScore(playingState.getMonsters().size());
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
