package com.github.cheesesoftware.ZombieInvasionMinigame.PathfinderGoal;

import org.bukkit.Location;

import net.minecraft.server.v1_8_R1.EntityCreature;
import net.minecraft.server.v1_8_R1.PathfinderGoal;

public class PathfinderGoalWalkToTile extends PathfinderGoal
{
	float speed;
	private EntityCreature entityCreature;
	private Location goal;
	private int times = 10;
	private boolean done = false;

	public PathfinderGoalWalkToTile(EntityCreature entitycreature, float speed, Location location)
	{
		this.speed = speed;
		this.entityCreature = entitycreature;
		this.goal = location;
	}

	@Override
	public boolean a() //canExecute
	{
		if(done)
			return false;
		if (times > 0 && this.entityCreature.isAlive())
		{
			//this.entityCreature.getNavigation().a(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ(), speed);
			times--;
			return true;
		}
		return false;
	}

	@Override
	public boolean b() //canContinue
	{
		if(this.entityCreature.getBukkitEntity().getLocation().distance(this.goal) <= 15)
			done = true;
		return !done && !this.entityCreature.getNavigation().m();
	}

	@Override
	public void c() //setup
	{
		this.entityCreature.getNavigation().a(goal.getBlockX(), goal.getBlockY(), goal.getBlockZ(), speed);
	}

}