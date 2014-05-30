package io.github.gustav9797.ZombieInvasion;

import io.github.gustav9797.ZombieInvasion.Entity.EntityBlockBreakingSkeleton;
import io.github.gustav9797.ZombieInvasion.Entity.EntityBlockBreakingVillager;
import io.github.gustav9797.ZombieInvasion.Entity.EntityBlockBreakingZombie;
import io.github.gustav9797.ZombieInvasion.Entity.ICustomMonster;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.server.v1_7_R3.BiomeBase;
import net.minecraft.server.v1_7_R3.BiomeMeta;
import net.minecraft.server.v1_7_R3.EntityCreature;
import net.minecraft.server.v1_7_R3.EntityVillager;
import net.minecraft.server.v1_7_R3.EntityZombie;
import net.minecraft.server.v1_7_R3.EntitySkeleton;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import ostkaka34.OstEconomyPlugin.IOstEconomy;
import ostkaka34.OstEconomyPlugin.OstEconomyPlugin;

public final class ZombieInvasion extends JavaPlugin implements Listener
{
	LinkedList<CustomEntityType> entityTypes;
	Random r = new Random();
	Map<String, Arena> arenas;
	Lobby lobby;
	//File configFile;
	File schematicsDirectory;
	public static IOstEconomy economyPlugin;

	@Override
	public void onEnable()
	{
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		
		ConfigurationSerialization.registerClass(BorderBlock.class, "BorderBlock");
		ConfigurationSerialization.registerClass(PotionRegion.class, "PotionRegion");
		ConfigurationSerialization.registerClass(SpawnPoint.class, "SpawnPoint");

		//this.configFile = new File(this.getDataFolder() + File.separator + "config.yml");
		this.schematicsDirectory = new File(this.getDataFolder() + File.separator + "schematics");
		this.entityTypes = new LinkedList<CustomEntityType>();

		this.entityTypes.add(new CustomEntityType("Zombie", 54, EntityType.ZOMBIE, EntityZombie.class, EntityBlockBreakingZombie.class));
		this.entityTypes.add(new CustomEntityType("Skeleton", 51, EntityType.SKELETON, EntitySkeleton.class, EntityBlockBreakingSkeleton.class));
		this.entityTypes.add(new CustomEntityType("Villager ", 120, EntityType.VILLAGER, EntityVillager.class, EntityBlockBreakingVillager.class));

		this.registerEntities();
		this.arenas = new HashMap<String, Arena>();
		this.lobby = new Lobby(arenas, this);
		
		ZombieInvasion.economyPlugin = (IOstEconomy) Bukkit.getPluginManager().getPlugin("OstEconomyPlugin");
		if(ZombieInvasion.economyPlugin == null)
		{
			this.getServer().getLogger().severe("Could not load economy!");
		}

		if (!schematicsDirectory.exists())
			schematicsDirectory.mkdir();

		/*if (!configFile.exists())
		{
			try
			{
				configFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			this.SaveConfig();
		}*/
		this.Load();
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable()
	{
		this.saveDefaultConfig();
		this.saveConfig();
		for (Arena a : arenas.values())
		{
			a.Reset();
			a.LoadMap();
			for (Player player : a.players)
			{
				player.removeMetadata("arena", this);
				player.teleport(this.lobby.getLocation());
			}
		}
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if(cmd.getName().equals("listarenas"))
			{
				String output = "";
				for(Arena a : this.arenas.values())
					output += a.name + ", ";
				player.sendMessage("Arenas: " + output);
				return true;
			}
			else if (cmd.getName().equals("createarena"))
			{
				if (args.length > 0)
				{
					String name = args[0];
					if (!arenas.containsKey(name))
					{
						Arena a = new ZombieArena(name, this.lobby);
						a.setMiddle(player.getLocation());
						a.setSpawnLocation(player.getLocation());
						a.setSize(96);
						a.Save();
						arenas.put(name, a);
						this.Save();
						sender.sendMessage("Arena " + name + " created!");
					}
					else
						sender.sendMessage("Arena already exists.");
				}
				else
					sender.sendMessage("Usage: /createarena <name>");
				return true;
			}
			else if (cmd.getName().equals("removearena"))
			{
				if (player.hasMetadata("selectedarena"))
				{
					String name = player.getMetadata("selectedarena").get(0).asString();
					if (arenas.containsKey(name))
					{
						arenas.remove(name);
						File file = new File(this.getDataFolder() + File.separator + name + File.separator + "config.yml");
						file.delete();
						File file2 = new File(this.getDataFolder() + File.separator + name);
						file2.delete();
						this.Save();
						sender.sendMessage("Arena " + name + " removed!");
					}
					else
						sender.sendMessage("Arena doesn't exists.");
				}
				else
					sender.sendMessage("You don't have any arena selected.");
				return true;
			}
			else if (cmd.getName().equals("selectarena"))
			{
				if (args.length > 0)
				{
					String name = args[0];
					if (arenas.containsKey(name))
					{
						player.setMetadata("selectedarena", new FixedMetadataValue(this, name));
						sender.sendMessage("Arena selected.");
					}
					else
						sender.sendMessage("Arena doesn't exist.");
				}
				else
					sender.sendMessage("Usage: /selectarena <name>");
				return true;
			}
			else if (cmd.getName().equals("joinarena"))
			{
				if (args.length > 0)
				{
					if (!player.hasMetadata("arena"))
					{
						String name = args[0];
						if (arenas.containsKey(name))
						{
							Arena arena = arenas.get(name);
							arena.JoinPlayer(player);
						}
						else
							sender.sendMessage("Arena doesn't exist.");
					}
					else
						sender.sendMessage("You are already inside an arena! (/leave)");
				}
				else
					sender.sendMessage("Usage: /joinarena <name>");
				return true;
			}
			else if (cmd.getName().equals("leave"))
			{
				if (player.hasMetadata("arena"))
				{
					if (arenas.containsKey(player.getMetadata("arena").get(0).asString()))
					{
						Arena arena = arenas.get(player.getMetadata("arena").get(0).asString());
						arena.RemovePlayer(player, "left the arena");
					}
				}
				else
					sender.sendMessage("You haven't joined any arena!");
				return true;
			}
			else if (cmd.getName().equals("editarena"))
			{
				if (args.length > 0)
				{
					if (player.hasMetadata("selectedarena"))
					{
						String name = player.getMetadata("selectedarena").get(0).asString();
						if (arenas.containsKey(name))
						{
							Arena arena = arenas.get(name);
							if (args[0].equals("startwave"))
							{
								arena.SendWaves();
								this.getServer().broadcastMessage("Waves are coming! Hide!");
							}
							else if (args[0].equals("setlocation"))
							{
								arena.setMiddle(player.getLocation());
								sender.sendMessage("Arena middle was set!");
							}
							else if (args[0].equals("setarenaspawn"))
							{
								arena.setSpawnLocation(player.getLocation());
								sender.sendMessage("Arena spawn was set!");
							}
							else if (args[0].equals("setsize"))
							{
								if (args.length > 0)
								{
									int size = Integer.parseInt(args[1]);
									if (size > 0 && size <= 128)
									{
										arena.setSize(size);
										sender.sendMessage("Size was set to " + size);
									}
									else
										sender.sendMessage("Size has to be between 0 and 128.");
								}
								else
									sender.sendMessage("Usage: /setsize <size>");
							}
							else if (args[0].equals("createborder"))
							{
								boolean roof = false;
								int height = 100;
								Material material = Material.GLASS;
								if (args.length >= 2)
								{
									material = Material.getMaterial(args[1]);
									if (material != null)
									{
										if (args.length >= 3)
											height = Integer.parseInt(args[2]);
										if (args.length >= 4)
											roof = Boolean.parseBoolean(args[3]);
										arena.CreateBorder(material, height, roof);
										sender.sendMessage("Border created.");
									}
									else
										sender.sendMessage("Invalid material!");
								}
								else
									sender.sendMessage("Usage: /createborder <string material> <int height> <bool buildroof=true>");
							}
							else if (args[0].equals("removeborder"))
							{
								arena.RestoreBorder();
								sender.sendMessage("Border removed.");
							}
							else if (args[0].equals("reset"))
							{
								arena.LoadMap();
								arena.Reset();
							}
							else if (args[0].equals("savemap"))
							{
								arena.SaveMap();
								sender.sendMessage("Map saved.");
							}
							else if (args[0].equals("loadmap"))
							{
								arena.LoadMap();
								sender.sendMessage("Map loaded.");
							}
							else if (args[0].equals("clearmap"))
							{
								arena.ClearMap();
								sender.sendMessage("Map cleared.");
							}
							else if (args[0].equals("clearpotionregions"))
							{
								arena.ClearPotionRegions();
								sender.sendMessage("Potion regions cleared.");
							}
							else if (args[0].equals("addpotionregion"))
							{
								if (args.length >= 4)
								{
									String typeString = args[1];
									typeString = typeString.toUpperCase();
									if (typeString.equals("NONE") || PotionEffectType.getByName(typeString) != null)
									{
										int duration = Integer.parseInt(args[2]);
										int amplifier = Integer.parseInt(args[3]);
										LocalSession session = WorldEdit.getInstance().getSession(player.getName());
										if (session != null)
										{
											try
											{
												Region region = session.getSelection(BukkitUtil.getLocalWorld(arena.getMiddle().getWorld()));
												CuboidRegion newRegion = new CuboidRegion(region.getWorld(), region.getMinimumPoint(), region.getMaximumPoint());
												PotionEffectType type = PotionEffectType.getByName(typeString);
												if (typeString.equals("NONE"))
												{
													arena.AddPotionRegion(new PotionRegion(newRegion, new ArrayList<PotionEffect>()));
													sender.sendMessage("Neutral potion region added.");
												}
												else if (type != null)
												{
													arena.AddPotionRegion(new PotionRegion(newRegion, new ArrayList<PotionEffect>(Arrays.asList(new PotionEffect(type, duration, amplifier)))));
													sender.sendMessage("Potion region added.");
												}
												else
													sender.sendMessage("Potion effect type does not exist.");
											}
											catch (IncompleteRegionException e)
											{
												sender.sendMessage("You have to select 2 corners.");
											}
										}
										else
											sender.sendMessage("You have to select 2 corners.");
									}
									else
										sender.sendMessage("Potion effect does not exist.");
								}
								else
									sender.sendMessage("Usage: /addpotionregion <effect> <duration> <amplifier>");
							}
							else if (args[0].equals("expandpotionregion"))
							{
								if (args.length >= 3)
								{
									String axis = args[1];
									axis = axis.toLowerCase();
									int amount = Integer.parseInt(args[2]);
									List<PotionRegion> regions = arena.getPotionRegions();
									for (PotionRegion region : regions)
									{
										if (region.getRegion().contains(BukkitUtil.toVector(player.getLocation())))
										{
											com.sk89q.worldedit.Vector toUse = null;
											if (region.getRegion().getMaximumPoint().distance(BukkitUtil.toVector(player.getLocation())) < region.getRegion().getMinimumPoint()
													.distance(BukkitUtil.toVector(player.getLocation())))
												toUse = region.getRegion().getMaximumPoint();
											else
												toUse = region.getRegion().getMinimumPoint();
											if (toUse != null)
											{
												if (axis.equals("x"))
													toUse.setX(toUse.getX() + amount);
												else if (axis.equals("z"))
													toUse.setZ(toUse.getZ() + amount);
												arena.Save();
											}
										}
									}
								}
								else
									sender.sendMessage("Usage: /expandpotionregion <axis> <amount>");
							}
							else if (args[0].equals("closestspawnpoint"))
							{
								if (arena instanceof ZombieArena)
								{
									ZombieArena zombieArena = (ZombieArena) arena;
									SpawnPointManager manager = zombieArena.getSpawnPointManager();
									double closestDistance = Integer.MAX_VALUE;
									SpawnPoint closestSpawnPoint = null;
									for (SpawnPoint p : manager.getSpawnPoints())
									{
										double distance = p.getPosition().distance(player.getLocation().toVector());
										if (distance < closestDistance)
										{
											closestDistance = distance;
											closestSpawnPoint = p;
										}
									}
									if (closestSpawnPoint != null)
									{
										String s = "";
										if (closestSpawnPoint.getEntityTypes().size() > 0)
											for (EntityType e : closestSpawnPoint.getEntityTypes())
												s += e.toString() + ",";
										else
											s = "No monsters";
										sender.sendMessage(closestSpawnPoint.getId() + ": " + s);
									}
									else
										sender.sendMessage("Could not find any spawn points!");
								}
							}
							else if (args[0].equals("whitelistentity"))
							{
								if (args.length > 2)
								{
									if (arena instanceof ZombieArena)
									{
										int id = Integer.parseInt(args[1]);
										String monster = args[2];
										EntityType entityType = EntityType.fromName(monster);
										if (entityType != null)
										{
											ZombieArena zombieArena = (ZombieArena) arena;
											SpawnPointManager manager = zombieArena.getSpawnPointManager();
											SpawnPoint spawnPoint = manager.getSpawnPoint(id);
											if (spawnPoint != null)
											{
												spawnPoint.WhitelistEntity(entityType);
												sender.sendMessage("Whitelisted entitytype " + entityType.toString());
												manager.Save();
											}
											else
												sender.sendMessage("Spawn point does not exist.");
										}
										else
											sender.sendMessage("Entity type does not exist.");
									}
								}
								else
									sender.sendMessage("Usage: /editarena addspawnpointentity <id> <entityType> <chance>");
							}
							else if (args[0].equals("showspawnpoints"))
							{
								if (arena instanceof ZombieArena)
								{
									ZombieArena zombieArena = (ZombieArena) arena;
									SpawnPointManager manager = zombieArena.getSpawnPointManager();
									manager.Show(player);
									player.sendMessage("Spawn points shown.");
								}
							}
							else if (args[0].equals("hidespawnpoints"))
							{
								if (arena instanceof ZombieArena)
								{
									ZombieArena zombieArena = (ZombieArena) arena;
									SpawnPointManager manager = zombieArena.getSpawnPointManager();
									manager.Hide(player);
									player.sendMessage("Spawn points hidden.");
								}
							}
							else if (args[0].equals("removespawnpoint"))
							{
								if (arena instanceof ZombieArena)
								{
									if (args.length > 1)
									{
										ZombieArena zombieArena = (ZombieArena) arena;
										int id = Integer.parseInt(args[1]);
										SpawnPointManager manager = zombieArena.getSpawnPointManager();
										if (manager.HasSpawnPoint(id))
										{
											manager.RemoveSpawnPoint(id);
											sender.sendMessage("Spawn point removed.");
										}
										else
											sender.sendMessage("Spawn point does not exist.");
									}
								}
							}
							else
								sender.sendMessage("Command doesn't exist.");
						}
						else
							sender.sendMessage("Arena doesn't exist.");
					}
					else
						sender.sendMessage("You don't have any arena selected.");
				}
				else
					sender.sendMessage("Usage: /editarena <something>");
			}
			else if (cmd.getName().equals("setlobby"))
			{
				lobby.setLocation(player.getLocation());
				sender.sendMessage("Lobby set!");
				return true;
			}
			else if (cmd.getName().equals("reloadzombieinvasion"))
			{
				this.Reload();
				sender.sendMessage("Reloaded!");
				return true;
			}
		}
		return false;
	}

	public void Save()
	{
		this.SaveConfig();
	}

	public void Load()
	{
		this.LoadConfig();
	}

	public void SaveConfig()
	{
		/*YamlConfiguration config = new YamlConfiguration();
		List<String> temp = new LinkedList<String>();
		for (Arena a : arenas.values())
			temp.add(a.name);
		config.set("zombiearenas", temp);
		try
		{
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}*/
	}

	public void LoadConfig()
	{
		/*YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(configFile);
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
		
		@SuppressWarnings("unchecked")
		List<String> temp = (List<String>) config.getList("zombiearenas");
		for (String arena : temp)
		{
			ZombieArena a = new ZombieArena(arena, lobby);
			a.Load();
			arenas.put(arena, a);
		}*/
		
		File file = new File("./plugins/ZombieInvasion");
		String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory() && !name.equals("schematics");
		  }
		});
		
		for(String directory : directories)
		{
			File arenafile = new File("./plugins/ZombieInvasion/" + directory + "/config.yml");
			if(arenafile.exists())
			{
				ZombieArena a = new ZombieArena(directory, lobby);
				a.Load();
				arenas.put(directory, a);
			}
		}
	}

	public void registerEntities()
	{
		BiomeBase[] biomes;
		try
		{
			biomes = (BiomeBase[]) getPrivateStatic(BiomeBase.class, "biomes");
		}
		catch (Exception exc)
		{
			return;
		}
		for (BiomeBase biomeBase : biomes)
		{
			if (biomeBase == null)
				break;
			for (String field : new String[]
			{ "as", "at", "au", "av" })
				try
				{
					Field list = BiomeBase.class.getDeclaredField(field);
					list.setAccessible(true);
					@SuppressWarnings("unchecked")
					List<BiomeMeta> mobList = (List<BiomeMeta>) list.get(biomeBase);

					for (BiomeMeta meta : mobList)
						for (CustomEntityType entity : entityTypes)
							if (entity.getNMSClass().equals(meta.b))
								meta.b = entity.getCustomClass();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
		}
	}

	public void Reload()
	{
		this.Load();
		this.lobby.Load();
		for (Arena a : arenas.values())
			a.Load();
	}

	public static ZombieInvasion getPlugin()
	{
		return (ZombieInvasion) Bukkit.getPluginManager().getPlugin("ZombieInvasion");
	}

	public static OstEconomyPlugin getEconomyPlugin()
	{
		return (OstEconomyPlugin) Bukkit.getPluginManager().getPlugin("OstEconomyPlugin");
	}

	public static JavaPlugin getWeaponsPlugin()
	{
		return (JavaPlugin) Bukkit.getPluginManager().getPlugin("WeaponsPlugin");
	}

	@SuppressWarnings("rawtypes")
	private static Object getPrivateStatic(Class clazz, String f) throws Exception
	{
		Field field = clazz.getDeclaredField(f);
		field.setAccessible(true);
		return field.get(null);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onCreatureSpawn(CreatureSpawnEvent event)
	{
		//Bukkit.getServer().getPlayer("gustav9797").sendMessage("spawnreason " + event.getSpawnReason().toString() + " type " + event.getEntityType().toString());
		if(event.getSpawnReason() == SpawnReason.CUSTOM)
			return;
		else if (event.getSpawnReason() == SpawnReason.BUILD_SNOWMAN)
			return;
		
		if (event.getSpawnReason() == SpawnReason.SPAWNER_EGG)
		{
			event.setCancelled(true);
			EntityCreature monster = null;
			net.minecraft.server.v1_7_R3.World mcWorld = ((CraftWorld) event.getLocation().getWorld()).getHandle();

			switch (event.getEntityType())
			{
				case SKELETON:
					monster = new EntityBlockBreakingSkeleton(mcWorld);
					break;
				case ZOMBIE:
					monster = new EntityBlockBreakingZombie(mcWorld);
					break;
				case VILLAGER:
					monster = new EntityBlockBreakingVillager(mcWorld);
					break;
				default:
					break;
			}

			if (monster != null)
			{
				monster.getBukkitEntity().teleport(event.getLocation());
				((ICustomMonster)monster).setArena(null);
				mcWorld.addEntity(monster, SpawnReason.CUSTOM);
			}
		}
		else
		{
			boolean monsterIsPartOfAnyArena = false;
			for (Arena a : this.arenas.values())
			{
				if (a instanceof ZombieArena)
				{
					ZombieArena arena = (ZombieArena) a;
					if (arena.monsters.containsKey(event.getEntity().getUniqueId()))
					{
						monsterIsPartOfAnyArena = true;
						if (!a.ContainsLocation(event.getLocation()))
							event.setCancelled(true);
						break;
					}
				}
			}
			if (!monsterIsPartOfAnyArena)
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerQuit(PlayerQuitEvent event)
	{
		lobby.onPlayerQuit(event);
		for (Arena a : arenas.values())
			a.onPlayerQuit(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerDeath(PlayerDeathEvent event)
	{
		for (Arena a : arenas.values())
			a.onPlayerDeath(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerRespawn(PlayerRespawnEvent event)
	{
		for (Arena a : arenas.values())
			a.onPlayerRespawn(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerInteract(PlayerInteractEvent event)
	{
		lobby.onPlayerInteract(event);
		for (Arena a : arenas.values())
			a.onPlayerInteract(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event)
	{
		for (Arena a : arenas.values())
			a.onEntityTargetLivingEntity(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onBlockBreak(BlockBreakEvent event)
	{
		lobby.onBlockBreak(event);
		for (Arena a : arenas.values())
			a.onBlockBreak(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onBlockPlace(BlockPlaceEvent event)
	{
		for (Arena a : arenas.values())
		{
			a.onBlockPlace(event);
			if (event.getBlock().getType() == Material.SPONGE)
			{
				Player player = event.getPlayer();
				if (player.hasPermission("zombieinvasion.addmonsterspawnpoint"))
				{
					if (a.ContainsLocation(event.getBlockPlaced().getLocation()))
					{
						if (a instanceof ZombieArena)
						{
							SpawnPointManager manager = ((ZombieArena) a).getSpawnPointManager();
							int id = manager.getFreeSpawnPointId();
							manager.AddSpawnPoint(id, new SpawnPoint(id, event.getBlockPlaced().getLocation().toVector()));
							player.sendMessage("Monster spawnpoint ID " + id + " added!");
							a.getMiddle().getWorld().getBlockAt(event.getBlock().getLocation()).setType(Material.AIR);
						}
					}
				}
				else
					player.sendMessage("You don't have permission to place zombie spawnpoints!");
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onSignChange(SignChangeEvent event)
	{
		lobby.onSignChange(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerJoin(PlayerJoinEvent event)
	{
		lobby.onPlayerJoin(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		for (Arena a : arenas.values())
			a.onEntityDamageByEntity(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerDropItem(PlayerDropItemEvent event)
	{
		for (Arena a : arenas.values())
			a.onPlayerDropItem(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		for (Arena a : arenas.values())
			a.onPlayerPickupItem(event);
	}
}
