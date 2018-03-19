package mca.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.common.base.Optional;

import mca.data.PlayerMemory;
import mca.enums.EnumGender;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class PetAttributes {
	private final Entity pet;
	private final EntityDataManager dataManager;
	private static final DataParameter<String> NAME = EntityDataManager.<String>createKey(EntityWolfMCA.class,
			DataSerializers.STRING);
	private static final DataParameter<Integer> GENDER = EntityDataManager.<Integer>createKey(EntityWolfMCA.class,
			DataSerializers.VARINT);
	private Map<UUID, PlayerMemory> playerMemories;
	private static final DataParameter<String> TEXTURE = EntityDataManager.<String>createKey(EntityWolfMCA.class,
			DataSerializers.STRING);
	private static final DataParameter<String> ANGRY_TEXTURE = EntityDataManager
			.<String>createKey(EntityWolfMCA.class, DataSerializers.STRING);

	public PetAttributes(Entity pet) {

		this.pet = pet;
		this.dataManager = pet.getDataManager();
		playerMemories = new HashMap<UUID, PlayerMemory>();
	}

	public PetAttributes(NBTTagCompound nbt) {
		this.pet = null;
		this.dataManager = null;
		playerMemories = new HashMap<UUID, PlayerMemory>();
		readFromNBT(nbt);
	}

	public void initialize() {
		dataManager.register(NAME, "Lillith");
		dataManager.register(GENDER, EnumGender.FEMALE.getId());
		if (pet instanceof EntityWolfMCA) {
			dataManager.register(TEXTURE, "mca:textures/husky_untamed.png");
			dataManager.register(ANGRY_TEXTURE, "mca:textures/husky_angry.png");
		}
		else if (pet instanceof EntityCatMCA) {
			dataManager.register(TEXTURE, "mca:textures/white_cat.png");
			dataManager.register(ANGRY_TEXTURE, "mca:textures/white_cat.png");
		}
	}

	public void readFromNBT(NBTTagCompound nbt) {
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
				e.printStackTrace();
			}
		}

		// ticksAlive = nbt.getInteger("ticksAlive");
		// timesWarnedForLowHearts = nbt.getInteger("timesWarnedForLowHearts");
		// inventory.readInventoryFromNBT(nbt.getTagList("inventory", 10));
	}

	public String getName() {
		return dataManager.get(NAME);
	}

	public void setName(String name) {
		dataManager.set(NAME, name);
	}

	/**
	 * @return the gender
	 */
	public EnumGender getGender() {
		return EnumGender.byId(dataManager.get(GENDER));
	}

	/**
	 * @param gender
	 *            the gender to set
	 */
	public void setGender(EnumGender gender) {
		dataManager.set(GENDER, gender.getId());
	}

	/**
	 * @return the texture
	 */
	public String getTexture() {
		try {
			return dataManager.get(TEXTURE);
		}
		catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).warning(e.getMessage());
			return null;
		}
	}

	/**
	 * @param texture
	 *            the texture to set
	 */
	public void setTexture(String texture) {
		dataManager.set(TEXTURE, texture);
	}

	/**
	 * @return the angryTexture
	 */
	public String getAngryTexture() {
		return dataManager.get(ANGRY_TEXTURE);
	}

	/**
	 * @param angryTexture
	 *            the angryTexture to set
	 */
	public void setAngryTexture(String angryTexture) {
		dataManager.set(ANGRY_TEXTURE, angryTexture);
	}
}
