package io.github.gustav9797.ZombieInvasionMinigame.PathfinderGoal;

import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;

import net.minecraft.server.v1_8_R1.AttributeInstance;
import net.minecraft.server.v1_8_R1.EntityCreature;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EntityLiving;
import net.minecraft.server.v1_8_R1.EntityOwnable;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.GenericAttributes;
import net.minecraft.server.v1_8_R1.MathHelper;
import net.minecraft.server.v1_8_R1.PathEntity;
import net.minecraft.server.v1_8_R1.PathPoint;
import net.minecraft.server.v1_8_R1.PathfinderGoal;

public abstract class PathfinderGoalCustomTarget extends PathfinderGoal {

    protected EntityCreature entity;
    protected boolean d;
    // private boolean a;
    private int b;
    private int e;
    private int f;
    private Location oldLocation = null;
    private int ticksStoodStill = 0;
    private boolean isWalking = true;
    private Random r = new Random();

    public PathfinderGoalCustomTarget(EntityCreature entitycreature) {
	this.entity = entitycreature;
    }

    public boolean b() // canContinue
    {
	EntityLiving entityliving = this.entity.getGoalTarget();

	if (!this.isWalking) {
	    return false;
	}
	if (entityliving == null) {
	    return false;
	} else if (!entityliving.isAlive()) {
	    return false;
	} else {
	    double d0 = this.f();

	    if (this.entity.h(entityliving) > d0 * d0) {
		return false;
	    } else {
		if (this.d) {
		    // if (this.c.getEntitySenses().canSee(entityliving))
		    // {
		    this.f = 0;
		    // }
		    /* else */if (++this.f > 60) {
			return false;
		    }
		}

		return !(entityliving instanceof EntityPlayer) || !((EntityPlayer) entityliving).playerInteractManager.isCreative();
	    }
	}
    }

    protected double f() {
	AttributeInstance attributeinstance = this.entity.getAttributeInstance(GenericAttributes.b);

	return attributeinstance == null ? 16.0D : attributeinstance.getValue();
	//return 128;
    }

    public void c() // setup
    {
	this.b = 0;
	this.e = 0;
	this.f = 0;
    }

    public void d() // finish
    {
	// this.entity.setGoalTarget((EntityLiving) null);
    }

    public void e() // move
    {
	Location currentLocation = this.entity.getBukkitEntity().getLocation();
	if (this.oldLocation != null) {
	    if (currentLocation.distance(oldLocation) <= 1.5) {
		this.ticksStoodStill++;
		if (this.ticksStoodStill > 20) {
		    this.isWalking = false;
		    this.ticksStoodStill = 0;
		}
	    } else {
		this.ticksStoodStill = 0;
		this.isWalking = true;
	    }
	}
	this.oldLocation = currentLocation;
    }

    protected boolean canTarget(EntityLiving target, boolean flag) // canExecute
    {
	if (target == null)
	    return false;
	else if (target == this.entity)
	    return false;
	else if (!target.isAlive())
	    return false;
	else if (!this.entity.a(target.getClass()))
	    return false;
	else {
	    if (this.entity instanceof EntityOwnable && StringUtils.isNotEmpty(((EntityOwnable) this.entity).getOwnerUUID())) {
		if (target instanceof EntityOwnable && ((EntityOwnable) this.entity).getOwnerUUID().equals(((EntityOwnable) target).getOwnerUUID())) {
		    return false;
		}

		if (target == ((EntityOwnable) this.entity).getOwner()) {
		    return false;
		}
	    } else if (target instanceof EntityHuman && !flag && ((EntityHuman) target).abilities.isInvulnerable) {
		return false;
	    }

	    // if (!this.entity.b(MathHelper.floor(target.locX), MathHelper.floor(target.locY), MathHelper.floor(target.locZ)))
	    {
		// return false;
	    }
	    // TODO: FIX

	    // else if (this.d && !this.c.getEntitySenses().canSee(target)) {
	    // return false; } else

	    // else if(this.d)
	    {
		// if (this.a)
		{
		    if (--this.e <= 0) {
			this.b = 0;
		    }

		    if (this.b == 0) {
			this.b = this.canWalkToTarget(target) ? 1 : 2;
		    }

		    if (this.b == 2) {
			return false;
		    }
		}

		return true;
	    }
	}
    }

    private boolean canWalkToTarget(EntityLiving target) {
	this.e = 10 + r.nextInt(5);
	PathEntity pathentity = this.entity.getNavigation().a(target);

	if (pathentity == null) {
	    return false;
	} else {
	    PathPoint pathpoint = pathentity.c();

	    if (pathpoint == null) {
		return false;
	    } else {
		int i = pathpoint.a - MathHelper.floor(target.locX);
		int j = pathpoint.c - MathHelper.floor(target.locZ);

		return (double) (i * i + j * j) <= 2.25D;
	    }
	}
    }
}