package io.github.gustav9797.ZombieInvasion.Entity;

import io.github.gustav9797.ZombieInvasion.Arena;
import net.minecraft.server.v1_7_R3.EntityHuman;

public interface ICustomMonster
{
	public void setArena(Arena arena);

	public EntityHuman findNearbyVulnerablePlayer(double d0, double d1, double d2);
}
