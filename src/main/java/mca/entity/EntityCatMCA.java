/**
 * 
 */
package mca.entity;

import java.util.UUID;
import java.util.logging.Logger;

import mca.core.Constants;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.enums.EnumGender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

/**
 * @author Michael M. Adkins
 *
 */
public class EntityCatMCA extends EntityOcelot implements EntityPet {

	boolean ownedByPlayer = true;
	public PetAttributes attributes;
	public static final String PATCHY_CAT_PATH = "mca:textures/patchy_cat.png";
	public static final String BLACK_CAT_PATH = "mca:textures/black_cat.png";
	public static final String CALICO_CAT_PATH = "mca:textures/calico_cat.png";
	public static final String SIAMESE_CAT_PATH = "mca:textures/siamese_cat.png";
	public static final String SNOW_CAT_PATH = "mca:textures/snow_cat.png";
	public static final String WHITE_CAT_PATH = "mca:textures/white_cat.png";

	/**
	 * @param worldIn
	 */
	public EntityCatMCA(World worldIn) {
		super(worldIn);
		attributes = new PetAttributes(this);
		this.setSize(0.6F, 0.7F);
		setSitting(false);
		// attributes.setTexture(WHITE_CAT_PATH);
	}

	/**
	 * @param worldIn
	 * @param owner
	 */
	public EntityCatMCA(World worldIn, EntityPlayer owner) {
		super(worldIn);
		this.setOwnerId(owner.getUniqueID());
		attributes = new PetAttributes(this);
		attributes.initialize();
		NBTPlayerData playerData = MCA.getPlayerData(owner);
		ownedByPlayer = true;
		EnumGender ownerGender = playerData.getGender();
		attributes.setGender(ownerGender);
		attributes.setName(getName());
		setSitting(false);
		// attributes.setTexture(WHITE_CAT_PATH);
	}

	/**
	 * @param worldIn
	 * @param owner
	 */
	public EntityCatMCA(World worldIn, EntityVillagerMCA owner) {
		super(worldIn);
		this.setOwnerId(owner.getUniqueID());
		attributes = new PetAttributes(this);
		attributes.initialize();
		ownedByPlayer = false;
		setSitting(false);
		// attributes.setTexture(WHITE_CAT_PATH);
	}

	/**
	 * @param worldIn
	 * @param owner
	 */
	public EntityCatMCA(World worldIn, EntityWitchMCA owner) {
		super(worldIn);
		this.setOwnerId(owner.getUniqueID());
		attributes = new PetAttributes(this);
		attributes.initialize();
		ownedByPlayer = false;
		setSitting(false);
		attributes.setTexture(BLACK_CAT_PATH);
	}

	@Override
	public int getGrowingAge() {
		if (ownedByPlayer) {
			return super.getGrowingAge();
		}
		else {
			EntityVillagerMCA owner = getVillagerOwnerInstance();
			if (owner != null) {
				return owner.attributes.getIsChild() ? -100 : super.growingAge;
			}
		}
		return super.growingAge;
	}

	@Override
	public int getTameSkin() {
		return super.getTameSkin();
	}

	@Override
	public void setTameSkin(int skinId) {
		super.setTameSkin(skinId);
		switch (skinId) {
		case 0:
		default:
			try {
				attributes.setTexture(PATCHY_CAT_PATH);
				attributes.setAngryTexture(PATCHY_CAT_PATH);
			}
			catch (Exception e) {
				Logger.getLogger(this.getClass().getName()).warning(e.getMessage());
			}
		case 1:
			try {
				attributes.setTexture(BLACK_CAT_PATH);
				attributes.setAngryTexture(BLACK_CAT_PATH);
			}
			catch (Exception e) {
				Logger.getLogger(this.getClass().getName()).warning(e.getMessage());
			}
		case 2:
			try {
				attributes.setTexture(CALICO_CAT_PATH);
				attributes.setAngryTexture(CALICO_CAT_PATH);
			}
			catch (Exception e) {
				Logger.getLogger(this.getClass().getName()).warning(e.getMessage());
			}
		case 3:
			try {
				attributes.setTexture(SIAMESE_CAT_PATH);
				attributes.setAngryTexture(SIAMESE_CAT_PATH);
			}
			catch (Exception e) {
				Logger.getLogger(this.getClass().getName()).warning(e.getMessage());
			}
		case 4:
			try {
				attributes.setTexture(SNOW_CAT_PATH);
				attributes.setAngryTexture(SNOW_CAT_PATH);
			}
			catch (Exception e) {
				Logger.getLogger(this.getClass().getName()).warning(e.getMessage());
			}
		case 5:
			try {
				attributes.setTexture(WHITE_CAT_PATH);
				attributes.setAngryTexture(WHITE_CAT_PATH);
			}
			catch (Exception e) {
				Logger.getLogger(this.getClass().getName()).warning(e.getMessage());
			}
		}
	}

	@Override
	public EntityPlayer getOwnerPlayer() {
		try {
			UUID uuid = this.getOwnerId();
			return uuid == null || uuid == Constants.EMPTY_UUID ? null : this.world.getPlayerEntityByUUID(uuid);
		}
		catch (IllegalArgumentException var2) {
			return null;
		}
	}

	@Override
	public EntityVillagerMCA getVillagerOwnerInstance() {
		if (this.getOwnerId() != null && this.getOwnerId() != Constants.EMPTY_UUID) {
			for (Object obj : world.loadedEntityList) {
				if (obj instanceof EntityVillagerMCA) {
					EntityVillagerMCA villager = (EntityVillagerMCA) obj;

					if (villager.getUniqueID().equals(getOwnerId())) {
						return villager;
					}
				}
			}
		}

		return null;
	}

	@Override
	public EntityLivingBase getOwner() {
		if (ownedByPlayer) {
			return getOwnerPlayer();
		}
		return getVillagerOwnerInstance();
	}

	/**
	 * @param owner
	 */
	@Override
	public void setOwner(EntityLivingBase owner) {
		this.setOwnerId(owner.getUniqueID());
		ownedByPlayer = owner instanceof EntityPlayerMP;
	}

	@Override
	public void setOwnerId(UUID uniqueId) {
		super.setOwnerId(uniqueId);
	}
}
