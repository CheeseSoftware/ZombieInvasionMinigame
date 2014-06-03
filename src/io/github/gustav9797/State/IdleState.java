package io.github.gustav9797.State;

import io.github.gustav9797.ZombieInvasionMinigame.Arena;

public class IdleState extends ArenaState
{

	public IdleState(Arena arena)
	{
		super(arena);
	}

	@Override
	public String getMotd()
	{
		return "Waiting";
	}

}
