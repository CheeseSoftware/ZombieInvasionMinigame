package io.github.gustav9797.ZombieInvasionMinigame;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

public class ArenaMap
{
	protected File folder;
	public String name;

	// config.yml start
	public int size = 96;
	public int maxPlayers = 10;
	public Location spawnLocation = new Location(null, 0, 80, 0, 0, 0);
	public String schematicFileName = "";
	public int startAtPlayerCount = 1;
	public int secondsBeforeStart = 60;
	public int secondsWaitFirstTimeStart = 30;
	// config.yml end

	// border.yml start
	public Material borderMaterial = Material.GLASS;
	public int borderHeight = 50;
	public boolean borderHasRoof = false;
	// border.yml end

	// potionregions.yml start
	public List<PotionRegion> potionRegions = new ArrayList<PotionRegion>();
	// potionregions.yml end

	public ArenaMap(String name)
	{
		File folder = new File("../maps/" + name);
		if (folder.isDirectory() && folder.exists())
		{
			this.folder = folder;
			this.name = name;
		}
		this.Load();
	}

	public void Load()
	{
		this.LoadConfig();
		this.LoadBorder();
		this.LoadPotionregionConfig();
	}

	public void Save()
	{
		this.SaveConfig();
		this.SaveBorder();
		this.SavePotionregionConfig();
	}

	public void LoadConfig()
	{
		File configFile = new File(this.folder.getAbsolutePath() + File.separator + "config.yml");
		
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
			this.size = config.getInt("size");
			this.maxPlayers = config.getInt("maxPlayers");
			Vector spawnPos = config.getVector("spawnLocation");
			this.spawnLocation = spawnPos.toLocation(null, (float) config.getDouble("spawnLocationYaw"), (float) config.getDouble("SpawnLocationPitch"));
			this.schematicFileName = config.getString("schematicFileName");
			this.startAtPlayerCount = config.getInt("startAtPlayerCount");
			this.secondsBeforeStart = config.getInt("secondsBeforeStart");
			this.secondsWaitFirstTimeStart = config.getInt("secondsWaitFirstTimeStart");
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	public void SaveConfig()
	{
		File configFile = new File(this.folder.getAbsolutePath() + File.separator + "config.yml");
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.set("size", this.size);
			config.set("maxPlayers", this.maxPlayers);
			config.set("spawnLocation", this.spawnLocation.toVector());
			config.set("spawnLocationYaw", this.spawnLocation.getYaw());
			config.set("spawnLocationPitch", this.spawnLocation.getPitch());
			config.set("schematicFileName", this.schematicFileName);
			config.set("startAtPlayerCount", this.startAtPlayerCount);
			config.set("secondsBeforeStart", this.secondsBeforeStart);
			config.set("secondsWaitFirstTimeStart", this.secondsWaitFirstTimeStart);
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void LoadBorder()
	{
		File configFile = new File(this.folder.getAbsolutePath() + File.separator + "border.yml");
		
		if(!configFile.exists())
		{
			try
			{
				configFile.createNewFile();
				this.SaveBorder();
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
			this.borderMaterial = Material.getMaterial(config.getString("borderMaterial"));
			if (this.borderMaterial == null)
				Bukkit.getLogger().severe("Loaded invalid border material in map " + this.name);
			this.borderHeight = config.getInt("borderHeight");
			this.borderHasRoof = config.getBoolean("borderHasRoof");
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	public void SaveBorder()
	{
		File configFile = new File(this.folder.getAbsolutePath() + File.separator + "border.yml");
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.set("borderMaterial", this.borderMaterial.toString());
			config.set("borderHeight", this.borderHeight);
			config.set("borderHasRoof", this.borderHasRoof);
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void LoadPotionregionConfig()
	{
		File configFile = new File(this.folder.getAbsolutePath() + File.separator + "potionregions.yml");
		
		if(!configFile.exists())
		{
			try
			{
				configFile.createNewFile();
				this.SavePotionregionConfig();
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
			@SuppressWarnings("unchecked")
			ArrayList<PotionRegion> temp = (ArrayList<PotionRegion>) config.getList("regions");
			if (temp == null)
				this.potionRegions = new ArrayList<PotionRegion>();
			else
				this.potionRegions = new ArrayList<PotionRegion>(temp);
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	public void SavePotionregionConfig()
	{
		File configFile = new File(this.folder.getAbsolutePath() + File.separator + "potionregions.yml");
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.set("regions", this.potionRegions);
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
