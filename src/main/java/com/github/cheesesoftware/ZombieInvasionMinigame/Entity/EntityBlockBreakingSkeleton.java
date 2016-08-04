package com.github.cheesesoftware.ZombieInvasionMinigame.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Random;

import org.bukkit.craftbukkit.v1_10_R1.util.UnsafeList;

import com.github.cheesesoftware.State.PlayingState;
import com.github.cheesesoftware.ZombieInvasionMinigame.PathfinderGoal.PathfinderGoalFindBreakBlock;
import com.github.cheesesoftware.ZombieInvasionMinigame.PathfinderGoal.PathfinderGoalWalkToTile;

import net.minecraft.server.v1_10_R1.AttributeInstance;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntitySkeleton;
import net.minecraft.server.v1_10_R1.Navigation;
import net.minecraft.server.v1_10_R1.NavigationAbstract;
import net.minecraft.server.v1_10_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_10_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_10_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_10_R1.World;

public class EntityBlockBreakingSkeleton extends EntitySkeleton implements ICustomMonster {
    private Random r = new Random();

    public EntityBlockBreakingSkeleton(World world) {
        super(world);

        try {
            Field field = NavigationAbstract.class.getDeclaredField("f");
            field.setAccessible(true);
            AttributeInstance e = (AttributeInstance) field.get(this.getNavigation());
            e.setValue(128); // Navigation distance in block lengths goes here
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Field gsa = PathfinderGoalSelector.class.getDeclaredField("b");
            gsa.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(gsa, gsa.getModifiers() & ~Modifier.FINAL);

            gsa.set(this.goalSelector, new UnsafeList<Object>());
            gsa.set(this.targetSelector, new UnsafeList<Object>());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((Navigation) this.getNavigation()).b(true);
        // this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this,
        // EntityHuman.class, 1.0D, false));
        this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(1, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(1, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        this.targetSelector.a(2, new PathfinderGoalHurtByTarget(this, true));
        this.a(0.6F, 1.8F);

    }

    public void setPlayingState(PlayingState state) {
        if (state != null)
            this.targetSelector.a(4, new PathfinderGoalWalkToTile(this, 1.2F, state.getSpawnLocation()));
        this.goalSelector.a(3, new PathfinderGoalFindBreakBlock(this, state, 5));
    }

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

    /*
     * @Override public Entity findTarget() { return super.findTarget(); }
     */

}
