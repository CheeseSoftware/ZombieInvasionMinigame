package io.github.gustav9797.ZombieInvasionMinigame.PathfinderGoal;

import io.github.gustav9797.State.PlayingState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
	
	protected EntityInsentient entity;
	protected int x;
	protected int y;
	protected int z;
	boolean f;

	protected Block block;
	protected static List<Vector> possiblePositions = new ArrayList<Vector>(Arrays.asList(new Vector(-1, 0, 0), new Vector(-1, 1, 0), new Vector(1, 0, 0), new Vector(1, 1, 0), new Vector(0, 0, -1),
			new Vector(0, 1, -1), new Vector(0, 0, 1), new Vector(0, 1, 1), new Vector(0, 2, 0)));

	//
	@SuppressWarnings("deprecation")
	private static List<Material> nonBreakableMaterials = new ArrayList<Material>(Arrays.asList(
			Material.BEDROCK, Material.getMaterial(8), Material.getMaterial(9), Material.GRASS,
			Material.SAND, Material.AIR, Material.QUARTZ_BLOCK, Material.STONE));
	private static List<Material> naturalMaterials = new ArrayList<Material>(Arrays.asList(
			Material.GRASS, Material.DIRT, Material.LEAVES));
	
	private static List<Material> breakableMaterials = new ArrayList<Material>(Arrays.asList(
			Material.WOOD_DOOR, Material.IRON_DOOR, Material.TRAP_DOOR, Material.THIN_GLASS,
			Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.GLASS, Material.TORCH/*, Material.WOOL*/));

	private Location oldLocation = null;

	public PathfinderGoalBreakBlock(EntityInsentient entity, PlayingState playingState, boolean isStrongBreaker)
	{
		this.playingState = playingState;
		this.entity = entity;
		this.isStrongBreaker = isStrongBreaker;
	}
	
	public PathfinderGoalBreakBlock(EntityInsentient entity, PlayingState playingState)
	{
		this.playingState = playingState;
		this.entity = entity;
		this.isStrongBreaker = false;
	}
	
	private boolean canBreak(Material material)
	{
		if (isStrongBreaker)
		{
			return (!nonBreakableMaterials.contains(material) && !naturalMaterials.contains(material));
		}
		else
		{
			return (breakableMaterials.contains(material));
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
				if (r.nextInt(300) == 0)
				{
					this.entity.world.triggerEffect(1010, block.getX(), block.getY(), block.getZ(), 0);
				}

				this.i += 2;
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
					//if (breakableMaterials.contains(block.getType()))
					//	priorityBlocks.add(block);
				}
				if (priorityBlocks.size() > 0)
					return (Block) priorityBlocks.toArray()[r.nextInt(priorityBlocks.size())];
				//if (blocks.size() > 0)
					//return (Block) blocks.toArray()[r.nextInt(blocks.size())];
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
}
