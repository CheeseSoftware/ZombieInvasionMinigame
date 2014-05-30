package io.github.gustav9797.ZombieInvasion;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Lobby implements Listener
{
	Location location;
	String defaultWorldName = "world";
	Map<String, Arena> arenas;
	JavaPlugin plugin;
	List<Vector> signs = new LinkedList<Vector>();
	File configFile;
	File signConfigFile;

	public Lobby(Map<String, Arena> arenas, JavaPlugin plugin)
	{
		this.configFile = new File(plugin.getDataFolder() + File.separator + "lobby.yml");
		this.signConfigFile = new File(plugin.getDataFolder() + File.separator + "lobbysigns.yml");
		this.location = new Location(plugin.getServer().getWorld(this.defaultWorldName), 0, 0, 0);
		this.arenas = arenas;
		this.plugin = plugin;

		if (!configFile.exists())
		{
			try
			{
				configFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			this.SaveConfig();
		}

		if (!signConfigFile.exists())
		{
			try
			{
				signConfigFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			this.SaveSignConfig();
		}
		this.Load();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public Location getLocation()
	{
		return this.location;
	}

	public void setLocation(Location l)
	{
		this.location = l;
		Save();
	}

	protected void SaveConfig()
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.set("world", this.location.getWorld().getName());
			config.set("location", this.location.toVector());
			config.set("yaw", this.location.getYaw());
			config.set("pitch", this.location.getPitch());
			config.save(this.configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void LoadConfig()
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(configFile);
			World world = Bukkit.getServer().getWorld(config.getString("world"));
			this.location = config.getVector("location").toLocation(world);
			location.setYaw((float) config.getDouble("yaw"));
			location.setPitch((float) config.getDouble("pitch"));
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	protected void SaveSignConfig()
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.set("signs", this.signs);
			config.save(this.signConfigFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	protected void LoadSignConfig()
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(signConfigFile);
			this.signs = (List<Vector>) config.getList("signs");
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	public void Save()
	{
		this.SaveConfig();
		this.SaveSignConfig();
	}

	public void Load()
	{
		this.LoadConfig();
		this.LoadSignConfig();
	}

	private void AddSign(Location l)
	{
		while (this.signs.contains(l.toVector()))
			this.signs.remove(l.toVector());
		this.signs.add(l.toVector());
		this.Save();
	}

	private void RemoveSign(Location l)
	{
		while (this.signs.contains(l.toVector()))
			this.signs.remove(l.toVector());
		this.Save();
	}

	public void UpdateSigns()
	{
		for (Vector l : this.signs)
		{
			BlockState state = this.location.getWorld().getBlockAt(l.toLocation(this.location.getWorld())).getState();
			if (state instanceof Sign)
			{
				Sign sign = (Sign) state;
				String arenaName = sign.getLine(1);
				if (this.arenas.containsKey(arenaName))
				{
					Arena arena = arenas.get(arenaName);
					sign.setLine(2, "Players: " + arena.players.size());
					sign.setLine(3, "Arena: " + arena.name);
					sign.update();
				}
			}
			else
				plugin.getServer().getLogger().warning("Block at " + l.toString() + " is not a sign!");
		}
	}

	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.hasBlock())
		{
			Block block = event.getClickedBlock();
			if (block.getType() == Material.WALL_SIGN)
			{
				Sign sign = (Sign) block.getState();
				String[] text = sign.getLines();
				if (this.signs.contains(sign.getLocation().toVector()))
				{
					String arenaName = text[1];
					if (arenas.containsKey(arenaName))
					{
						Arena arena = arenas.get(arenaName);
						Player player = event.getPlayer();
						if (arena != null)
						{
							if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
							{
								arena.JoinPlayer(player);
							}
						}
					}
				}
			}
		}
	}

	public void onSignChange(SignChangeEvent event)
	{
		Player player = event.getPlayer();
		if (player.hasPermission("zombieinvasion.admin"))
		{
			String[] text = event.getLines();
			if (text[0].equals("ZombieInvasion"))
			{
				String arenaName = text[1];
				if (arenas.containsKey(arenaName))
				{
					this.AddSign(event.getBlock().getLocation());
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
					{
						@Override
						public void run()
						{
							UpdateSigns();
						}
					}, 1L);
					player.sendMessage("[ZombieInvasion] Lobby sign successfully placed!");
				}
				else
					player.sendMessage("[ZombieInvasion] Arena " + arenaName + " doesn't exist.");
			}
		}
	}

	public void onBlockBreak(BlockBreakEvent event)
	{
		if (this.signs.contains(event.getBlock().getLocation()))
		{
			this.RemoveSign(event.getBlock().getLocation());
			event.getPlayer().sendMessage("[ZombieInvasion] Lobby sign removed!");
		}
	}

	public void onPlayerQuit(PlayerQuitEvent event)
	{
		this.UpdateSigns();
	}

	public void onPlayerJoin(PlayerJoinEvent event)
	{
		this.UpdateSigns();
	}
}
