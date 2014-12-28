package com.github.cheesesoftware.State;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import com.github.cheesesoftware.ZombieInvasionMinigame.Arena;
import com.github.cheesesoftware.ZombieInvasionMinigame.ZombieInvasionMinigame;

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
