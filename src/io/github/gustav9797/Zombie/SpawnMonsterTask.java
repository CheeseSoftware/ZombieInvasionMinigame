package io.github.gustav9797.Zombie;

import io.github.gustav9797.State.PlayingState;
import io.github.gustav9797.ZombieInvasionMinigame.SpawnPoint;

import org.bukkit.scheduler.BukkitRunnable;

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