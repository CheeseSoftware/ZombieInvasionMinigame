package io.github.gustav9797.ZombieInvasion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SpawnPointManager
{
	private Map<Integer, SpawnPoint> spawnPoints = new HashMap<Integer, SpawnPoint>();
	private File configFile;
	private Random r = new Random();

	public SpawnPointManager(Arena arena)
	{
		configFile = new File(ZombieInvasion.getPlugin().getDataFolder() + File.separator + arena.name + File.separator + "spawnpoints.yml");
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
			this.Save();
		}
	}

	public void Save()
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			List<SpawnPoint> temp = new ArrayList<SpawnPoint>();
			for (SpawnPoint s : this.spawnPoints.values())
				if (s instanceof SpawnPoint)
					temp.add((SpawnPoint) s);
			config.set("monsterSpawnPoints", temp);
			config.save(this.configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void Load()
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(this.configFile);
			this.spawnPoints.clear();
			List<SpawnPoint> spawnPoints = (List<SpawnPoint>) config.getList("monsterSpawnPoints");
			if (spawnPoints != null)
			{
				for (SpawnPoint m : spawnPoints)
					this.spawnPoints.put(m.getId(), m);
			}
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean HasSpawnPoint(int id)
	{
		return this.spawnPoints.containsKey(id);
	}

	@SuppressWarnings("deprecation")
	public void Show(Player player)
	{
		for (SpawnPoint spawnPoint : this.spawnPoints.values())
			player.sendBlockChange(spawnPoint.getPosition().toLocation(player.getWorld()), Material.SPONGE, (byte) 0);
	}

	@SuppressWarnings("deprecation")
	public void Hide(Player player)
	{
		for (SpawnPoint spawnPoint : this.spawnPoints.values())
			player.sendBlockChange(spawnPoint.getPosition().toLocation(player.getWorld()), Material.AIR, (byte) 0);
	}

	public void AddSpawnPoint(int id, SpawnPoint s)
	{
		if (!spawnPoints.containsKey(id))
		{
			this.spawnPoints.put(id, s);
			this.Save();
		}
	}

	public void RemoveSpawnPoint(int id)
	{
		if (this.spawnPoints.containsKey(id))
			this.spawnPoints.remove(id);
		this.Save();
	}

	public Collection<SpawnPoint> getSpawnPoints()
	{
		return this.spawnPoints.values();
	}

	public SpawnPoint getSpawnPoint(int id)
	{
		if (this.spawnPoints.containsKey(id))
			return this.spawnPoints.get(id);
		return null;
	}

	public SpawnPoint getRandomSpawnPoint()
	{
		List<SpawnPoint> temp = new ArrayList<SpawnPoint>();
		for (SpawnPoint p : this.spawnPoints.values())
			temp.add((SpawnPoint) p);
		if (temp.size() > 0)
			return temp.get(r.nextInt(temp.size()));
		return null;
	}

	/*public SpawnPoint getRandomPlayerSpawnPoint()
	{
		List<SpawnPoint> temp = new ArrayList<SpawnPoint>();
		for (SpawnPoint p : this.spawnPoints.values())
			if (p.hasEntityType(EntityType.PLAYER))
				temp.add((SpawnPoint) p);
		if (temp.size() > 0)
			return temp.get(r.nextInt(temp.size()));
		return null;
	}*/

	public SpawnPoint getRandomMonsterSpawnPoint()
	{
		List<SpawnPoint> temp = new ArrayList<SpawnPoint>();
		for (SpawnPoint p : this.spawnPoints.values())
			temp.add((SpawnPoint) p);
		if (temp.size() > 0)
			return temp.get(r.nextInt(temp.size()));
		return null;
	}

	public int getFreeSpawnPointId()
	{
		for (int i = 0;; i++)
		{
			if (!spawnPoints.containsKey(i))
				return i;
		}
	}
}
