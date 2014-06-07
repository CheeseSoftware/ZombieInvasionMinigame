package io.github.gustav9797.ZombieInvasionMinigame.PathfinderGoal;

import io.github.gustav9797.State.PlayingState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.server.v1_7_R3.EntityCreature;
import net.minecraft.server.v1_7_R3.EntityInsentient;
import net.minecraft.server.v1_7_R3.PathfinderGoal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.util.Vector;

public class PathfinderGoalBreakBlock extends PathfinderGoal
{
	float g;
	float h;
	Random r = new Random();
	int i;
	PlayingState playingState;
	int j = -1;
	boolean isStrongBreaker; // false: only breakable materials

	int breakEnergy = 0;
	int breakSpeed;

	protected EntityInsentient entity;
	protected int x;
	protected int y;
	protected int z;
	boolean f;

	protected Block block;
	protected static List<Vector> possiblePositions = new ArrayList<Vector>(Arrays.asList(new Vector(-1, 0, 0), new Vector(-1, 1, 0), new Vector(1, 0, 0), new Vector(1, 1, 0), new Vector(0, 0, -1),
			new Vector(0, 1, -1), new Vector(0, 0, 1), new Vector(0, 1, 1), new Vector(0, 2, 0)));

	protected static final Map<Material, Integer> hardnessList = new HashMap<Material, Integer>();
	static
	{
		hardnessList.put(Material.OBSIDIAN, 2 * 1000);
		// hardnessList.put(Material.ANVIL, 100);
		hardnessList.put(Material.COAL_BLOCK, 100);
		hardnessList.put(Material.DIAMOND_BLOCK, 2 * 100);
		hardnessList.put(Material.EMERALD_BLOCK, 2 * 100);
		hardnessList.put(Material.IRON_BLOCK, 2 * 100);
		hardnessList.put(Material.REDSTONE_BLOCK, 100);
		hardnessList.put(Material.IRON_FENCE, 2 * 100);
		hardnessList.put(Material.IRON_DOOR, 2 * 100);
		hardnessList.put(Material.WEB, 80);
		hardnessList.put(Material.DISPENSER, 2 * 70);
		hardnessList.put(Material.DROPPER, 2 * 70);
		hardnessList.put(Material.FURNACE, 2 * 20);
		hardnessList.put(Material.GOLD_BLOCK, 2 * 60);
		hardnessList.put(Material.COAL_ORE, 2 * 60);
		hardnessList.put(Material.DRAGON_EGG, 60);
		hardnessList.put(Material.DIAMOND_ORE, 2 * 60);
		hardnessList.put(Material.EMERALD_ORE, 2 * 60);
		hardnessList.put(Material.ENDER_STONE, 2 * 60);
		hardnessList.put(Material.GOLD_ORE, 2 * 60);
		hardnessList.put(Material.IRON_ORE, 2 * 60);
		hardnessList.put(Material.LAPIS_BLOCK, 2 * 60);
		hardnessList.put(Material.LAPIS_ORE, 2 * 60);
		hardnessList.put(Material.QUARTZ_ORE, 2 * 60);
		hardnessList.put(Material.REDSTONE_ORE, 2 * 60);
		hardnessList.put(Material.TRAP_DOOR, 60);
		hardnessList.put(Material.WOOD_DOOR, 60);
		// hardnessList.put(Material.BRICK_STAIRS, 40); ??
		hardnessList.put(Material.CLAY_BRICK, 2 * 40);
		hardnessList.put(Material.COBBLESTONE, 2 * 40);
		hardnessList.put(Material.COBBLESTONE_STAIRS, 2 * 40);
		hardnessList.put(Material.COBBLE_WALL, 2 * 40);
		hardnessList.put(Material.FENCE, 40);
		hardnessList.put(Material.FENCE_GATE, 40);
		hardnessList.put(Material.JUKEBOX, 40);
		hardnessList.put(Material.MOSSY_COBBLESTONE, 2 * 40);
		hardnessList.put(Material.NETHER_BRICK, 2 * 40);
		hardnessList.put(Material.NETHER_FENCE, 2 * 40);
		hardnessList.put(Material.NETHER_BRICK_STAIRS, 2 * 40);
		hardnessList.put(Material.STONE_PLATE, 2 * 40);
		hardnessList.put(Material.WOOD, 40);
		// hardnessList.put(Material., 40); WOOD_PLANKS?
		hardnessList.put(Material.WOOD_PLATE, 40);
		hardnessList.put(Material.WOOD_STAIRS, 40 * 2);
		hardnessList.put(Material.BOOKSHELF, 30);
		hardnessList.put(Material.STONE, 30 * 2);
		hardnessList.put(Material.BRICK, 30 * 2);
		hardnessList.put(Material.BRICK_STAIRS, 30 * 2);
		hardnessList.put(Material.STONE, 30 * 2);
		hardnessList.put(Material.HARD_CLAY, 25 * 2);
		hardnessList.put(Material.STAINED_CLAY, 25 * 2);
	}

	//
	@SuppressWarnings("deprecation")
	private static List<Material> nonBreakableMaterials = new ArrayList<Material>(Arrays.asList(Material.BEDROCK, Material.getMaterial(8), Material.getMaterial(9), Material.GRASS, Material.SAND,
			Material.AIR, Material.QUARTZ_BLOCK, Material.STONE));
	private static List<Material> naturalMaterials = new ArrayList<Material>(Arrays.asList(Material.GRASS, Material.DIRT, Material.LEAVES));

	private static List<Material> breakableMaterials = new ArrayList<Material>(Arrays.asList(Material.WOOD_DOOR, Material.IRON_DOOR, Material.TRAP_DOOR, Material.THIN_GLASS, Material.STAINED_GLASS,
			Material.STAINED_GLASS_PANE, Material.GLASS, Material.TORCH/*
																		 * ,
																		 * Material
																		 * .WOOL
																		 */));

	private Location oldLocation = null;

	public PathfinderGoalBreakBlock(EntityInsentient entity, PlayingState playingState, int breakSpeed, boolean isStrongBreaker)
	{
		this.playingState = playingState;
		this.entity = entity;
		this.breakSpeed = breakSpeed;
		this.isStrongBreaker = isStrongBreaker;
	}

	public PathfinderGoalBreakBlock(EntityInsentient entity, PlayingState playingState, int breakSpeed)
	{
		this.playingState = playingState;
		this.entity = entity;
		this.breakSpeed = breakSpeed;
		this.isStrongBreaker = false;
	}

	private boolean canBreak(Material material)
	{
		if (isStrongBreaker)
		{
			return material.isSolid() && (!nonBreakableMaterials.contains(material) && !naturalMaterials.contains(material));
		}
		else
		{
			return material.isSolid() && (breakableMaterials.contains(material));
		}

	}

	public boolean a() // canExecute
	{
		if (this.entity instanceof EntityCreature)
		{
			EntityCreature monster = (EntityCreature) this.entity;
			if (monster.getGoalTarget() == null || !monster.getGoalTarget().isAlive())
				return false;
			return true;
		}
		return false;
	}

	public void c() // setup
	{
		oldLocation = entity.getBukkitEntity().getLocation();
		this.f = false;
		this.g = (float) ((double) ((float) this.x + 0.5F) - this.entity.locX);
		this.h = (float) ((double) ((float) this.z + 0.5F) - this.entity.locZ);
		this.i = 0;
	}

	public boolean b() // canContinue
	{
		if (this.entity instanceof EntityCreature)
		{
			EntityCreature monster = (EntityCreature) this.entity;
			if (monster.getGoalTarget() == null || !monster.getGoalTarget().isAlive())
				return false;
			return true;
		}
		return false;
	}

	public void d() // finish
	{
		super.d();
	}

	public void e() // move
	{
		float f = (float) ((double) ((float) this.x + 0.5F) - this.entity.locX);
		float f1 = (float) ((double) ((float) this.z + 0.5F) - this.entity.locZ);
		float f2 = this.g * f + this.h * f1;
		if (f2 < 0.0F)
		{
			this.f = true;
		}
		Location currentLocation = this.entity.getBukkitEntity().getLocation();
		if (currentLocation.getBlockX() == oldLocation.getBlockX() && currentLocation.getBlockY() == oldLocation.getBlockY() && currentLocation.getBlockZ() == oldLocation.getBlockZ())
		{
			Entity entity = this.entity.getBukkitEntity();
			if (this.entity instanceof EntityCreature)
			{
				EntityCreature monster = (EntityCreature) this.entity;
				if (monster.getGoalTarget() == null || !monster.getGoalTarget().isAlive())
					return;
			}

			if (block == null || block.getType() == Material.AIR || getDistanceBetween(block.getLocation(), entity.getLocation()) > 3.5)
			{
				if (block != null)
					this.entity.world.d(this.entity.getId(), block.getX(), block.getY(), block.getZ(), 0);
				block = getRandomCloseBlock();
				if (block == null)
					return;
				this.i = 0;
			}

			if (block != null && block.getType() != Material.AIR)
			{
				int hardness = getBlockHardness(block.getType());

				if (r.nextInt(300) == 0)
				{
					this.entity.world.triggerEffect(1010, block.getX(), block.getY(), block.getZ(), 0);
				}

				breakEnergy += breakSpeed;

				if (breakEnergy >= hardness)
				{
					this.i += 2 * (breakEnergy / hardness);
					breakEnergy %= hardness;

					int i = (int) ((float) this.i / 240.0F * 10.0F);

					if (i != this.j)
					{
						this.entity.world.d(this.entity.getId(), block.getX(), block.getY(), block.getZ(), i);
						this.j = i;
					}

					if (this.i >= 240)
					{
						this.i = 0;
						Bukkit.getPluginManager().callEvent(new LeavesDecayEvent(block));
						block.setType(Material.AIR);
						this.entity.world.triggerEffect(1012, block.getX(), block.getY(), block.getZ(), 0);
						this.entity.world.triggerEffect(2001, block.getX(), block.getY(), block.getZ(),
								net.minecraft.server.v1_7_R3.Block.b(this.entity.world.getType(block.getX(), block.getY(), block.getZ())));
						block = null;
					}
				}
			}
		}
		oldLocation = currentLocation;
	}

	private Block getRandomCloseBlock()
	{
		if (this.entity instanceof EntityCreature)
		{
			EntityCreature zombie = (EntityCreature) this.entity;
			if (zombie.getGoalTarget() != null && zombie.getGoalTarget().isAlive())
			{
				Set<Block> blocks = this.getCloseBlocks();
				Location monsterLocation = zombie.getBukkitEntity().getLocation();
				Location targetLocation = zombie.getGoalTarget().getBukkitEntity().getLocation();
				if (targetLocation.getBlockY() > monsterLocation.getBlockY())
				{
					Location above = this.entity.getBukkitEntity().getLocation();
					above.setY(above.getY() + 1);
					blocks.add(above.getBlock());
				}

				// //Set<Block> innaturalBlocks = new HashSet<Block>();
				// //Set<Block> priorityBlocks = new HashSet<Block>();
				// //Set<Block> hatedBlocks = new HashSet<Block>();
				// for (Block block : blocks)
				// {
				// //if (!nonBreakableMaterials.contains(block.getType()) &&
				// (arena == null ||
				// arena.ContainsLocation(block.getLocation())))
				// {
				// Location l = block.getLocation();
				// l.setY(l.getY() + 1);
				// Block above =
				// this.entity.getBukkitEntity().getWorld().getBlockAt(l);
				// if (above == null ||
				// (!priorityMaterials.contains(block.getType()) &&
				// above.getType() == Material.AIR))
				// hatedBlocks.add(block);
				// else if (priorityBlocks.contains(block.getType()))
				// priorityBlocks.add(block);
				// else if (!naturalMaterials.contains(block.getType()))
				// innaturalBlocks.add(block);
				// }
				// }

				// if (priorityBlocks.size() > 0)
				// return (Block)
				// priorityBlocks.toArray()[r.nextInt(priorityBlocks.size())];
				// else if (innaturalBlocks.size() > 0)
				// return (Block)
				// innaturalBlocks.toArray()[r.nextInt(innaturalBlocks.size())];
				// else if (hatedBlocks.size() > 0 && blocks.size() <=
				// hatedBlocks.size())
				// {
				// int i = r.nextInt(400);
				// if (i == 0)
				// {
				// return (Block)
				// hatedBlocks.toArray()[r.nextInt(hatedBlocks.size())];
				// }
				// }
				// else
				Set<Block> priorityBlocks = new HashSet<Block>();
				for (Block block : blocks)
				{
					if (canBreak(block.getType()))
						priorityBlocks.add(block);
					// if (breakableMaterials.contains(block.getType()))
					// priorityBlocks.add(block);
				}
				if (priorityBlocks.size() > 0)
					return (Block) priorityBlocks.toArray()[r.nextInt(priorityBlocks.size())];
				// if (blocks.size() > 0)
				// return (Block) blocks.toArray()[r.nextInt(blocks.size())];
			}
		}
		return null;
	}

	private Set<Block> getCloseBlocks()
	{
		Set<Block> blocks = new HashSet<Block>();
		Location e = this.entity.getBukkitEntity().getLocation();
		Location a = new Location(e.getWorld(), e.getBlockX(), e.getBlockY(), e.getBlockZ());
		for (Vector vector : possiblePositions)
		{
			Location finalLocation = new Location(a.getWorld(), a.getBlockX() + vector.getBlockX(), a.getBlockY() + vector.getBlockY(), a.getBlockZ() + vector.getBlockZ());
			if (playingState == null || (playingState != null && !playingState.isBorder(finalLocation.toVector())))
				blocks.add(finalLocation.getBlock());
		}
		return (Set<Block>) blocks;
	}

	private double getDistanceBetween(Location a, Location b)
	{
		int xd = b.getBlockX() - a.getBlockX();
		int yd = b.getBlockY() - a.getBlockY();
		int zd = b.getBlockZ() - a.getBlockZ();
		return Math.sqrt(xd * xd + yd * yd + zd * zd);
	}

	private int getBlockHardness(Material material)
	{
		if (hardnessList.containsKey(material))
		{
			int hardness = hardnessList.get(material);
			if(hardness != 0)
				return hardness;
			else
				Bukkit.getLogger().warning("Block with zero hardness is not allowed. Fix this!");
		}
		return 20;
	}
}
