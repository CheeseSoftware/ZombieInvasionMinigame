package io.github.gustav9797.ZombieInvasionMinigame.PathfinderGoal;

import java.util.Random;

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
	private double i;
	private double j;
	private double k;
	private int ticksPassed = 0;
	private Random r = new Random();

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

	@SuppressWarnings("unchecked")
	public boolean a()
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
			this.ticksPassed++;
			if (this.ticksPassed >= 20)
			{
				this.pathEntity = this.entityCreature.getNavigation().a(entityliving);
				this.ticksPassed = 0;
				return this.pathEntity != null;
			}
			return false;
		}
	}

	public boolean b()
	{
		EntityLiving entityliving = this.entityCreature.getGoalTarget();

		return entityliving == null ? false : (!entityliving.isAlive() ? false : (!this.e ? !this.entityCreature.getNavigation().g() : this.entityCreature.b(MathHelper.floor(entityliving.locX),
				MathHelper.floor(entityliving.locY), MathHelper.floor(entityliving.locZ))));
	}

	public void c()
	{
		this.entityCreature.getNavigation().a(this.pathEntity, this.d);
		this.h = 0;
	}

	public void d()
	{
		this.entityCreature.getNavigation().h();
	}

	public void e()
	{
		EntityLiving entityliving = this.entityCreature.getGoalTarget();

		this.entityCreature.getControllerLook().a(entityliving, 30.0F, 30.0F);
		double d0 = this.entityCreature.e(entityliving.locX, entityliving.boundingBox.b, entityliving.locZ);
		double d1 = (double) (this.entityCreature.width * 2.0F * this.entityCreature.width * 2.0F + entityliving.width);

		--this.h;
		if ((this.e || this.entityCreature.getEntitySenses().canSee(entityliving)) && this.h <= 0
				&& (this.i == 0.0D && this.j == 0.0D && this.k == 0.0D || entityliving.e(this.i, this.j, this.k) >= 1.0D || r.nextFloat() < 0.05F))
		{
			this.i = entityliving.locX;
			this.j = entityliving.boundingBox.b;
			this.k = entityliving.locZ;
			this.h = 4 + r.nextInt(7);
			if (d0 > 1024.0D)
			{
				this.h += 10;
			}
			else if (d0 > 256.0D)
			{
				this.h += 5;
			}

			if (!this.entityCreature.getNavigation().a((Entity) entityliving, this.d))
			{
				this.h += 15;
			}
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