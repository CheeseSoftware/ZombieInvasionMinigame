package io.github.gustav9797.ZombieInvasion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class SpawnPoint implements Cloneable, ConfigurationSerializable
{
	protected int id;
	protected Vector position;
	protected List<EntityType> entityTypeWhitelist = new ArrayList<EntityType>();
	protected Random r = new Random();

	public SpawnPoint(int id, Vector position)
	{
		this.id = id;
		this.position = position;
	}

	public int getId()
	{
		return this.id;
	}

	public Vector getPosition()
	{
		return this.position;
	}

	public void WhitelistEntity(EntityType entityType)
	{
		this.entityTypeWhitelist.add(entityType);
	}

	public void RemoveWhitelistEntity(EntityType entityType)
	{
		if (this.entityTypeWhitelist.contains(entityType))
			this.entityTypeWhitelist.remove(entityType);
	}

	public boolean canSpawnEntity(EntityType entityType)
	{
		if(this.entityTypeWhitelist.size() <= 0)
			return true;
		for (EntityType e : this.entityTypeWhitelist)
		{
			if (e.equals(entityType))
				return true;
		}
		return false;
	}

	public List<EntityType> getEntityTypes()
	{
		return this.entityTypeWhitelist;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("id", this.id);
		result.put("position", this.position);
		List<String> temp = new ArrayList<String>();
		for (EntityType e : this.entityTypeWhitelist)
			temp.add(e.toString());
		result.put("entitytypes", temp);
		return result;
	}

	@SuppressWarnings(
	{ "unchecked", "deprecation" })
	public static SpawnPoint deserialize(Map<String, Object> args)
	{
		Vector position = (Vector) args.get("position");
		if (position != null)
		{
			SpawnPoint spawnPoint = new SpawnPoint((int) args.get("id"), position);
			List<String> temp = (List<String>) args.get("entitytypes");
			if (temp != null)
			{
				for (String s : temp)
					spawnPoint.WhitelistEntity(EntityType.fromName(s));
			}
			return spawnPoint;
		}
		return null;
	}
}
