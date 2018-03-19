package mca.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Optional;

import mca.core.MCA;
import mca.data.PlayerMemory;
import mca.enums.EnumGender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public class WitchAttributes {
	private final EntityWitchMCA witch;
	private final EntityDataManager dataManager;
	private static final DataParameter<String> NAME = EntityDataManager.<String>createKey(EntityWitchMCA.class,
			DataSerializers.STRING);
	private static final DataParameter<Integer> GENDER = EntityDataManager.<Integer>createKey(EntityWitchMCA.class,
			DataSerializers.VARINT);
	private Map<UUID, PlayerMemory> playerMemories;

	public WitchAttributes(EntityWitchMCA witch) {

		this.witch = witch;
		this.dataManager = witch.getDataManager();
		playerMemories = new HashMap<UUID, PlayerMemory>();
	}

	public WitchAttributes(NBTTagCompound nbt) {
		this.witch = null;
		this.dataManager = null;
		playerMemories = new HashMap<UUID, PlayerMemory>();
		readFromNBT(nbt);
	}

	public void initialize() {
		dataManager.register(NAME, "Lillith");
		dataManager.register(GENDER, EnumGender.FEMALE.getId());
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

	/**
	 * 
	 */
	public void assignRandomName() {
		if (getGender() == EnumGender.MALE) {
			setName(String.format("%s the wizard", MCA.getLocalizer().getString("name.male")));
		}
		else {
			setName(String.format("%s the witch", MCA.getLocalizer().getString("name.female")));
		}
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
}
