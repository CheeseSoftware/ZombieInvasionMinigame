package io.github.gustav9797.ZombieInvasionMinigame;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import io.github.gustav9797.State.*;
import io.github.gustav9797.ZombieInvasion.ZombieInvasion;

public class Arena
{
	private IArenaState state;
	public Location middle;
	
	public Arena()
	{
		this.state = new IdleState(this);
		this.Load();
	}
	
	public void Broadcast(List<Player> players, String message)
	{
		for(Player player : players)
			player.sendMessage("&f[&9ZombieInvasion&f] " + message);
	}
	
	public void Broadcast(List<Player> players, String[] message)
	{
		for(Player player : players)
			player.sendMessage("&f[&9ZI&f] " + message);
	}
	
	public void setMiddle(Location middle)
	{
		this.middle = middle;
		this.SaveConfig();
	}

	public Location getMiddle()
	{
		return this.middle;
	}
	
	public World getLobbyWorld()
	{
		return Bukkit.getWorld("lobby");
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
		File configFile = new File(ZombieInvasion.getPlugin().getDataFolder() + File.separator + "config.yml");
		
		if(!configFile.exists())
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
		
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(configFile);
			String world = config.getString("world");
			if (world != null && ZombieInvasionMinigame.getPlugin().getServer().getWorld(world) != null && config.getVector("location") != null)
			{
				this.middle = config.getVector("location").toLocation(ZombieInvasionMinigame.getPlugin().getServer().getWorld(world));
			}
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}
	
	public void SaveConfig()
	{
		File configFile = new File(ZombieInvasion.getPlugin().getDataFolder() + File.separator + "config.yml");
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.set("world", middle.getWorld().getName());
			config.set("location", middle.toVector());
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
