package mca.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.UUID;

import com.google.common.base.Optional;

import mca.entity.monster.EntityTitan;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;

public class TitanCore extends Entity {
	private EntityTitan titan;
	private int ticksAlive;
	public TitanCore(World world) {
		super(world);
		setSize(1.0F, 1.5F);
	}

	public TitanCore(EntityTitan titan, World world) {
		this(world);
		this.setTitan(titan);
	}

	@Override
	protected void entityInit() {
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (getTitan() == null && !world.isRemote) {
			isDead = true;
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		for (Field f : this.getClass().getDeclaredFields()) {
			try {
				if (f.getType() == DataParameter.class) {
					Type genericType = f.getGenericType();
					String typeName = genericType.getTypeName();
					DataParameter param = (DataParameter) f.get(this);
					String paramName = f.getName();

					if (typeName.contains("Boolean")) {
						DataParameter<Boolean> bParam = param;
						dataManager.set(bParam, nbt.getBoolean(paramName));
					}
					else if (typeName.contains("Integer")) {
						DataParameter<Integer> iParam = param;
						dataManager.set(iParam, nbt.getInteger(paramName));
					}
					else if (typeName.contains("String")) {
						DataParameter<String> sParam = param;
						dataManager.set(sParam, nbt.getString(paramName));
					}
					else if (typeName.contains("Float")) {
						DataParameter<Float> fParam = param;
						dataManager.set(fParam, nbt.getFloat(paramName));
					}
					else if (typeName.contains("Optional<java.util.UUID>")) {
						DataParameter<Optional<UUID>> uuParam = param;
						dataManager.set(uuParam, Optional.of(nbt.getUniqueId(paramName)));
					}
					else {
						throw new RuntimeException("Field type not handled while saving to NBT: " + f.getName());
					}
				}
			}
			catch (Exception e) {
				String msg = String.format("Exception occurred!%nMessage: %s%n", e.getLocalizedMessage());
				FMLLog.severe(msg, e);
				// java.util.logging.LogManager.getLogManager().getLogger(this.getClass().getName()).severe(msg);
				org.apache.logging.log4j.LogManager.getLogger(this.getClass().getName()).error(msg, e);
				// java.util.logging.Logger.getLogger(this.getClass().getName()).severe(msg);
			}
		}

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		// Auto save data manager values to NBT by reflection
		for (Field f : this.getClass().getDeclaredFields()) {
			try {
				if (f.getType() == DataParameter.class) {
					Type genericType = f.getGenericType();
					String typeName = genericType.getTypeName();
					DataParameter param = (DataParameter) f.get(this);
					String paramName = f.getName();

					if (typeName.contains("Boolean")) {
						DataParameter<Boolean> bParam = param;
						nbt.setBoolean(paramName, dataManager.get(bParam).booleanValue());
					}
					else if (typeName.contains("Integer")) {
						DataParameter<Integer> iParam = param;
						nbt.setInteger(paramName, dataManager.get(iParam).intValue());
					}
					else if (typeName.contains("String")) {
						DataParameter<String> sParam = param;
						nbt.setString(paramName, dataManager.get(sParam));
					}
					else if (typeName.contains("Float")) {
						DataParameter<Float> fParam = param;
						nbt.setFloat(paramName, dataManager.get(fParam).floatValue());
					}
					else if (typeName.contains("Optional<java.util.UUID>")) {
						DataParameter<Optional<UUID>> uuParam = param;
						nbt.setUniqueId(paramName, dataManager.get(uuParam).get());
					}
					else {
						throw new RuntimeException("Field type not handled while saving to NBT: " + f.getName());
					}
				}
			}
			catch (Exception e) {
				String msg = String.format("Exception occurred!%nMessage: %s%n", e.getLocalizedMessage());
				FMLLog.severe(msg, e);
				// java.util.logging.LogManager.getLogManager().getLogger(this.getClass().getName()).severe(msg);
				org.apache.logging.log4j.LogManager.getLogger(this.getClass().getName()).error(msg, e);
				// java.util.logging.Logger.getLogger(this.getClass().getName()).severe(msg);
			}
		}

		nbt.setInteger("ticksAlive", ticksAlive);

	}

	/**
	 * @return the titan
	 */
	public EntityTitan getTitan() {
		return titan;
	}

	/**
	 * @param titan the titan to set
	 */
	public void setTitan(EntityTitan titan) {
		this.titan = titan;
	}

}
