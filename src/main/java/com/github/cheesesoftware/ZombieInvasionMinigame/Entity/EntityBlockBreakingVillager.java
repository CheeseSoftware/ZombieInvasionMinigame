package com.github.cheesesoftware.ZombieInvasionMinigame.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Random;
import java.util.Set;

import net.minecraft.server.v1_10_R1.AttributeInstance;
import net.minecraft.server.v1_10_R1.DamageSource;
import net.minecraft.server.v1_10_R1.Entity;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityVillager;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import net.minecraft.server.v1_10_R1.Navigation;
import net.minecraft.server.v1_10_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_10_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_10_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_10_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_10_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_10_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_10_R1.World;

import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_10_R1.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.cheesesoftware.State.PlayingState;
import com.github.cheesesoftware.ZombieInvasionMinigame.PathfinderGoal.PathfinderGoalBreakBlock;
import com.github.cheesesoftware.ZombieInvasionMinigame.PathfinderGoal.PathfinderGoalWalkToTile;

public class EntityBlockBreakingVillager extends EntityVillager implements ICustomMonster {
    private Random r = new Random();

    public EntityBlockBreakingVillager(World world) {
        super(world);
        try {
            Field field = Navigation.class.getDeclaredField("f");
            field.setAccessible(true);
            AttributeInstance e = (AttributeInstance) field.get(this.getNavigation());
            e.setValue(128); // Navigation distance in block lengths goes here
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.3D); // walking
            // speed
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

        this.setProfession(r.nextInt(6));

        if (random.nextInt(4) != 0) // 75% villagers, 25% random
            this.setProfession(5);

        int profession = this.getProfession();

        switch (profession) {
        case 0: // farmer
            // ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 8));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 3));
            break;
        case 1: // Librarian
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 2));

            // ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 8));
            // ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 2));
            // ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1));
            // ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 1));
            // ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 1));
            break;
        case 2: // Priest
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 8));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
            // ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 1));
            // ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1)); // heal
            // others!
            break;
        case 3: // Blacksmith
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 4));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 2));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
            break;
        case 4: // Butcher
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 2));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 1));
            ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
            break;
        case 5:
            // Generic, "villager"
            // ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
            // ((LivingEntity) this.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
            break;
        }

        ((Navigation) this.getNavigation()).b(true);
        this.goalSelector.a(6, new PathfinderGoalFloat(this));
        this.goalSelector.a(7, new com.github.cheesesoftware.ZombieInvasionMinigame.PathfinderGoal.PathfinderGoalCustomMeleeAttack(this, EntityHuman.class, 1.0D, false));
        this.goalSelector.a(8, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
        this.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        this.a(0.6F, 1.8F);
    }

    public void setPlayingState(PlayingState state) {
        if (state != null)
            this.targetSelector.a(0, new PathfinderGoalWalkToTile(this, 1.0F, state.getSpawnLocation()));
        if (this.getProfession() == 3)
            this.goalSelector.a(3, new PathfinderGoalBreakBlock(this, state, 80, true));
        else if (this.getProfession() == 5)
            this.goalSelector.a(3, new PathfinderGoalBreakBlock(this, state, 20, true));
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

    @Override
    public boolean damageEntity(DamageSource arg0, float arg1) {
        // 1:librarian slender
        if (this.getProfession() == 1 && random.nextInt(8) == 0) {
            Entity e = arg0.getEntity();
            if (e instanceof Snowball) {
                e = (Entity) ((Snowball) e).getShooter();
            }

            if (e instanceof Player) {
                this.teleportTo(((Player) e).getLocation(), true);// this.enderTeleportTo(e.locX, e.locY, e.locZ);
                ((Player) e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));

                ((Player) e).playSound(((Player) e).getLocation(), Sound.ENTITY_GHAST_SCREAM, 1, 0.125F);
                ((Player) e).playSound(((Player) e).getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 2, 0.25F);
            }
        }
        return super.damageEntity(arg0, arg1);
    }

}
