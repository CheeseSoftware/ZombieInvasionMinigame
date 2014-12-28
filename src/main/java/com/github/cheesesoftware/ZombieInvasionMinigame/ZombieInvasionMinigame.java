package com.github.cheesesoftware.ZombieInvasionMinigame;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.server.v1_8_R1.EntityCreature;
import net.minecraft.server.v1_8_R1.EntityZombie;
import net.minecraft.server.v1_8_R1.EntitySkeleton;
import net.minecraft.server.v1_8_R1.EntityVillager;
import net.minecraft.server.v1_8_R1.EntityWither;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.cheesesoftware.OstEconomyPlugin.IOstEconomy;
import com.github.cheesesoftware.OstEconomyPlugin.ItemStackShopItem;
import com.github.cheesesoftware.OstEconomyPlugin.OstEconomyPlugin;
import com.github.cheesesoftware.OstEconomyPlugin.ShopItem;
import com.github.cheesesoftware.State.IdleState;
import com.github.cheesesoftware.State.PlayingState;
import com.github.cheesesoftware.State.VotingState;
import com.github.cheesesoftware.Zombie.CustomEntityType;
import com.github.cheesesoftware.Zombie.ZombieArenaMap;
import com.github.cheesesoftware.ZombieInvasionMinigame.Entity.EntityBlockBreakingSkeleton;
import com.github.cheesesoftware.ZombieInvasionMinigame.Entity.EntityBlockBreakingVillager;
import com.github.cheesesoftware.ZombieInvasionMinigame.Entity.EntityBlockBreakingZombie;
import com.github.cheesesoftware.ZombieInvasionMinigame.Entity.EntityBossWither;
import com.github.cheesesoftware.ZombieInvasionMinigame.Entity.ICustomMonster;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

public final class ZombieInvasionMinigame extends JavaPlugin implements Listener {
    private LinkedList<CustomEntityType> entityTypes;
    private Arena arena;
    private File mapDirectory;
    private File configFile;

    private World lobbyWorld;
    private int votingTaskId = -1;

    public static IOstEconomy economyPlugin;

    @Override
    public void onEnable() {
	Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

	ConfigurationSerialization.registerClass(BorderBlock.class, "BorderBlock");
	ConfigurationSerialization.registerClass(PotionRegion.class, "PotionRegion");
	ConfigurationSerialization.registerClass(SpawnPoint.class, "SpawnPoint");

	this.mapDirectory = new File("../maps");
	if (!mapDirectory.exists())
	    mapDirectory.mkdir();

	this.entityTypes = new LinkedList<CustomEntityType>();
	this.entityTypes.add(new CustomEntityType("Zombie", 54, EntityType.ZOMBIE, EntityZombie.class, EntityBlockBreakingZombie.class));
	this.entityTypes.add(new CustomEntityType("Skeleton", 51, EntityType.SKELETON, EntitySkeleton.class, EntityBlockBreakingSkeleton.class));
	this.entityTypes.add(new CustomEntityType("Villager ", 120, EntityType.VILLAGER, EntityVillager.class, EntityBlockBreakingVillager.class));
	this.entityTypes.add(new CustomEntityType("Wither", 64, EntityType.WITHER, EntityWither.class, EntityBossWither.class));
	this.registerEntities();

	ZombieInvasionMinigame.economyPlugin = (IOstEconomy) Bukkit.getPluginManager().getPlugin("OstEconomyPlugin");
	if (ZombieInvasionMinigame.economyPlugin == null) {
	    this.getServer().getLogger().severe("Could not load economy!");
	} else {
	    // ////////////////////////////////////////////
	    // SHOP ITEMS //
	    // ////////////////////////////////////////////

	    ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ShopItem("Wood Sword", Material.WOOD_SWORD, 100, 0, false, 1));
	    ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ShopItem("Stone Sword", Material.STONE_SWORD, 250, 0, false, 1));
	    ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ShopItem("Gold Sword", Material.GOLD_SWORD, 500, 0, false, 1));
	    ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ShopItem("Iron Sword", Material.IRON_SWORD, 1000, 0, false, 1));
	    ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ShopItem("Diamond Sword", Material.DIAMOND_SWORD, 1500, 0, false, 1));

	    // Lucky Paper
	    {
		ItemStack bowItem = new ItemStack(Material.PAPER);
		bowItem.setAmount(10);
		bowItem.setDurability((short) 1);

		ItemMeta itemMeta = bowItem.getItemMeta();

		itemMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
		itemMeta.addEnchant(Enchantment.DAMAGE_ALL, 4, true);
		itemMeta.addEnchant(Enchantment.LOOT_BONUS_MOBS, 10, true);

		bowItem.setItemMeta(itemMeta);

		ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ItemStackShopItem("Lucky Paper", bowItem, 200, 0, false, 4));
	    }

	    // Bow
	    {
		ItemStack bowItem = new ItemStack(Material.BOW);
		ItemMeta itemMeta = bowItem.getItemMeta();

		itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 4, true);
		itemMeta.addEnchant(Enchantment.DURABILITY, 2, true);
		itemMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, 1, true);
		itemMeta.addEnchant(Enchantment.DIG_SPEED, 1, true);

		bowItem.setItemMeta(itemMeta);

		ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ItemStackShopItem("Bow", bowItem, 50, 0, false, 1));
	    }

	    // Fire Bow
	    {
		ItemStack fireBowItem = new ItemStack(Material.BOW);
		ItemMeta itemMeta = fireBowItem.getItemMeta();

		itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 6, true);
		itemMeta.addEnchant(Enchantment.DURABILITY, 4, true);
		itemMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, 3, true);
		itemMeta.addEnchant(Enchantment.ARROW_FIRE, 3, true);
		itemMeta.addEnchant(Enchantment.DIG_SPEED, 1, true);

		fireBowItem.setItemMeta(itemMeta);

		ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ItemStackShopItem("Fire Bow", fireBowItem, 200, 0, false, 1));
	    }

	    ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ShopItem("Stack of Arrows", Material.ARROW, 10, 0, false, 64));

	    ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ShopItem("Bone Key", Material.BONE, 500, 0, false, 1));
	    ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ShopItem("Disc Key", Material.RECORD_11, 1000, 0, false, 1));
	    ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ShopItem("Iron Key", Material.IRON_INGOT, 2000, 0, false, 1));
	    ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ShopItem("Gold Key", Material.GOLD_INGOT, 3000, 0, false, 1));
	    ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ShopItem("Emerald Key", Material.EMERALD, 4000, 0, false, 1));
	    ZombieInvasionMinigame.economyPlugin.RegisterShopItem(new ShopItem("Diamond Key", Material.DIAMOND, 5000, 0, false, 1));
	}

	this.lobbyWorld = this.getServer().getWorld("lobby");
	if (this.lobbyWorld == null)
	    this.getLogger().severe("Could not find world \"lobby\"!");

	this.configFile = new File(this.getDataFolder() + File.separator + "config.yml");
	if (!configFile.exists()) {
	    try {
		configFile.createNewFile();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    this.SaveConfig();
	}
	this.Load();
	this.arena = new Arena();
	getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
	this.SaveConfig();
	if (arena != null && arena.getState() instanceof PlayingState) {
	    PlayingState state = (PlayingState) arena.getState();
	    if (!state.isRestarting()) {
		state.Reset();
		state.LoadMap();
		for (Player player : state.getPlayers()) {
		    if (player.isOnline()) {
			player.removeMetadata("arena", this);
			ConnectPlayer(player, "S150");
		    }
		}
	    }
	}
    }

    public static int tryParse(String text) {
	try {
	    return new Integer(text);
	} catch (NumberFormatException e) {
	    return -1;
	}
    }

    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	if (sender instanceof Player) {
	    Player player = (Player) sender;
	    if (cmd.getName().equals("zombieinvasion")) {
		player.sendMessage("ZombieInvasion version: " + this.getDescription().getVersion());
		return true;
	    } else if (cmd.getName().equals("vote")) {
		int id;
		if (args.length > 0 && (id = tryParse(args[0])) != -1) {
		    if (arena.getState() instanceof VotingState) {
			((VotingState) arena.getState()).TryVote(player, id);
		    } else
			sender.sendMessage("It's too late to vote.");
		} else
		    sender.sendMessage("Usage: /vote <id>");
		return true;
	    }
	    /*
	     * else if (cmd.getName().equals("zombie")) { net.minecraft.server.v1_7_R4.World mcWorld = ((CraftWorld) this.arena.middle.getWorld()).getHandle(); EntityCreature monster = new
	     * EntityBlockBreakingZombie(mcWorld); ((EntityBlockBreakingZombie) monster).setPlayingState((PlayingState) arena.getState()); monster.getBukkitEntity().teleport(player.getLocation());
	     * mcWorld.addEntity(monster, SpawnReason.CUSTOM); return true; }
	     */
	    else if (cmd.getName().equals("createarena")) {
		if (args.length > 0) {
		    String name = args[0];
		    ZombieArenaMap map = new ZombieArenaMap(name);
		    arena = new Arena();
		    arena.Save();
		    map.Save();
		    this.Save();
		    sender.sendMessage("Arena " + name + " created!");
		} else
		    sender.sendMessage("Usage: /createarena <name>");
		return true;
	    } else if (cmd.getName().equals("editarena")) {
		if (arena.getState() instanceof PlayingState) {

		    PlayingState state = (PlayingState) arena.getState();
		    ZombieArenaMap map = state.getMap();

		    if (args.length > 0) {
			if (args[0].equals("maintenace")) {
			    state.StopAllActivity();
			    sender.sendMessage("Maintenace mode");
			} else if (args[0].equals("setspawn")) {
			    state.setSpawnLocation(player.getLocation());
			    sender.sendMessage("Arena spawn was set!");
			} else if (args[0].equals("setsize")) {
			    if (args.length > 0) {
				int size = Integer.parseInt(args[1]);
				if (size > 0 && size <= 128) {
				    map.size = size;
				    map.SaveConfig();
				    sender.sendMessage("Size was set to " + size);
				} else
				    sender.sendMessage("Size has to be between 0 and 128.");
			    } else
				sender.sendMessage("Usage: /setsize <size>");
			} else if (args[0].equals("createborder")) {
			    boolean roof = false;
			    int height = 100;
			    Material material = Material.GLASS;
			    if (args.length >= 2) {
				material = Material.getMaterial(args[1]);
				if (material != null) {
				    if (args.length >= 3)
					height = Integer.parseInt(args[2]);
				    if (args.length >= 4)
					roof = Boolean.parseBoolean(args[3]);
				    state.CreateBorder(material, height, roof);
				    sender.sendMessage("Border created.");
				} else
				    sender.sendMessage("Invalid material!");
			    } else
				sender.sendMessage("Usage: /createborder <string material> <int height> <bool buildroof=true>");
			} else if (args[0].equals("removeborder")) {
			    state.RestoreBorder();
			    sender.sendMessage("Border removed.");
			} else if (args[0].equals("reset")) {
			    state.LoadMap();
			    state.Reset();
			} else if (args[0].equals("savemap")) {
			    state.SaveMap();
			    sender.sendMessage("Map saved.");
			} else if (args[0].equals("loadmap")) {
			    state.LoadMap();
			    sender.sendMessage("Map loaded.");
			} else if (args[0].equals("clearmap")) {
			    state.ClearMap();
			    sender.sendMessage("Map cleared.");
			} else if (args[0].equals("clearpotionregions")) {
			    state.ClearPotionRegions();
			    sender.sendMessage("Potion regions cleared.");
			} else if (args[0].equals("addpotionregion")) {
			    if (args.length >= 4) {
				String typeString = args[1];
				typeString = typeString.toUpperCase();
				if (typeString.equals("NONE") || PotionEffectType.getByName(typeString) != null) {
				    int duration = Integer.parseInt(args[2]);
				    int amplifier = Integer.parseInt(args[3]);
				    LocalSession session = WorldEdit.getInstance().getSession(player.getName());
				    if (session != null) {
					try {
					    Region region = session.getSelection(BukkitUtil.getLocalWorld(player.getWorld()));
					    CuboidRegion newRegion = new CuboidRegion(region.getWorld(), region.getMinimumPoint(), region.getMaximumPoint());
					    PotionEffectType type = PotionEffectType.getByName(typeString);
					    if (typeString.equals("NONE")) {
						state.AddPotionRegion(new PotionRegion(newRegion, new ArrayList<PotionEffect>()));
						sender.sendMessage("Neutral potion region added.");
					    } else if (type != null) {
						state.AddPotionRegion(new PotionRegion(newRegion, new ArrayList<PotionEffect>(Arrays.asList(new PotionEffect(type, duration, amplifier)))));
						sender.sendMessage("Potion region added.");
					    } else
						sender.sendMessage("Potion effect type does not exist.");
					} catch (IncompleteRegionException e) {
					    sender.sendMessage("You have to select 2 corners.");
					}
				    } else
					sender.sendMessage("You have to select 2 corners.");
				} else
				    sender.sendMessage("Potion effect does not exist.");
			    } else
				sender.sendMessage("Usage: /addpotionregion <effect> <duration> <amplifier>");
			} else if (args[0].equals("expandpotionregion")) {
			    if (args.length >= 3) {
				String axis = args[1];
				axis = axis.toLowerCase();
				int amount = Integer.parseInt(args[2]);
				List<PotionRegion> regions = state.getPotionRegions();
				for (PotionRegion region : regions) {
				    if (region.getRegion().contains(BukkitUtil.toVector(player.getLocation()))) {
					com.sk89q.worldedit.Vector toUse = null;
					if (region.getRegion().getMaximumPoint().distance(BukkitUtil.toVector(player.getLocation())) < region.getRegion().getMinimumPoint()
						.distance(BukkitUtil.toVector(player.getLocation())))
					    toUse = region.getRegion().getMaximumPoint();
					else
					    toUse = region.getRegion().getMinimumPoint();
					if (toUse != null) {
					    if (axis.equals("x"))
						toUse.setX(toUse.getX() + amount);
					    else if (axis.equals("z"))
						toUse.setZ(toUse.getZ() + amount);
					    arena.Save();
					}
				    }
				}
			    } else
				sender.sendMessage("Usage: /expandpotionregion <axis> <amount>");
			} else if (args[0].equals("closestspawnpoint")) {
			    SpawnPointManager manager = state.getSpawnPointManager();
			    double closestDistance = Integer.MAX_VALUE;
			    SpawnPoint closestSpawnPoint = null;
			    for (SpawnPoint p : manager.getSpawnPoints()) {
				double distance = p.getPosition().distance(player.getLocation().toVector());
				if (distance < closestDistance) {
				    closestDistance = distance;
				    closestSpawnPoint = p;
				}
			    }
			    if (closestSpawnPoint != null) {
				String s = "";
				if (closestSpawnPoint.getEntityTypes().size() > 0)
				    for (EntityType e : closestSpawnPoint.getEntityTypes())
					s += e.toString() + ",";
				else
				    s = "No monsters";
				sender.sendMessage(closestSpawnPoint.getId() + ": " + s);
			    } else
				sender.sendMessage("Could not find any spawn points!");
			} else if (args[0].equals("whitelistentity")) {
			    if (args.length > 2) {
				int id = Integer.parseInt(args[1]);
				String monster = args[2];
				EntityType entityType = EntityType.fromName(monster);
				if (entityType != null) {
				    SpawnPointManager manager = state.getSpawnPointManager();
				    SpawnPoint spawnPoint = manager.getSpawnPoint(id);
				    if (spawnPoint != null) {
					spawnPoint.WhitelistEntity(entityType);
					sender.sendMessage("Whitelisted entitytype " + entityType.toString());
					manager.Save();
				    } else
					sender.sendMessage("Spawn point does not exist.");
				} else
				    sender.sendMessage("Entity type does not exist.");
			    } else
				sender.sendMessage("Usage: /editarena whitelistentity <id> <entityType> <chance>");
			} else if (args[0].equals("showspawnpoints")) {
			    SpawnPointManager manager = state.getSpawnPointManager();
			    manager.Show(player);
			    player.sendMessage("Spawn points shown.");
			} else if (args[0].equals("hidespawnpoints")) {
			    SpawnPointManager manager = state.getSpawnPointManager();
			    manager.Hide(player);
			    player.sendMessage("Spawn points hidden.");
			} else if (args[0].equals("removespawnpoint")) {
			    if (args.length > 1) {
				int id = Integer.parseInt(args[1]);
				SpawnPointManager manager = state.getSpawnPointManager();
				if (manager.HasSpawnPoint(id)) {
				    manager.RemoveSpawnPoint(id);
				    sender.sendMessage("Spawn point removed.");
				} else
				    sender.sendMessage("Spawn point does not exist.");
			    }
			} else if (args[0].equals("wither")) {
			    net.minecraft.server.v1_8_R1.World mcWorld = ((CraftWorld) player.getWorld()).getHandle();
			    EntityCreature monster = new EntityBossWither(mcWorld);
			    monster.getBukkitEntity().teleport(player.getLocation());
			    mcWorld.addEntity(monster, SpawnReason.CUSTOM);
			    sender.sendMessage("Wither spawned!");
			} else
			    sender.sendMessage("Command doesn't exist.");
		    } else
			sender.sendMessage("Usage: /editarena <something>");
		} else
		    sender.sendMessage("Game is in wrong state.");
	    } else if (cmd.getName().equals("reloadzombieinvasion")) {
		this.Reload();
		sender.sendMessage("Reloaded!");
		return true;
	    }
	}
	return false;
    }

    public void Save() {
	this.SaveConfig();
    }

    public void Load() {
	this.LoadConfig();
    }

    public void SaveConfig() {
	YamlConfiguration config = new YamlConfiguration();
	try {
	    config.save(configFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void LoadConfig() {
	YamlConfiguration config = new YamlConfiguration();
	try {
	    config.load(configFile);
	} catch (IOException | InvalidConfigurationException e) {
	    e.printStackTrace();
	}
    }

    public static void ConnectPlayer(Player player, String server) {
	ByteArrayDataOutput out = ByteStreams.newDataOutput();
	out.writeUTF("Connect");
	out.writeUTF(server);
	player.sendPluginMessage(ZombieInvasionMinigame.getPlugin(), "BungeeCord", out.toByteArray());
    }

    public void registerEntities() {
	/*
	 * BiomeBase[] biomes; try { biomes = (BiomeBase[]) getPrivateStatic(BiomeBase.class, "biomes"); } catch (Exception exc) { return; } for (BiomeBase biomeBase : biomes) { if (biomeBase == null)
	 * break; for (String field : new String[] { "as", "at", "au", "av" }) try { Field list = BiomeBase.class.getDeclaredField(field); list.setAccessible(true);
	 * 
	 * @SuppressWarnings("unchecked") List<BiomeMeta> mobList = (List<BiomeMeta>) list.get(biomeBase);
	 * 
	 * for (BiomeMeta meta : mobList) for (CustomEntityType entity : entityTypes) if (entity.getNMSClass().equals(meta.b)) meta.b = entity.getCustomClass(); } catch (Exception e) {
	 * e.printStackTrace(); } }
	 */
    }

    public void Reload() {
	this.Load();
	arena.Load();
    }

    public static ZombieInvasionMinigame getPlugin() {
	return (ZombieInvasionMinigame) Bukkit.getPluginManager().getPlugin("ZombieInvasionMinigame");
    }

    public static OstEconomyPlugin getEconomyPlugin() {
	return (OstEconomyPlugin) Bukkit.getPluginManager().getPlugin("OstEconomyPlugin");
    }

    public static JavaPlugin getWeaponsPlugin() {
	return (JavaPlugin) Bukkit.getPluginManager().getPlugin("WeaponsPlugin");
    }

    public boolean isVoting() {
	return this.votingTaskId != -1;
    }

    /*
     * @SuppressWarnings("rawtypes") private static Object getPrivateStatic(Class clazz, String f) throws Exception { Field field = clazz.getDeclaredField(f); field.setAccessible(true); return
     * field.get(null); }
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onServerListPing(ServerListPingEvent event) {
	event.setMotd(arena.getState().getMotd());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onCreatureSpawn(CreatureSpawnEvent event) {
	event.setCancelled(false);
	if (event.getSpawnReason() == SpawnReason.CUSTOM)
	    return;
	else if (event.getSpawnReason() == SpawnReason.BUILD_SNOWMAN)
	    return;

	if (event.getSpawnReason() == SpawnReason.SPAWNER_EGG) {
	    event.setCancelled(true);
	    EntityCreature monster = null;
	    net.minecraft.server.v1_8_R1.World mcWorld = ((CraftWorld) event.getLocation().getWorld()).getHandle();

	    switch (event.getEntityType()) {
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

	    if (monster != null) {
		monster.getBukkitEntity().teleport(event.getLocation());
		((ICustomMonster) monster).setPlayingState(null);
		mcWorld.addEntity(monster, SpawnReason.CUSTOM);
	    }
	} else {
	    if (arena.getState() instanceof PlayingState) {
		PlayingState state = (PlayingState) arena.getState();
		boolean monsterIsPartOfAnyArena = false;
		if (state.getMonsters().containsKey(event.getEntity().getUniqueId())) {
		    monsterIsPartOfAnyArena = true;
		    if (!state.ContainsLocation(event.getLocation()))
			event.setCancelled(true);
		}
		if (!monsterIsPartOfAnyArena)
		    event.setCancelled(true);
	    }
	}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerJoin(PlayerJoinEvent event) {
	if (arena.getState() instanceof IdleState) {
	    VotingState state = new VotingState(arena);
	    arena.setState(state);
	    state.onPlayerJoin(event);
	}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlockPlace(BlockPlaceEvent event) {
	if (event.getBlock().getType() == Material.SEA_LANTERN) {
	    Player player = event.getPlayer();
	    if (player.hasPermission("zombieinvasion.addmonsterspawnpoint")) {
		if (arena.getState() instanceof PlayingState) {
		    PlayingState state = (PlayingState) arena.getState();
		    SpawnPointManager manager = state.getSpawnPointManager();
		    int id = manager.getFreeSpawnPointId();
		    manager.AddSpawnPoint(id, new SpawnPoint(id, event.getBlockPlaced().getLocation().toVector()));
		    player.sendMessage("Monster spawnpoint ID " + id + " added!");
		    player.getWorld().getBlockAt(event.getBlock().getLocation()).setType(Material.AIR);
		}
	    } else
		player.sendMessage("You don't have permission to place zombie spawnpoints!");
	}
    }
}
