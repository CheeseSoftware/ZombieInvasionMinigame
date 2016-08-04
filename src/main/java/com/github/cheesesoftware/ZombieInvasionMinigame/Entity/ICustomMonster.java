package com.github.cheesesoftware.ZombieInvasionMinigame.Entity;

import com.github.cheesesoftware.State.PlayingState;

import net.minecraft.server.v1_10_R1.Entity;

public interface ICustomMonster
{
	public void setPlayingState(PlayingState state);

	public Entity findNearbyVulnerablePlayer(double d0, double d1, double d2);
}
