package mca.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.UUID;

import com.google.common.base.Optional;

import mca.entity.monster.EntityTitan;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraftforge.fml.common.FMLLog;

// Referenced classes of package mods.TheTitan:
//            EntityTitan

public class TitanAttributes {
	private static final DataParameter<String> TEXTURE = EntityDataManager.<String>createKey(EntityTitan.class,
			DataSerializers.STRING);
	private final EntityDataManager dataManager;
	private EntityTitan titan;
	private int ticksAlive;

	public TitanAttributes(EntityTitan titan) {
		this.titan = titan;
		this.dataManager = titan.getDataManager();
	}

	public void initialize() {
	}

	public void setSkin(String texture) {
		dataManager.set(TEXTURE, texture);
	}

	public void readEntityFromNBT(NBTTagCompound nbt) {
		// Auto read data manager values
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
		ticksAlive = nbt.getInteger("ticksAlive");
	}

	public void writeToNBT(NBTTagCompound nbt) {
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
}
