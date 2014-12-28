package com.github.cheesesoftware.State;

import com.github.cheesesoftware.ZombieInvasionMinigame.Arena;

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
