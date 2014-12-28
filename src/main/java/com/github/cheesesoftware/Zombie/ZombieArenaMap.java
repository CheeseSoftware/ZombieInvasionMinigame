package com.github.cheesesoftware.Zombie;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.github.cheesesoftware.ZombieInvasionMinigame.ArenaMap;

public class ZombieArenaMap extends ArenaMap
{
	//config.yml start
	public int zombieGroups = 10;
	public int zombieStartAmount = 20;
	public int zombieAmountIncrease = 10;
	public int maxZombieAmount = 200;
	public int startWave = 1;
	public int ticksBetweenZombieSpawns = 20;
	//config.yml end
	
	public ZombieArenaMap(String folder)
	{
		super(folder);
	}
	
	public void Load()
	{
		super.Load();
		this.LoadZombieConfig();
	}

	public void Save()
	{
		super.Save();
		this.SaveZombieConfig();
	}
	
	public void LoadZombieConfig()
	{
		File configFile = new File(this.folder.getAbsolutePath() + File.separator + "zombieconfig.yml");
		
		if(!configFile.exists())
		{
			try
			{
				configFile.createNewFile();
				this.SaveZombieConfig();
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
			this.zombieGroups = config.getInt("zombieGroups");
			this.zombieStartAmount = config.getInt("zombieStartAmount");
			this.zombieAmountIncrease = config.getInt("zombieAmountIncrease");
			this.maxZombieAmount = config.getInt("maxZombieAmount");
			this.startWave = config.getInt("startWave");
			this.ticksBetweenZombieSpawns = config.getInt("ticksBetweenZombieSpawns");
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	public void SaveZombieConfig()
	{
		File configFile = new File(this.folder.getAbsolutePath() + File.separator + "zombieconfig.yml");
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.set("zombieGroups", this.zombieGroups);
			config.set("zombieStartAmount", this.zombieStartAmount);
			config.set("zombieAmountIncrease", this.zombieAmountIncrease);
			config.set("maxZombieAmount", this.maxZombieAmount);
			config.set("startWave", this.startWave);
			config.set("ticksBetweenZombieSpawns", this.ticksBetweenZombieSpawns);
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
