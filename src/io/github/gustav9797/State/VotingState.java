package io.github.gustav9797.State;

import java.io.File;
import java.io.FilenameFilter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import io.github.gustav9797.Zombie.ZombieArenaMap;
import io.github.gustav9797.ZombieInvasionMinigame.ArenaMap;
import io.github.gustav9797.ZombieInvasionMinigame.Arena;
import io.github.gustav9797.ZombieInvasionMinigame.ZombieInvasionMinigame;

public class VotingState extends ArenaState
{
	private List<Player> votingPlayers = new ArrayList<Player>();
	private int ticksBetweenVotingMessage = 100;
	private int ticksVotingTotal = 600;
	private int ticksVotingCurrent = 0;
	private int votingTaskId = -1;
	private int startingTaskId = -1;
	private int currentStartingTicks = 0;
	private boolean voting = true;
	private Map<Integer, Map.Entry<ArenaMap, Integer>> maps = new HashMap<Integer, Map.Entry<ArenaMap, Integer>>();

	public VotingState(final Arena arena)
	{
		super(arena);
		this.LoadMaps();

		this.votingTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ZombieInvasionMinigame.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				ticksVotingCurrent += 10;
				if (ticksVotingCurrent >= ticksVotingTotal)
				{
					// End voting
					voting = false;
					final ArenaMap mostVotes = DetermineMapWon();
					arena.Broadcast(votingPlayers, "Map " + mostVotes.name + " has won! Starting game in 10 seconds..");
					startingTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ZombieInvasionMinigame.getPlugin(), new Runnable()
					{
						@Override
						public void run()
						{
							currentStartingTicks += 20;
							if (currentStartingTicks >= 100)
								arena.Broadcast(votingPlayers, currentStartingTicks / 20 + "..");
							if (currentStartingTicks >= 200)
							{
								arena.setState(new PlayingState(arena, votingPlayers, (ZombieArenaMap) mostVotes));
								Bukkit.getScheduler().cancelTask(startingTaskId);
							}
						}
					}, 0, 20);
					Bukkit.getScheduler().cancelTask(votingTaskId);
					votingTaskId = -1;
				}
				else if (ticksVotingCurrent % ticksBetweenVotingMessage == 0)
				{
					String[] votingMessage = getVotingMessage();
					arena.Broadcast(votingPlayers, votingMessage);
				}
			}
		}, 60, 10);
	}

	private ArenaMap DetermineMapWon()
	{
		int mostVotes = Integer.MIN_VALUE;
		ArenaMap mostVotesMap = null;
		for (Map.Entry<Integer, Map.Entry<ArenaMap, Integer>> map : this.maps.entrySet())
		{
			if (mostVotesMap == null || map.getValue().getValue() > mostVotes)
			{
				mostVotes = map.getValue().getValue();
				mostVotesMap = map.getValue().getKey();
			}
		}
		return mostVotesMap;
	}

	private String[] getVotingMessage()
	{
		String[] votingMessage = new String[this.maps.size() + 3];
		votingMessage[0] = "-----------------------";
		votingMessage[this.maps.size() + 1] = "-Use /vote <id> to vote!-";
		votingMessage[this.maps.size() + 2] = "-----------------------";
		int i = 1;
		for (Map.Entry<Integer, Map.Entry<ArenaMap, Integer>> map : this.maps.entrySet())
		{
			votingMessage[i] = "ID: " + map.getKey() + " Arena: " + map.getValue().getKey().name + " Votes: " + map.getValue().getValue();
			i++;
		}
		return votingMessage;
	}

	private void LoadMaps()
	{
		File mapsDirectory = new File("../maps");

		String[] maps = mapsDirectory.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File parent, String child)
			{
				return new File(parent, child).isDirectory();
			}
		});

		int i = 0;
		for (String mapName : maps)
		{
			ArenaMap map = new ZombieArenaMap(mapName);
			this.maps.put(i, new AbstractMap.SimpleEntry<ArenaMap, Integer>(map, 0));
			Bukkit.getLogger().info("Added map " + mapName);
			i++;
		}
	}

	public void TryVote(Player player, int id)
	{
		if (this.voting)
		{
			if (!player.hasMetadata("voted"))
			{
				if (maps.containsKey(id))
				{
					player.setMetadata("voted", new FixedMetadataValue(ZombieInvasionMinigame.getPlugin(), true));
					maps.get(id).setValue(maps.get(id).getValue() + 1);
					player.sendMessage("Thanks for voting!");
					for (Player temp : this.votingPlayers)
						if (!temp.hasMetadata("voted"))
							return;
					this.ticksVotingCurrent = Integer.MAX_VALUE / 2;
				}
				else
					player.sendMessage("That map doesn't exist.");
			}
			else
				player.sendMessage("You have already voted.");
		}
		else
			player.sendMessage("Voting time has ended.");
	}

	@Override
	public String getMotd()
	{
		return "Voting";
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if (voting)
		{
			Player player = event.getPlayer();
			this.votingPlayers.add(player);
			World lobbyWorld = arena.getLobbyWorld();
			if (lobbyWorld == null)
				player.sendMessage("Could not find lobby world!");
			else
				player.teleport(lobbyWorld.getSpawnLocation());

			player.sendMessage(this.getVotingMessage());
		}
		else
			event.getPlayer().kickPlayer("Oops! Game has already begun!");
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (this.votingPlayers.contains(event.getPlayer()))
			this.votingPlayers.remove(event.getPlayer());
	}

}
