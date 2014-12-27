package io.github.gustav9797.ZombieInvasionMinigame.PathfinderGoal;

import java.lang.reflect.Field;
import java.util.Random;

import io.github.gustav9797.State.PlayingState;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import net.minecraft.server.v1_8_R1.EntityCreature;
import net.minecraft.server.v1_8_R1.EntityLiving;

public class PathfinderGoalCustomNearestAttackableTarget extends PathfinderGoalCustomTarget {

    private final int attackCheckFrequency;
    private EntityLiving target;
    private PlayingState state;
    private Random r = new Random();

    public PathfinderGoalCustomNearestAttackableTarget(EntityCreature entitycreature, int attackCheckFrequency, PlayingState state) {
	super(entitycreature);
	this.attackCheckFrequency = attackCheckFrequency;
	this.state = state;
	this.a(1);
    }

    public boolean a() // canExecute
    {
	if (this.attackCheckFrequency > 0 && r.nextInt(this.attackCheckFrequency) != 0) {
	    return false;
	} else if (state != null
		&& (this.entity.getGoalTarget() == null || (this.entity.getGoalTarget().getBukkitEntity() instanceof Player && state
			.isSpectator((Player) this.entity.getGoalTarget().getBukkitEntity()))))
	// else if(this.entity.getGoalTarget() == null)
	{
	    // this.target = (EntityLiving) list.get(0);
	    // Player[] players = Bukkit.getServer().getOnlinePlayers();
	    Player closestPlayer = null;
	    double closestPlayerDistance = Double.MAX_VALUE;
	    for (Player player : state.getPlayers()) {
		if (player.getWorld().getName().equals(this.entity.getBukkitEntity().getWorld().getName())) {
		    double distance = player.getLocation().distance(this.entity.getBukkitEntity().getLocation());
		    if (distance < closestPlayerDistance && player.getGameMode() == GameMode.SURVIVAL && (state == null || !state.isSpectator(player))) {
			closestPlayerDistance = distance;
			closestPlayer = player;
		    }
		}
	    }

	    if (closestPlayer != null) {
		this.target = ((CraftPlayer) closestPlayer).getHandle();
		Field f;
		try {
		    f = ((EntityLiving)this.entity).getClass().getDeclaredField("goalTarget");
		    f.setAccessible(true);
		    f.set(this.entity, target);
		} catch (Exception e) {
		    e.printStackTrace();
		}

		// this.entity.setGoalTarget(target, TargetReason.CLOSEST_ENTITY, false);
		// this.entity.target = this.target;
		return true;
	    }
	}
	return false;
    }

    public void c() // setup
    {
	this.entity.setGoalTarget(target, TargetReason.CLOSEST_ENTITY, false);
	super.c();
    }
}