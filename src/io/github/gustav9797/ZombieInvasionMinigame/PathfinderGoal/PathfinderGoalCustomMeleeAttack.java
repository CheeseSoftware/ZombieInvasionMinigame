package io.github.gustav9797.ZombieInvasionMinigame.PathfinderGoal;

import java.util.Random;

import org.bukkit.Location;

import net.minecraft.server.v1_7_R3.DamageSource;
import net.minecraft.server.v1_7_R3.Entity;
import net.minecraft.server.v1_7_R3.EntityCreature;
import net.minecraft.server.v1_7_R3.EntityLiving;
import net.minecraft.server.v1_7_R3.MathHelper;
import net.minecraft.server.v1_7_R3.PathEntity;
import net.minecraft.server.v1_7_R3.PathfinderGoal;
import net.minecraft.server.v1_7_R3.World;

public class PathfinderGoalCustomMeleeAttack extends PathfinderGoal
{

	World a;
	EntityCreature entityCreature;
	int c;
	double d;
	boolean e;
	PathEntity pathEntity;
	@SuppressWarnings("rawtypes")
	Class g;
	private int h;
	private double locX;
	private double locY;
	private double locZ;
	// private int ticksPassed = 0;
	// private int ticksMin = 40;
	private Random r = new Random();

	private Location targetLocation = null;
	private Location targetOldLocation = null;

	@SuppressWarnings("rawtypes")
	public PathfinderGoalCustomMeleeAttack(EntityCreature entitycreature, Class oclass, double d0, boolean flag)
	{
		this(entitycreature, d0, flag);
		this.g = oclass;
	}

	public PathfinderGoalCustomMeleeAttack(EntityCreature entitycreature, double d0, boolean flag)
	{
		this.entityCreature = entitycreature;
		this.a = entitycreature.world;
		this.d = d0;
		this.e = flag;
		this.a(3);
	}

	private boolean hasTarget()
	{
		return this.entityCreature.getGoalTarget() != null && this.entityCreature.getGoalTarget().isAlive();
	}

	private boolean hasTargetMoved()
	{
		return this.targetLocation == null || this.targetOldLocation == null || this.targetLocation.getBlockX() != this.targetOldLocation.getBlockX() || this.targetLocation.getBlockY() != this.targetOldLocation.getBlockY()
				|| this.targetLocation.getBlockZ() != this.targetOldLocation.getBlockZ();
	}

	private boolean hasPathToWalk()
	{
		boolean bla = !this.entityCreature.getNavigation().g();
		return bla; // hasEnded() eller
					// något
	}

	@SuppressWarnings("unchecked")
	public boolean a()
	{
		if (!this.hasPathToWalk())
		{
			EntityLiving entityliving = this.entityCreature.getGoalTarget();

			if (entityliving == null)
			{
				return false;
			}
			else if (!entityliving.isAlive())
			{
				return false;
			}
			else if (this.g != null && !this.g.isAssignableFrom(entityliving.getClass()))
			{
				return false;
			}
			else
			{
				this.pathEntity = this.entityCreature.getNavigation().a(entityliving);
				return true;
			}
		}
		return false;
	}

	public boolean b() // canContinue
	{
		EntityLiving entityliving = this.entityCreature.getGoalTarget();

		return entityliving == null ? false : (!entityliving.isAlive() ? false : (!this.e ? !this.entityCreature.getNavigation().g() : this.entityCreature.b(MathHelper.floor(entityliving.locX),
				MathHelper.floor(entityliving.locY), MathHelper.floor(entityliving.locZ))));
	}

	public void c() // setup
	{
		this.entityCreature.getNavigation().a(this.pathEntity, this.d);
		this.h = 0;
	}

	public void d() // finish
	{
		this.entityCreature.getNavigation().h(); // .end(), .stop() eller något
	}

	public void e() // move
	{
		EntityLiving entityliving = this.entityCreature.getGoalTarget();
		this.targetOldLocation = this.targetLocation;
		this.targetLocation = entityliving.getBukkitEntity().getLocation();

		this.entityCreature.getControllerLook().a(entityliving, 30.0F, 30.0F);
		double d0 = this.entityCreature.e(entityliving.locX, entityliving.boundingBox.b, entityliving.locZ);
		double d1 = (double) (this.entityCreature.width * 2.0F * this.entityCreature.width * 2.0F + entityliving.width);

		--this.h;
		if ((this.e || this.entityCreature.getEntitySenses().canSee(entityliving)) && this.h <= 0
				&& (this.locX == 0.0D && this.locY == 0.0D && this.locZ == 0.0D || entityliving.e(this.locX, this.locY, this.locZ) >= 1.0D || r.nextFloat() < 0.05F))
		{
			this.locX = entityliving.locX;
			this.locY = entityliving.boundingBox.b;
			this.locZ = entityliving.locZ;
			this.h = 4 + r.nextInt(7);
			if (d0 > 1024.0D)
			{
				this.h += 10;
			}
			else if (d0 > 256.0D)
			{
				this.h += 5;
			}
			if (!this.hasTargetMoved())
				h += 15;

			if (this.hasTargetMoved() || (this.hasTarget() && !this.hasPathToWalk()))
			{
				if (!this.entityCreature.getNavigation().a((Entity) entityliving, this.d))
				{
					this.h += 15;
				}
			}
			/*
			 * if(this.entityCreature.getBukkitEntity().getWorld().getBlockAt(this
			 * .
			 * entityCreature.getBukkitEntity().getLocation()).getRelative(BlockFace
			 * .DOWN).getType() == Material.AIR) { this.h += 15; }
			 */
		}

		this.c = Math.max(this.c - 1, 0);
		if (d0 <= d1 && this.c <= 20)
		{
			this.c = 20;

			this.entityCreature.m(entityliving);
			this.entityCreature.getGoalTarget().damageEntity(DamageSource.a(this.entityCreature), (float) this.d);
		}
	}
}