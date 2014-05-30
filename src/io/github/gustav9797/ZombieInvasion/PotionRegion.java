package io.github.gustav9797.ZombieInvasion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

public class PotionRegion implements Cloneable, ConfigurationSerializable
{
	Region region;
	List<PotionEffect> effects;

	public PotionRegion(Region region, List<PotionEffect> effects)
	{
		this.region = region;
		this.effects = effects;
	}

	public void AddEffect(PotionEffect effect)
	{
		this.effects.add(effect);
	}

	public Region getRegion()
	{
		return this.region;
	}

	public boolean isNeutral()
	{
		return this.effects.isEmpty();
	}

	public List<PotionEffect> getEffects()
	{
		return this.effects;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof PotionRegion))
			return false;
		PotionRegion otherMyClass = (PotionRegion) other;
		if (otherMyClass.region.getMaximumPoint().equals(this.region.getMaximumPoint()) && otherMyClass.region.getMinimumPoint().equals(this.region.getMinimumPoint()))
			return true;
		return false;
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("world", this.region.getWorld().getName());

		Vector minimumPoint = new Vector();
		minimumPoint.setX(this.region.getMinimumPoint().getBlockX());
		minimumPoint.setY(this.region.getMinimumPoint().getBlockY());
		minimumPoint.setZ(this.region.getMinimumPoint().getBlockZ());
		result.put("minimumPoint", minimumPoint);

		Vector maximumPoint = new Vector();
		maximumPoint.setX(this.region.getMaximumPoint().getBlockX());
		maximumPoint.setY(this.region.getMaximumPoint().getBlockY());
		maximumPoint.setZ(this.region.getMaximumPoint().getBlockZ());
		result.put("maximumPoint", maximumPoint);

		result.put("effects", this.effects);
		return result;
	}

	@SuppressWarnings("unchecked")
	public static PotionRegion deserialize(Map<String, Object> args)
	{
		String worldName = (String) args.get("world");
		World world = Bukkit.getWorld(worldName);
		if (world != null)
		{
			com.sk89q.worldedit.Vector minimumPoint = BukkitUtil.toVector((Vector) args.get("minimumPoint"));
			com.sk89q.worldedit.Vector maximumPoint = BukkitUtil.toVector((Vector) args.get("maximumPoint"));

			CuboidRegion region = new CuboidRegion(BukkitUtil.getLocalWorld(world), minimumPoint, maximumPoint);

			List<PotionEffect> effects = (List<PotionEffect>) args.get("effects");
			return new PotionRegion(region, effects);
		}
		return null;
	}
}
