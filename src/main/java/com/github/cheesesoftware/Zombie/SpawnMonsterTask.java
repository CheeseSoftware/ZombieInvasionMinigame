package com.github.cheesesoftware.Zombie;

import org.bukkit.scheduler.BukkitRunnable;

import com.github.cheesesoftware.State.PlayingState;
import com.github.cheesesoftware.ZombieInvasionMinigame.SpawnPoint;

public class SpawnMonsterTask extends BukkitRunnable
{

	private final SpawnPoint spawnPoint;
	private final PlayingState playingState;

	public SpawnMonsterTask(SpawnPoint spawnPoint, PlayingState playingState)
	{
		this.spawnPoint = spawnPoint;
		this.playingState = playingState;
	}

	@Override
	public void run()
	{
		playingState.SpawnMonster(spawnPoint);
	}

}