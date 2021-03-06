package com.github.cheesesoftware.ZombieInvasionMinigame.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Random;
import java.util.Set;

import net.minecraft.server.v1_10_R1.AttributeInstance;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityWither;
import net.minecraft.server.v1_10_R1.Navigation;
import net.minecraft.server.v1_10_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_10_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_10_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_10_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_10_R1.World;

import org.bukkit.craftbukkit.v1_10_R1.util.UnsafeList;

import com.github.cheesesoftware.State.PlayingState;
import com.github.cheesesoftware.ZombieInvasionMinigame.PathfinderGoal.PathfinderGoalBreakBlock;
import com.github.cheesesoftware.ZombieInvasionMinigame.PathfinderGoal.PathfinderGoalCustomMeleeAttack;
import com.github.cheesesoftware.ZombieInvasionMinigame.PathfinderGoal.PathfinderGoalCustomNearestAttackableTarget;
import com.github.cheesesoftware.ZombieInvasionMinigame.PathfinderGoal.PathfinderGoalWalkToTile;

public class EntityBossWither extends EntityWither implements ICustomMonster {
    private Random r = new Random();

    public EntityBossWither(World world) {
        super(world);

        try {
            Field field = Navigation.class.getDeclaredField("f");
            field.setAccessible(true);
            AttributeInstance e = (AttributeInstance) field.get(this.getNavigation());
            e.setValue(128); // Navigation distance in block lengths goes here
        } catch (Exception ex) {
        }

        try {
            {
                Field b = PathfinderGoalSelector.class.getDeclaredField("b");
                b.setAccessible(true);
                Set blist = (Set) b.get(this.goalSelector);
                blist.clear();
            }

            {
                Field c = PathfinderGoalSelector.class.getDeclaredField("c");
                c.setAccessible(true);
                Set blist = (Set) c.get(this.goalSelector);
                blist.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((Navigation) this.getNavigation()).b(true);
        this.goalSelector.a(6, new PathfinderGoalFloat(this));
        this.goalSelector.a(7, new PathfinderGoalCustomMeleeAttack(this, EntityHuman.class, 1.0D, false));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
        this.a(0.6F, 1.8F);

    }

    public void setPlayingState(PlayingState state) {
        if (state != null)
            this.goalSelector.a(0, new PathfinderGoalWalkToTile(this, 1.0F, state.getSpawnLocation()));

        this.goalSelector.a(1, new PathfinderGoalBreakBlock(this, state, 15, true));
        this.targetSelector.a(1, new PathfinderGoalCustomNearestAttackableTarget(this, 0, state));
    }

    /*
     * @Override protected Entity findTarget() { EntityHuman entityhuman = this.findNearbyVulnerablePlayer(128, 128, 128);
     * 
     * return entityhuman; }
     */

    public EntityHuman findNearbyVulnerablePlayer(double d0, double d1, double d2) {
        if (world.players.size() > 0) {
            int i = r.nextInt(world.players.size());
            EntityHuman entityhuman1 = (EntityHuman) this.world.players.get(i);

            if (!entityhuman1.abilities.isInvulnerable && entityhuman1.isAlive()) {
                return entityhuman1;
            }
        }
        return null;
    }
}
