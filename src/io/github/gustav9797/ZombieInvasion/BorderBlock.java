package io.github.gustav9797.ZombieInvasion;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;

@SerializableAs("BorderBlock")
public class BorderBlock implements Cloneable, ConfigurationSerializable
{
	Vector location;
	Material block;
	Material blockReplaced;

	public BorderBlock(Vector location, Material block, Material blockReplaced)
	{
		this.location = location;
		this.block = block;
		this.blockReplaced = blockReplaced;
	}

	public Vector getLocation()
	{
		return this.location;
	}

	public Material getBlockType()
	{
		return this.block;
	}

	public Material getReplacedBlockType()
	{
		return this.blockReplaced;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("x", this.location.getBlockX());
		result.put("y", this.location.getBlockY());
		result.put("z", this.location.getBlockZ());
		result.put("blockId", this.block.getId());
		result.put("blockIdReplaced", this.blockReplaced.getId());
		return result;
	}

	@SuppressWarnings("deprecation")
	public static BorderBlock deserialize(Map<String, Object> args)
	{
		int x = 0;
		int y = 0;
		int z = 0;
		int blockId = 0;
		int blockIdReplaced = 0;

		if (args.containsKey("x"))
			x = (int)args.get("x");
		if (args.containsKey("y"))
			y = (int)args.get("y");
		if (args.containsKey("z"))
			z = (int)args.get("z");
		if(args.containsKey("blockId"))
			blockId = (int)args.get("blockId");
		if(args.containsKey("blockIdReplaced"))
			blockIdReplaced = (int)args.get("blockIdReplaced");
		return new BorderBlock(new Vector(x, y, z), Material.getMaterial(blockId), Material.getMaterial(blockIdReplaced));
	}

}
