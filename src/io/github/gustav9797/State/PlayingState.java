package io.github.gustav9797.State;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.server.v1_7_R3.EntityCreature;
import net.minecraft.server.v1_7_R3.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftZombie;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import ostkaka34.OstEconomyPlugin.OstEconomyPlugin;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;

import io.github.gustav9797.Zombie.SpawnMonsterTask;
import io.github.gustav9797.Zombie.ZombieArenaMap;
import io.github.gustav9797.ZombieInvasionMinigame.Arena;
import io.github.gustav9797.ZombieInvasionMinigame.ArenaScoreboard;
import io.github.gustav9797.ZombieInvasionMinigame.BorderBlock;
import io.github.gustav9797.ZombieInvasionMinigame.PotionRegion;
import io.github.gustav9797.ZombieInvasionMinigame.SpawnPoint;
import io.github.gustav9797.ZombieInvasionMinigame.SpawnPointManager;
import io.github.gustav9797.ZombieInvasionMinigame.ZombieInvasionMinigame;
import io.github.gustav9797.ZombieInvasionMinigame.Entity.EntityBlockBreakingSkeleton;
import io.github.gustav9797.ZombieInvasionMinigame.Entity.EntityBlockBreakingVillager;
import io.github.gustav9797.ZombieInvasionMinigame.Entity.EntityBlockBreakingZombie;
import io.github.gustav9797.ZombieInvasionMinigame.Entity.ICustomMonster;

public class PlayingState extends ArenaState
{
	private List<Player> players;
	private ZombieArenaMap map;
	private Random r = new Random();

	private int sendWavesTaskId = -1;
	private int tickTaskId = -1;
	private int staticTickTaskId = -1;
	private int ticksPassed = -1;
	private int oldMinutesPassed = -1;
	private int ticksUntilNextWave = -1;
	private int ticksSinceLastWave = -1;

	private List<Player> spectators = new ArrayList<Player>();
	private ArenaScoreboard scoreboard;
	private ArrayList<BorderBlock> border;

	private Map<UUID, EntityCreature> monsters = new HashMap<UUID, EntityCreature>();
	private List<SpawnPoint> monsterSpawnList = new ArrayList<SpawnPoint>();
	private SpawnPointManager spawnPointManager;
	private Random random = new Random();
	private Material[][] armorTypes = new Material[5][4];

	private int currentWave = 0;

	private int zombiesToSpawn = 0;
	private int skeletonsToSpawn = 0;
	private int villagersToSpawn = 0;

	public PlayingState(Arena arena, List<Player> toTransfer, ZombieArenaMap map)
	{
		super(arena);
		this.players = toTransfer;
		this.map = map;

		spawnPointManager = new SpawnPointManager(this);

		armorTypes[0][0] = Material.LEATHER_HELMET;
		armorTypes[0][1] = Material.LEATHER_CHESTPLATE;
		armorTypes[0][2] = Material.LEATHER_LEGGINGS;
		armorTypes[0][3] = Material.LEATHER_BOOTS;

		armorTypes[1][0] = Material.GOLD_HELMET;
		armorTypes[1][1] = Material.GOLD_CHESTPLATE;
		armorTypes[1][2] = Material.GOLD_LEGGINGS;
		armorTypes[1][3] = Material.GOLD_BOOTS;

		armorTypes[2][0] = Material.CHAINMAIL_HELMET;
		armorTypes[2][1] = Material.CHAINMAIL_CHESTPLATE;
		armorTypes[2][2] = Material.CHAINMAIL_LEGGINGS;
		armorTypes[2][3] = Material.CHAINMAIL_BOOTS;

		armorTypes[3][0] = Material.IRON_HELMET;
		armorTypes[3][1] = Material.IRON_CHESTPLATE;
		armorTypes[3][2] = Material.IRON_LEGGINGS;
		armorTypes[3][3] = Material.IRON_BOOTS;

		armorTypes[4][0] = Material.DIAMOND_HELMET;
		armorTypes[4][1] = Material.DIAMOND_CHESTPLATE;
		armorTypes[4][2] = Material.DIAMOND_LEGGINGS;
		armorTypes[4][3] = Material.DIAMOND_BOOTS;
	}

	@Override
	public String getMotd()
	{
		return "In-Game";
	}

	public void SpawnMonster(SpawnPoint spawnPoint)
	{
		monsterSpawnList.add(spawnPoint);
	}

	public List<Player> getPlayers()
	{
		return this.players;
	}

	public Map<UUID, EntityCreature> getMonsters()
	{
		return this.monsters;
	}

	public ZombieArenaMap getMap()
	{
		return this.map;
	}

	public SpawnPointManager getSpawnPointManager()
	{
		return this.spawnPointManager;
	}

	public Location getSpawnLocation()
	{
		return new Location(arena.middle.getWorld(), arena.middle.getBlockX() + map.spawnLocation.getBlockX(), arena.middle.getBlockY() + map.spawnLocation.getBlockY(), arena.middle.getBlockZ()
				+ map.spawnLocation.getBlockZ(), arena.middle.getYaw(), arena.middle.getPitch());
	}

	public void setSpawnLocation(Location location)
	{
		map.spawnLocation = new Location(arena.middle.getWorld(), location.getBlockX() - arena.middle.getBlockX(), location.getBlockY() - arena.middle.getBlockY(), location.getBlockZ()
				- arena.middle.getBlockZ(), arena.middle.getYaw(), arena.middle.getPitch());
		map.SaveConfig();
	}

	protected void Broadcast(String message)
	{
		for (Player p : players)
		{
			p.sendMessage(ChatColor.WHITE + "[" + ChatColor.BLUE + "ZombieInvasion" + ChatColor.WHITE + "] " + message);
		}
	}

	public void StopAllActivity()
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		if (this.tickTaskId != -1)
			scheduler.cancelTask(tickTaskId);
		if (this.sendWavesTaskId != -1)
			scheduler.cancelTask(sendWavesTaskId);
		if (this.staticTickTaskId != -1)
			scheduler.cancelTask(staticTickTaskId);
	}

	public void SaveMap()
	{
		int topY = 256;
		/*
		 * if (!border.isEmpty()) { for (BorderBlock borderBlock : border) if
		 * (borderBlock.getLocation().getBlockY() > topY) topY =
		 * borderBlock.getLocation().getBlockY(); topY--; } else
		 */
		// topY = 256;
		int groundLevel = 4;
		EditSession session = new EditSession(new BukkitWorld(arena.middle.getWorld()), 999999999);
		File schematic = new File(ZombieInvasionMinigame.getPlugin().getDataFolder() + File.separator + "schematics" + File.separator + this.map.schematicFileName + ".schematic");
		CuboidClipboard clipboard = new CuboidClipboard(new com.sk89q.worldedit.Vector(this.getSize() - 1, topY, this.getSize() - 1), new com.sk89q.worldedit.Vector(arena.middle.getBlockX()
				- getRadius() + 1, groundLevel, arena.middle.getBlockZ() - getRadius() + 1));
		clipboard.copy(session);
		try
		{
			SchematicFormat.MCEDIT.save(clipboard, schematic);
		}
		catch (IOException | DataException e)
		{
			e.printStackTrace();
		}
	}

	public void LoadMap()
	{
		EditSession es = new EditSession(new BukkitWorld(arena.middle.getWorld()), 999999999);
		es.enableQueue();
		File schematic = new File(ZombieInvasionMinigame.getPlugin().getDataFolder() + File.separator + "schematics" + File.separator + map.schematicFileName + ".schematic");
		int groundLevel = 4;
		if (schematic.exists())
		{
			try
			{
				CuboidClipboard cc = SchematicFormat.MCEDIT.load(schematic);
				com.sk89q.worldedit.Vector location = new com.sk89q.worldedit.Vector(arena.middle.getBlockX() - getRadius() + 1, groundLevel, arena.middle.getBlockZ() - getRadius() + 1);
				cc.paste(es, location, false);
				es.flushQueue();
			}
			catch (MaxChangedBlocksException | DataException | IOException e)
			{
				e.printStackTrace();
			}
		}
		else
			Bukkit.getLogger().warning("[ZombieInvasion] Schematic file for arena " + map.name + " was not found! This will cause the arena to not get reset properly.");
	}

	public void ClearMap()
	{
		int topY = 0;
		if (!border.isEmpty())
		{
			for (BorderBlock borderBlock : border)
				if (borderBlock.getLocation().getBlockY() > topY)
					topY = borderBlock.getLocation().getBlockY();
			topY--;
		}
		else
			topY = 100;
		int groundLevel = 4;
		EditSession es = new EditSession(new BukkitWorld(arena.middle.getWorld()), 999999999);
		CuboidRegion region = new CuboidRegion(new com.sk89q.worldedit.Vector(arena.middle.getBlockX() - getRadius() + 1, groundLevel, arena.middle.getBlockZ() - getRadius() + 1),
				new com.sk89q.worldedit.Vector(arena.middle.getBlockX() + getRadius() - 1, topY, arena.middle.getBlockZ() + getRadius() - 1));
		try
		{
			es.setBlocks(region, new BaseBlock(0));
		}
		catch (MaxChangedBlocksException e)
		{
			e.printStackTrace();
		}
	}

	public void ResetSpectators()
	{
		List<Player> tempspectators = new ArrayList<Player>(this.spectators);
		for (Player player : tempspectators)
		{
			this.SetAlive(player);
			player.teleport(this.getSpawnLocation());
			player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 30, 2)); // 30s
																								// absorption
																								// 2
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 30, 2)); // 30s
																										// damage
																										// resistance
																										// 2
			player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 10, 1)); // 10s
																									// fire
																									// resistance
																									// 1
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 1)); // 10s
																							// speed
																							// 1
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 30, 2)); // 30s
																							// jump
																							// 2
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 10, 1)); // 10s
																									// invisibility
																									// 1
		}
		spectators.clear();
		tempspectators.clear();
	}

	private void CheckSpectators()
	{
		if (this.spectators.size() >= this.players.size())
		{
			Restart("Everyone have died. Reseting arena...");
		}
	}

	private void Restart(String message)
	{
		Broadcast(message);
		Reset();
		for (Player player : players)
			ZombieInvasionMinigame.ConnectPlayer(player, "S150");
		Bukkit.getServer().shutdown();
	}

	@SuppressWarnings("deprecation")
	public void MakeSpectator(Player player)
	{
		if (!spectators.contains(player))
			spectators.add(player);
		player.getInventory().clear();
		player.updateInventory();
		player.setGameMode(GameMode.ADVENTURE);
		player.setAllowFlight(true);
		player.setFlying(true);
		for (Player p : players)
		{
			p.hidePlayer(player);
		}
		player.sendMessage("[ZombieInvasion] You are now a spectator.");

		for (EntityCreature monster : monsters.values())
		{
			if (monster.target != null && monster.target instanceof EntityPlayer)
			{
				EntityPlayer target = (EntityPlayer) monster.target;
				if (this.spectators.contains(target.getBukkitEntity()))
				{
					List<Player> possiblePlayers = new ArrayList<Player>();
					for (Player poss : players)
					{
						if (!this.isSpectator(poss))
							possiblePlayers.add(poss);
					}
					if (possiblePlayers.size() != 0)
						monster.setTarget(((CraftPlayer) possiblePlayers.get(r.nextInt(possiblePlayers.size()))).getHandle());
				}
			}
		}
	}

	public void RemoveSpectator(Player player)
	{
		while (spectators.contains(player))
			spectators.remove(player);
		for (Player p : players)
			p.showPlayer(player);
		player.setGameMode(GameMode.SURVIVAL);
		player.setFlying(false);
		player.setAllowFlight(false);
		OstEconomyPlugin.getPlugin().ResetStats(player);
	}

	public boolean isSpectator(Player player)
	{
		return this.spectators.contains(player);
	}

	public void SetAlive(Player player)
	{
		this.RemoveSpectator(player);
		player.setHealth((double) 20);
		player.setFoodLevel(20);
		this.TeleportPlayerToRandomPlayer(player);
		player.sendMessage("[ZombieInvasion] You are now alive again!");

	}

	public void RespawnPlayers()
	{
		for (Player player : this.players)
		{
			if (!player.isDead())
			{
				player.teleport(this.getSpawnLocation());
				player.resetMaxHealth();
				Damageable p = player;
				player.setHealth(p.getMaxHealth());// player.setHealth((float)
													// player.getMaxHealth());
			}
		}
	}

	public void Reset()
	{
		if (this.tickTaskId != -1)
		{
			Bukkit.getServer().getScheduler().cancelTask(this.tickTaskId);
			this.tickTaskId = -1;
		}
		if (this.sendWavesTaskId != -1)
		{
			Bukkit.getServer().getScheduler().cancelTask(sendWavesTaskId);
			sendWavesTaskId = -1;
		}
		for (Player player : players)
		{
			if (!player.isDead())
			{
				player.teleport(this.getSpawnLocation());
				OstEconomyPlugin.getPlugin().ResetStats(player);
			}
		}
		this.ticksPassed = -1;
		this.ticksSinceLastWave = -1;
		this.oldMinutesPassed = -1;
		this.ResetSpectators();
		this.RespawnPlayers();

		List<Entity> entList = arena.middle.getWorld().getEntities();
		for (Entity entity : entList)
		{
			if (entity instanceof Item)
			{
				Item item = (Item) entity;
				if (this.ContainsLocation(item.getLocation()))
					item.remove();
			}
		}

		this.currentWave = 0;
		if (this.sendWavesTaskId != -1)
		{
			Bukkit.getServer().getScheduler().cancelTask(this.sendWavesTaskId);
			this.sendWavesTaskId = -1;
		}
		for (EntityCreature monster : monsters.values())
		{
			if (monster.isAlive())
				monster.die();
		}
		monsters.clear();

		this.Broadcast("Arena was reset!");
	}

	public boolean ContainsLocation(Location location)
	{
		if (location.getWorld().getName().equals(arena.middle.getWorld().getName()))
		{
			if (location.getBlockX() >= (-getRadius() + arena.middle.getBlockX()) && location.getBlockX() <= (getRadius() + arena.middle.getBlockX()))
			{
				if (location.getBlockZ() >= (-getRadius() + arena.middle.getBlockZ()) && location.getBlockZ() <= (getRadius() + arena.middle.getBlockZ()))
				{
					return true;
				}
			}
		}
		return false;
	}

	public void SendWaves()
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		this.sendWavesTaskId = scheduler.scheduleSyncRepeatingTask(ZombieInvasionMinigame.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				onSpawnZombieTick();
			}
		}, 0L, 5L);

		if (this.tickTaskId != -1)
			scheduler.cancelTask(tickTaskId);
		this.tickTaskId = scheduler.scheduleSyncRepeatingTask(ZombieInvasionMinigame.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				Tick();
			}
		}, 0L, 1L);

		this.SendWave(map.startWave);
	}

	protected void Tick()
	{
		this.ticksSinceLastWave += 1;
		ticksPassed += 1;
		int minutesPassed = Math.round(ticksPassed / 20 / 60);
		if (minutesPassed != oldMinutesPassed)
		{
			this.Broadcast(minutesPassed + " minutes have passed!");
			oldMinutesPassed = minutesPassed;
		}
		if (this.sendWavesTaskId != -1)
			this.ticksUntilNextWave += 1;
		else
			this.ticksUntilNextWave = 0;

		if (this.currentWave >= 200)
			this.Reset();

		if (this.ticksUntilNextWave != -1)
		{
			this.ticksUntilNextWave -= 1;
			if (this.ticksUntilNextWave <= 0)
			{
				this.ticksSinceLastWave = 0;
				this.currentWave += 1;
				this.ResetSpectators();
				this.ticksUntilNextWave = -1;
				this.SendWave(this.currentWave);
				this.Broadcast("Wave " + (int) (currentWave) + " is coming!");

			}
		}

		Iterator<EntityCreature> i = monsters.values().iterator();
		while (i.hasNext())
		{
			EntityCreature monster = i.next();
			if (!monster.isAlive())
			{
				i.remove();
			}
		}

		if (this.monsters.size() < 10 && this.ticksSinceLastWave >= 20 * 20 && this.ticksUntilNextWave == -1)
		{
			this.ResetSpectators();// >.<
			this.ticksUntilNextWave = 30 * 20;// >.<
			this.ResetSpectators();
			this.Broadcast("Below 10 zombies left, prepare for the next wave in 30 seconds!");
		}
	}

	protected void StaticTick()
	{
		List<PotionRegion> safeRegions = new ArrayList<PotionRegion>();
		for (PotionRegion safeRegion : map.potionRegions)
		{
			if (safeRegion != null && safeRegion.isNeutral())
				safeRegions.add(safeRegion);
		}

		for (PotionRegion potionRegion : map.potionRegions)
		{
			Iterator<Player> it = this.players.iterator();
			while (it.hasNext())
			{
				Player player = it.next();
				boolean isPlayerSafe = false;
				if (!player.isDead() && player.isOnline() && !spectators.contains(player) && player.getGameMode() == GameMode.SURVIVAL)
				{
					for (PotionRegion verySafeRegion : safeRegions)
					{
						if (verySafeRegion.getRegion().contains(BukkitUtil.toVector(player.getLocation())))
						{
							isPlayerSafe = true;
							break;
						}
					}

					if (!isPlayerSafe && potionRegion.getRegion().contains(BukkitUtil.toVector(player.getLocation())))
					{
						for (PotionEffect effect : potionRegion.getEffects())
						{
							player.addPotionEffect(effect, true);
						}
					}
				}
			}
		}
	}

	public void CreateBorder(Material material, int height, boolean buildRoof)
	{
		@SuppressWarnings("deprecation")
		List<Material> replacableMaterials = new ArrayList<Material>(Arrays.asList(Material.AIR, Material.WATER, Material.getMaterial(8), Material.getMaterial(9), Material.LAVA, material));
		if (!border.isEmpty())
			RestoreBorder();

		int radius = map.size / 2;
		BlockState originalBlock = null;
		World world = arena.middle.getWorld();

		for (int y = 0; y <= height; y++)
		{
			for (int x = -radius; x <= radius; x++)
			{
				for (int z = -radius; z <= radius; z++)
				{
					if (x == -radius || z == -radius || x == radius || z == radius)
					{
						originalBlock = world.getBlockAt(x + arena.middle.getBlockX(), y, z + arena.middle.getBlockZ()).getState();
						if (replacableMaterials.contains(originalBlock.getType()))
						{
							BorderBlock block = new BorderBlock(originalBlock.getLocation().toVector(), material, originalBlock.getType());
							while (this.border.contains(block))
								this.border.remove(block);
							this.border.add(block);
							world.getBlockAt(x + arena.middle.getBlockX(), y, z + arena.middle.getBlockZ()).setType(material);
						}
					}
				}
			}
		}

		if (buildRoof)
		{
			for (int x = -radius + 1; x < radius; x++)
			{
				for (int z = -radius + 1; z < radius; z++)
				{
					originalBlock = world.getBlockAt(x + arena.middle.getBlockX(), height, z + arena.middle.getBlockZ()).getState();
					this.border.add(new BorderBlock(new Vector(x + arena.middle.getBlockX(), height, z + arena.middle.getBlockZ()), material, originalBlock.getType()));
					world.getBlockAt(x + arena.middle.getBlockX(), height, z + arena.middle.getBlockZ()).setType(material);
				}
			}
		}
	}

	public void RestoreBorder()
	{
		for (BorderBlock block : border)
		{
			arena.middle.getWorld().getBlockAt(block.getLocation().toLocation(arena.middle.getWorld())).setType(block.getReplacedBlockType());
		}
		border.clear();
	}

	public void AddPotionRegion(PotionRegion potionRegion)
	{
		Region region = potionRegion.getRegion();
		for (PotionRegion equalRegion : map.potionRegions)
		{
			if (equalRegion.equals(region))
			{
				for (PotionEffect effect : potionRegion.getEffects())
				{
					equalRegion.AddEffect(effect);
				}
				map.SavePotionregionConfig();
				return;
			}
		}

		map.potionRegions.add(potionRegion);
		map.SavePotionregionConfig();
	}

	public void ClearPotionRegions()
	{
		map.potionRegions.clear();
		map.SavePotionregionConfig();
	}

	public List<PotionRegion> getPotionRegions()
	{
		return map.potionRegions;
	}

	public void setSize(int size)
	{
		map.size = size;
		map.SaveConfig();
	}

	public int getSize()
	{
		return map.size;
	}

	public int getRadius()
	{
		return map.size / 2;
	}

	public int getTicksUntilNextWave()
	{
		return this.ticksUntilNextWave;
	}

	public int getTotalGameTicks()
	{
		return this.ticksPassed == -1 ? 0 : this.ticksPassed;
	}

	public void setMiddle(Location middle)
	{
		arena.middle = middle;
		map.SaveConfig();
	}

	public Location getMiddle()
	{
		return arena.middle;
	}

	public boolean isBorder(Vector position)
	{
		return (position.getBlockX() == (arena.middle.getBlockX() + this.getRadius())) || (position.getBlockX() == (arena.middle.getBlockX() - this.getRadius()))
				|| (position.getBlockZ() == (arena.middle.getBlockZ() + this.getRadius())) || (position.getBlockZ() == (arena.middle.getBlockZ() - this.getRadius()));
	}

	public boolean isOnBorder(Vector position)
	{
		if (position.getBlockX() == arena.middle.getBlockX() - this.getRadius() + 1 || position.getBlockX() == arena.middle.getBlockX() + this.getRadius() - 1
				|| position.getBlockZ() == arena.middle.getBlockZ() - this.getRadius() + 1 || position.getBlockZ() == arena.middle.getBlockZ() + this.getRadius() - 1)
			return true;
		return false;
	}

	public boolean isRunning()
	{
		return this.ticksPassed != -1;
	}

	public boolean isStarting()
	{
		return this.sendWavesTaskId != -1;
	}

	public void Start()
	{
		if (!isRunning() && !isStarting())
		{
			this.Broadcast("Waves are coming in " + map.secondsWaitFirstTimeStart + " seconds!");
			if (this.sendWavesTaskId != -1)
				Bukkit.getServer().getScheduler().cancelTask(this.sendWavesTaskId);
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			this.sendWavesTaskId = scheduler.scheduleSyncDelayedTask(ZombieInvasionMinigame.getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					SendWaves();
					Bukkit.getScheduler().cancelTask(sendWavesTaskId);
					sendWavesTaskId = -1;
				}
			}, 20 * map.secondsWaitFirstTimeStart);
		}
	}

	public void TeleportPlayerToRandomPlayer(Player player)
	{
		List<Player> possiblePlayers = new ArrayList<Player>();
		for (Player poss : players)
			if (!isSpectator(poss) && poss != player)
				possiblePlayers.add(poss);
		if (possiblePlayers.size() > 0)
			player.teleport(possiblePlayers.get(r.nextInt(possiblePlayers.size())));
		else
			player.teleport(this.getSpawnLocation());
	}

	public void JoinPlayer(Player player)
	{
		while (players.contains(player))
			players.remove(player);
		ZombieInvasionMinigame.getEconomyPlugin().ResetStats(player);
		players.add(player);
		player.setGameMode(GameMode.SURVIVAL);
		player.setHealth((double) 20);
		player.setFoodLevel(20);
		scoreboard.AddPlayerScoreboard(player);
		this.TeleportPlayerToRandomPlayer(player);

		if (this.isRunning())
		{
			this.MakeSpectator(player);
			CheckSpectators();
		}
		else
			this.Broadcast(player.getName() + " has joined the arena!");
	}

	@SuppressWarnings("deprecation")
	public void RemovePlayer(Player player, String reason)
	{
		while (players.contains(player))
			players.remove(player);
		player.removeMetadata("arena", ZombieInvasionMinigame.getPlugin());
		scoreboard.RemovePlayerScoreboard(player);
		RemoveSpectator(player);
		CheckSpectators();
		if (players.size() <= 0)
		{
			this.Reset();
			this.LoadMap();
		}
		player.getInventory().clear();
		player.updateInventory();
		this.Broadcast(player.getName() + " has " + reason + "!");
		ZombieInvasionMinigame.ConnectPlayer(player, "S150");
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (players.contains(event.getPlayer()))
		{
			RemovePlayer(event.getPlayer(), "quit");
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		if (players.contains(player))
		{
			event.getDrops().clear();
			if (this.isRunning() && !this.isStarting())
			{
				if (this.spectators.size() >= this.players.size() - 1)
					Restart("Everyone have died. Restarting..");
				else
					this.MakeSpectator(player);
			}
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();
		if (players.contains(player))
		{
			if (!this.isRunning())
				this.SetAlive(player);
			event.setRespawnLocation(this.getSpawnLocation());
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (spectators.contains(player))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event)
	{
		if (event.getTarget() instanceof Player)
		{
			Player target = (Player) event.getTarget();
			if (this.isSpectator(target))
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (this.players.contains(event.getPlayer()))
		{
			if (this.isBorder(event.getBlock().getLocation().toVector()))
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage("Don't try to escape. You are ment to die with the monsters.");
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		if (this.players.contains(player))
		{
			if (isOnBorder(event.getBlockPlaced().getLocation().toVector()))
			{
				event.setCancelled(true);
				player.sendMessage("Don't try to build on the border. You will die in here.");
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		if (event.getCause() == DamageCause.ENTITY_ATTACK)
		{
			if (event.getDamager() instanceof Player)
			{
				Player player = (Player) event.getDamager();
				if (spectators.contains(player))
				{
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		if (spectators.contains(event.getPlayer()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		if (spectators.contains(event.getPlayer()))
			event.setCancelled(true);
	}

	public void SpawnMonsterGroup(SpawnPoint spawnPoint, int amount)
	{
		int delay = 1;
		Vector spawnPosition = spawnPoint.getPosition();

		for (int i = 0; i < amount; i++)
		{
			while (arena.middle.getWorld().getBlockAt(spawnPoint.getPosition().toLocation(arena.middle.getWorld())).getType() == Material.AIR)
				spawnPosition.setY(spawnPosition.getBlockY() - 1);

			spawnPosition.setY(spawnPosition.getBlockY() + 2);

			new SpawnMonsterTask(spawnPoint, this).runTaskLater(ZombieInvasionMinigame.getPlugin(), delay);

			delay += map.ticksBetweenZombieSpawns;
		}
	}

	@SuppressWarnings("deprecation")
	public void onSpawnZombieTick()
	{
		Iterator<SpawnPoint> i = monsterSpawnList.iterator();
		while (i.hasNext() && monsters.size() < map.maxZombieAmount)
		{
			SpawnPoint spawnPoint = i.next();
			net.minecraft.server.v1_7_R3.World mcWorld = ((CraftWorld) arena.middle.getWorld()).getHandle();
			EntityCreature monster = null;

			ArrayList<String> possibleEntityTypes = new ArrayList<String>();
			if (this.zombiesToSpawn > 0)
				possibleEntityTypes.add("ZOMBIE");
			if (this.skeletonsToSpawn > 0)
				possibleEntityTypes.add("SKELETON");
			if (this.villagersToSpawn > 0)
				possibleEntityTypes.add("VILLAGER");
			if (possibleEntityTypes.size() > 0)
			{
				EntityType entityType = EntityType.fromName(possibleEntityTypes.get(r.nextInt(possibleEntityTypes.size())));

				if (entityType != null)
				{
					switch (entityType)
					{
						case SKELETON:
							if (skeletonsToSpawn > 0)
							{
								monster = new EntityBlockBreakingSkeleton(mcWorld);
								skeletonsToSpawn--;
							}
							break;
						case ZOMBIE:
							if (this.getCurrentWave() < 10 && zombiesToSpawn > 0 || (villagersToSpawn == 0 && skeletonsToSpawn == 0))
							{
								EntityBlockBreakingZombie m = new EntityBlockBreakingZombie(mcWorld);
								monster = m;

								((CraftZombie) monster.getBukkitEntity()).getEquipment().setHelmet(new ItemStack(getArmorType(0)));
								((CraftZombie) monster.getBukkitEntity()).getEquipment().setChestplate(new ItemStack(getArmorType(1)));
								((CraftZombie) monster.getBukkitEntity()).getEquipment().setLeggings(new ItemStack(getArmorType(2)));
								((CraftZombie) monster.getBukkitEntity()).getEquipment().setBoots(new ItemStack(getArmorType(3)));

								zombiesToSpawn--;
							}
							else if (villagersToSpawn > 0)
							{
								monster = new EntityBlockBreakingVillager(mcWorld);
								villagersToSpawn--;
							}
							break;
						case VILLAGER:
							if (villagersToSpawn > 0)
							{
								monster = new EntityBlockBreakingVillager(mcWorld);
								villagersToSpawn--;
							}
							break;
						case SNOWMAN:

							break;
						default:
							break;
					}

					if (monster != null)
					{
						((ICustomMonster) monster).setArena(arena);
						double xd = r.nextDouble() / 10;
						double zd = r.nextDouble() / 10;

						Location l = new Location(arena.middle.getWorld(), spawnPoint.getPosition().getBlockX() + xd, spawnPoint.getPosition().getBlockY(), spawnPoint.getPosition().getBlockZ() + zd);

						monster.getBukkitEntity().teleport(l);
						monsters.put(monster.getBukkitEntity().getUniqueId(), monster);
						mcWorld.addEntity(monster, SpawnReason.CUSTOM);
					}
				}
			}
			i.remove();
			break;
		}
	}

	private boolean isValidZombieSpawningPosition(int x, int z)
	{
		if (x > ((map.size / 2 * -1) + 5) && x < (map.size / 2) - 10)
		{
			if (z > ((map.size / 2 * -1) + 5) && z < (map.size / 2) - 10)
				return false;
		}
		return true;
	}

	private int getZombieSpawnAmount(int wave)
	{
		// y = ax² + bx + c
		// y = (25x² + 205x + 330)/28
		// y = 25x²/28 + 205x/28 + 165x/14
		// Wave 1: 20
		// Wave >= 9: 150

		// Lower amount of zombie in boss fights.
		// if (wave%10 == 0)
		// return 50;

		if (wave >= 10)
			return 150;

		int x = wave % 10;
		float a = 25 / 28;
		float b = 205 / 28;
		float c = 165 / 14;

		return (int) Math.floor(a * x * x + b * x + c);
	}

	private Material getArmorType(int type)
	{
		int armorLevel = currentWave / 2;

		armorLevel = (armorLevel > 7) ? 7 : armorLevel;

		int armorType2 = random.nextInt(7) - random.nextInt(3);

		if (armorType2 > 5 && armorType2 < armorLevel)
			armorType2 = 2 * armorType2 - armorType2;

		if (armorType2 < 0)
			return Material.AIR;

		if (armorType2 > armorLevel)
			return Material.AIR;

		return armorTypes[armorType2][type];
	}

	public int getCurrentWave()
	{
		return this.currentWave;
	}

	public void SendWave(int wave)
	{
		int spawnPointsSize = this.spawnPointManager.getSpawnPoints().size();

		int amount = getZombieSpawnAmount(wave);

		zombiesToSpawn = 0;
		skeletonsToSpawn = 0;
		villagersToSpawn = 0;

		if (currentWave > 10)
		{
			skeletonsToSpawn = amount / 5;
			villagersToSpawn = amount / 2;
			zombiesToSpawn = amount - skeletonsToSpawn - villagersToSpawn;
		}
		else if (currentWave >= 5)
		{
			skeletonsToSpawn = amount / 40;
			villagersToSpawn = amount / 20;
			zombiesToSpawn = amount - skeletonsToSpawn - villagersToSpawn;
		}
		else
		{
			zombiesToSpawn = amount;
		}

		for (int i = 0; i < map.zombieGroups; i++)
		{
			int groupAmount = amount / map.zombieGroups + ((i <= amount % map.zombieGroups) ? 1 : 0);

			if (spawnPointsSize > 0)
				SpawnMonsterGroup(this.spawnPointManager.getRandomMonsterSpawnPoint(), groupAmount);
			else
			{
				int x = Integer.MAX_VALUE;
				int z = Integer.MAX_VALUE;
				while (x == Integer.MAX_VALUE || !isValidZombieSpawningPosition(x, z))
				{
					x = r.nextInt(map.size - 4) - map.size / 2 + 2;
					z = r.nextInt(map.size - 4) - map.size / 2 + 2;
				}
				Location groupLocation = new Location(arena.middle.getWorld(), x + arena.middle.getBlockX(), r.nextInt(map.size) + arena.middle.getBlockY(), z + arena.middle.getBlockZ());
				SpawnPoint s = new SpawnPoint(-1, groupLocation.toVector());
				SpawnMonsterGroup(s, groupAmount);

			}
		}
	}

}
