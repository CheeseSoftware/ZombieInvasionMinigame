package io.github.gustav9797.ZombieInvasionMinigame.Entity;

import io.github.gustav9797.State.PlayingState;
import net.minecraft.server.v1_8_R1.EntityHuman;

public interface ICustomMonster
{
	public void setPlayingState(PlayingState state);

	public EntityHuman findNearbyVulnerablePlayer(double d0, double d1, double d2);
}
