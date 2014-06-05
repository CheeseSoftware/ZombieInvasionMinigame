package io.github.gustav9797.ZombieInvasionMinigame;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import io.github.gustav9797.State.*;

public class Arena
{
	private IArenaState state;

	public Arena()
	{
		this.state = new IdleState(this);
		this.Load();
	}

	public void Broadcast(List<Player> players, String message)
	{
		for (Player player : players)
			player.sendMessage(ChatColor.WHITE + "[" + ChatColor.BLUE + "ZombieInvasion" + ChatColor.WHITE + "] " + message);
	}

	public void Broadcast(List<Player> players, String[] message)
	{
		for (Player player : players)
			player.sendMessage(message);
	}

	public Vector getLocation()
	{
		return new Vector(0, 0, 0);
	}

	public Vector getMiddle()
	{
		if (this.getState() instanceof PlayingState)
		{
			PlayingState state = (PlayingState) this.getState();
			ArenaMap map = state.getMap();
			if (map != null)
			{
				return new Vector(map.size / 2, map.size / 2, map.size / 2);
			}
		}
		return null;
	}

	public World getLobbyWorld()
	{
		return Bukkit.getWorld("world_lobby");
	}

	public void setState(IArenaState state)
	{
		this.state = state;
	}

	public IArenaState getState()
	{
		return this.state;
	}

	public void Load()
	{
		this.LoadConfig();
	}

	public void Save()
	{
		this.SaveConfig();
	}

	public void LoadConfig()
	{
		File configFile = new File(ZombieInvasionMinigame.getPlugin().getDataFolder() + File.separator + "config.yml");

		if (!configFile.exists())
		{
			try
			{
				configFile.createNewFile();
				this.SaveConfig();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		/*YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(configFile);
			String world = config.getString("world");
			if (world != null && ZombieInvasionMinigame.getPlugin().getServer().getWorld(world) != null && config.getVector("location") != null)
			{
				this.locationOffset = config.getVector("locationOffset");
			}
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}*/
	}

	public void SaveConfig()
	{
		/*File configFile = new File(ZombieInvasionMinigame.getPlugin().getDataFolder() + File.separator + "config.yml");
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.set("locationOffset", locationOffset);
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}*/
	}
}
