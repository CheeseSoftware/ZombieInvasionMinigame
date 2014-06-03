package io.github.gustav9797.State;

import io.github.gustav9797.ZombieInvasionMinigame.Arena;
import io.github.gustav9797.ZombieInvasionMinigame.ZombieInvasionMinigame;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public abstract class ArenaState implements IArenaState, Listener
{
	protected final Arena arena;
	
	public ArenaState(Arena arena)
	{
		this.arena = arena;
		Bukkit.getServer().getPluginManager().registerEvents(this, ZombieInvasionMinigame.getPlugin());
	}

	public abstract String getMotd();

}
