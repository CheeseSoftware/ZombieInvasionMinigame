package io.github.gustav9797.Zombie;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.server.v1_7_R3.EntityInsentient;
import net.minecraft.server.v1_7_R3.EntityTypes;
import net.minecraft.server.v1_7_R3.IMonster;

import org.bukkit.entity.EntityType;

public class CustomEntityType implements IMonster
{
	// SKELETON("Skeleton", 51, EntityType.SKELETON, EntitySkeleton.class,
	// CustomEntitySkeleton.class),

	private String name;
	private int id;
	private EntityType entityType;
	private Class<? extends EntityInsentient> nmsClass;
	private Class<? extends EntityInsentient> customClass;

	public CustomEntityType(String name, int id, EntityType entityType, Class<? extends EntityInsentient> nmsClass, Class<? extends EntityInsentient> customClass)
	{
		this.name = name;
		this.id = id;
		this.entityType = entityType;
		this.nmsClass = nmsClass;
		this.customClass = customClass;
		Register();
	}

	public String getName()
	{
		return this.name;
	}

	public int getId()
	{
		return this.id;
	}

	public EntityType getEntityType()
	{
		return this.entityType;
	}

	public Class<? extends EntityInsentient> getNMSClass()
	{
		return this.nmsClass;
	}

	public Class<? extends EntityInsentient> getCustomClass()
	{
		return this.customClass;
	}

	@SuppressWarnings("rawtypes")
	private static Object getPrivateStatic(Class clazz, String f) throws Exception
	{
		Field field = clazz.getDeclaredField(f);
		field.setAccessible(true);
		return field.get(null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void a(Class paramClass, String paramString, int paramInt)
	{
		try
		{
			((Map) getPrivateStatic(EntityTypes.class, "c")).put(paramString, paramClass);
			((Map) getPrivateStatic(EntityTypes.class, "d")).put(paramClass, paramString);
			((Map) getPrivateStatic(EntityTypes.class, "e")).put(Integer.valueOf(paramInt), paramClass);
			((Map) getPrivateStatic(EntityTypes.class, "f")).put(paramClass, Integer.valueOf(paramInt));
			((Map) getPrivateStatic(EntityTypes.class, "g")).put(paramString, Integer.valueOf(paramInt));
		}
		catch (Exception exc)
		{
		}
	}

	private void Register()
	{
		a(getCustomClass(), getName(), getId());
	}

}
