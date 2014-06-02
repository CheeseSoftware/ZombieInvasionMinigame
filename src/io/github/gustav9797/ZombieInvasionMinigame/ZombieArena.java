package io.github.gustav9797.ZombieInvasionMinigame;

import io.github.gustav9797.ZombieInvasionMinigame.Entity.EntityBlockBreakingSkeleton;
import io.github.gustav9797.ZombieInvasionMinigame.Entity.EntityBlockBreakingVillager;
import io.github.gustav9797.ZombieInvasionMinigame.Entity.EntityBlockBreakingZombie;
import io.github.gustav9797.ZombieInvasionMinigame.Entity.ICustomMonster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.server.v1_7_R3.EntityCreature;
import net.minecraft.server.v1_7_R3.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftZombie;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class ZombieArena extends Arena
{
	protected Map<UUID, EntityCreature> monsters = new HashMap<UUID, EntityCreature>();
	protected List<SpawnPoint> monsterSpawnList = new ArrayList<SpawnPoint>();
	protected SpawnPointManager spawnPointManager;
	protected Random random = new Random();
	protected Material[][] armorTypes = new Material[5][4];
	
	protected int currentWave = 0;
	protected int ticksUntilNextWave = -1;
	protected int sendWavesTaskId = -1;
	protected int monsterSpawnLimit = 130;

	protected int zombieGroups = 10;
	protected int zombieStartAmount = 20;
	protected int zombieAmountIncrease = 10;
	protected int maxZombieAmount = 200;
	protected int startWave = 1;
	protected int waveIncrease = 1;
	protected int ticksBetweenZombieSpawns = 20;

	protected int zombiesToSpawn = 0;
	protected int skeletonsToSpawn = 0;
	protected int villagersToSpawn = 0;

	public ZombieArena(String name)
	{
		super(name);
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
	public void Load()
	{
		super.Load();
		this.spawnPointManager.Load();
	}

	@Override
	public void Save()
	{
		super.Save();
		this.spawnPointManager.Save();
	}

	public SpawnPointManager getSpawnPointManager()
	{
		return this.spawnPointManager;
	}

	public void SpawnMonster(SpawnPoint spawnPoint)
	{
		monsterSpawnList.add(spawnPoint);
	}

	public void SpawnMonsterGroup(SpawnPoint spawnPoint, int amount)
	{
		int delay = 1;
		Vector spawnPosition = spawnPoint.getPosition();

		for (int i = 0; i < amount; i++)
		{
			while (this.middle.getWorld().getBlockAt(spawnPoint.getPosition().toLocation(this.middle.getWorld())).getType() == Material.AIR)
				spawnPosition.setY(spawnPosition.getBlockY() - 1);

			spawnPosition.setY(spawnPosition.getBlockY() + 2);

			new SpawnMonsterTask(spawnPoint, this).runTaskLater(ZombieInvasionMinigame.getPlugin(), delay);

			delay += this.ticksBetweenZombieSpawns;
		}
	}

	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		CraftZombie zombie;
		
		net.minecraft.server.v1_7_R3.World mcWorld = ((CraftWorld) this.getMiddle().getWorld()).getHandle();
		EntityCreature monster = new EntityBlockBreakingZombie(mcWorld);
		((EntityBlockBreakingZombie)monster).setArena(this);
		
        ItemStack skull = new ItemStack(Material.SKULL_ITEM);
        SkullMeta meta3 = (SkullMeta) skull.getItemMeta();
        meta3.setOwner(player.getName());
        skull.setItemMeta(meta3);
		
        zombie = (CraftZombie)monster.getBukkitEntity();
        
        zombie.getEquipment().setHelmet(skull);
        zombie.setCustomName(player.getName());
        zombie.setCustomNameVisible(true);
        zombie.setCanPickupItems(true);
		
		monster.getBukkitEntity().teleport(player.getLocation());
		mcWorld.addEntity(monster, SpawnReason.CUSTOM);		
		
		super.onPlayerDeath(event);
	}
	
	@SuppressWarnings("deprecation")
	public void onSpawnZombieTick()
	{
		Iterator<SpawnPoint> i = monsterSpawnList.iterator();
		while (i.hasNext() && monsters.size() < this.monsterSpawnLimit)
		{
			SpawnPoint spawnPoint = i.next();
			net.minecraft.server.v1_7_R3.World mcWorld = ((CraftWorld) this.middle.getWorld()).getHandle();
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
								
								((CraftZombie)monster.getBukkitEntity()).getEquipment().setHelmet(new ItemStack(getArmorType(0)));
								((CraftZombie)monster.getBukkitEntity()).getEquipment().setChestplate(new ItemStack(getArmorType(1)));
								((CraftZombie)monster.getBukkitEntity()).getEquipment().setLeggings(new ItemStack(getArmorType(2)));
								((CraftZombie)monster.getBukkitEntity()).getEquipment().setBoots(new ItemStack(getArmorType(3)));
								
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
						((ICustomMonster) monster).setArena(this);
						double xd = r.nextDouble() / 10;
						double zd = r.nextDouble() / 10;

						Location l = new Location(this.middle.getWorld(), spawnPoint.getPosition().getBlockX() + xd, spawnPoint.getPosition().getBlockY(), spawnPoint.getPosition().getBlockZ() + zd);

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
		if (x > ((this.size / 2 * -1) + 5) && x < (this.size / 2) - 10)
		{
			if (z > ((this.size / 2 * -1) + 5) && z < (this.size / 2) - 10)
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
		//if (wave%10 == 0)
		//	return 50;
		
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
		int armorLevel = currentWave/2;
		
		armorLevel = (armorLevel > 7)? 7:armorLevel;
		
		int armorType2 = random.nextInt(7) - random.nextInt(3);
		
		if (armorType2 > 5 && armorType2 < armorLevel)
			armorType2 = 2*armorType2 - armorType2;
		
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

	@Override
	public void SendWave(int wave)
	{
		int spawnPointsSize = this.getSpawnPointManager().getSpawnPoints().size();

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

		for (int i = 0; i < zombieGroups; i++)
		{
			int groupAmount = amount / zombieGroups + ((i <= amount % zombieGroups) ? 1 : 0);

			if (spawnPointsSize > 0)
				SpawnMonsterGroup(this.getSpawnPointManager().getRandomMonsterSpawnPoint(), groupAmount);
			else
			{
				int x = Integer.MAX_VALUE;
				int z = Integer.MAX_VALUE;
				while (x == Integer.MAX_VALUE || !isValidZombieSpawningPosition(x, z))
				{
					x = r.nextInt(size - 4) - this.size / 2 + 2;
					z = r.nextInt(size - 4) - this.size / 2 + 2;
				}
				Location groupLocation = new Location(middle.getWorld(), x + middle.getBlockX(), r.nextInt(size) + middle.getBlockY(), z + middle.getBlockZ());
				SpawnPoint s = new SpawnPoint(-1, groupLocation.toVector());
				SpawnMonsterGroup(s, groupAmount);

			}
		}
	}

	@Override
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
		super.SendWaves();
		this.SendWave(this.startWave);
	}

	@Override
	public void Reset()
	{
		super.Reset();
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
	}

	@Override
	protected void LoadConfig()
	{
		super.LoadConfig();
		config = new YamlConfiguration();
		try
		{
			config.load(configFile);
			this.zombieGroups = config.getInt("zombieGroups");
			this.zombieStartAmount = config.getInt("zombieStartAmount");
			this.zombieAmountIncrease = config.getInt("zombieAmountIncrease");
			this.maxZombieAmount = config.getInt("maxZombieAmount");
			this.startWave = config.getInt("startWave");
			this.waveIncrease = config.getInt("waveIncrease");
			this.ticksBetweenZombieSpawns = config.getInt("ticksBetweenZombieSpawns");
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void SaveConfig()
	{
		super.SaveConfig();
		try
		{
			config.set("zombieGroups", this.zombieGroups);
			config.set("zombieStartAmount", this.zombieStartAmount);
			config.set("zombieAmountIncrease", this.zombieAmountIncrease);
			config.set("maxZombieAmount", this.maxZombieAmount);
			config.set("startWave", this.startWave);
			config.set("waveIncrease", this.waveIncrease);
			config.set("ticksBetweenZombieSpawns", this.ticksBetweenZombieSpawns);
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void Tick()
	{
		super.Tick();
		if (this.currentWave >= 200)
			this.Reset();

		if (this.ticksUntilNextWave != -1)
		{
			this.ticksUntilNextWave -= 1;
			if (this.ticksUntilNextWave <= 0)
			{
				this.ticksSinceLastWave = 0;
				this.currentWave += this.waveIncrease;
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

	@Override
	public void RemovePlayer(Player player, String reason)
	{
		super.RemovePlayer(player, reason);
		if (this.players.size() <= 0)
		{
			this.Reset();
		}
	}

	@Override
	public void MakeSpectator(Player player)
	{
		super.MakeSpectator(player);
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
}
