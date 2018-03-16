/**
 * 
 */
package mca.entity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import mca.core.minecraft.SoundsMCA;
import mca.data.PlayerMemory;
import net.minecraft.block.material.Material;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import radixcore.constant.Time;
import radixcore.modules.RadixNettyIO;

/**
 * @author Michael M. Adkins
 *
 */
public class EntityWitchMCA extends EntityWitch implements IEntityAdditionalSpawnData {
	// private static final DataParameter<Boolean> IS_ANGRY =
	// EntityDataManager.<Boolean>createKey(EntityWitchMCA.class,
	// DataSerializers.BOOLEAN);
	private static final int MAX_WAIT_TIME = Time.SECOND / 2;
	private int witchAttackTimer;
	private Map<UUID, PlayerMemory> playerMemories;
	private static final DataParameter<Boolean> DO_DISPLAY = EntityDataManager
			.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);
	/**
	 * @param worldIn
	 */
	public EntityWitchMCA(World worldIn) {
		super(worldIn);
	}

//	/**
//	 * @return if angry
//	 */
//	public boolean isAngry() {
//		return this.getDataManager().get(IS_ANGRY);
//	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.entity.monster.EntityWitch#onLivingUpdate()
	 */
	@Override
	public void onLivingUpdate() {
		if (!this.world.isRemote) {
			if (this.isDrinkingPotion()) {
				if (this.witchAttackTimer-- <= 0) {
					this.setAggressive(false);
					ItemStack itemstack = this.getHeldItemMainhand();
					this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);

					if (itemstack.getItem() == Items.POTIONITEM) {
						List<PotionEffect> list = PotionUtils.getEffectsFromStack(itemstack);

						if (list != null) {
							for (PotionEffect potioneffect : list) {
								this.addPotionEffect(new PotionEffect(potioneffect));
							}
						}
					}

					// this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(MODIFIER);
				}
			}
			else {
				PotionType potiontype = null;

				if (this.rand.nextFloat() < 0.15F && this.isInsideOfMaterial(Material.WATER)
						&& !this.isPotionActive(MobEffects.WATER_BREATHING)) {
					potiontype = PotionTypes.WATER_BREATHING;
				}
				else if (this.rand.nextFloat() < 0.15F
						&& (this.isBurning()
								|| this.getLastDamageSource() != null && this.getLastDamageSource().isFireDamage())
						&& !this.isPotionActive(MobEffects.FIRE_RESISTANCE)) {
					potiontype = PotionTypes.FIRE_RESISTANCE;
				}
				else if (this.rand.nextFloat() < 0.05F && this.getHealth() < this.getMaxHealth()) {
					potiontype = PotionTypes.HEALING;
				}
				else if (this.rand.nextFloat() < 0.5F && this.getAttackTarget() != null
						&& !this.isPotionActive(MobEffects.SPEED)
						&& this.getAttackTarget().getDistanceSqToEntity(this) > 121.0D) {
					potiontype = PotionTypes.SWIFTNESS;
				}

				if (potiontype != null) {
					this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
							PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), potiontype));
					this.witchAttackTimer = MAX_WAIT_TIME;
					this.setAggressive(true);
					this.playSound(this.getAmbientSound(), 1.0f, 0.9f);
				}
			}

			if (this.rand.nextFloat() < 7.5E-4F) {
				this.world.setEntityState(this, (byte) 15);
			}
		}

		super.onLivingUpdate();
	}
//
//	/**
//	 * @param angry
//	 *            the anger to set
//	 */
//	public void setAngry(boolean angry) {
//		this.getDataManager().set(IS_ANGRY, Boolean.valueOf(angry));
//	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundsMCA.villager_female_heh;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
		return SoundsMCA.evil_female_hurt_1;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundsMCA.evil_female_death_1;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		RadixNettyIO.writeObject(buffer, playerMemories);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		Map<UUID, PlayerMemory> recvMemories = (Map<UUID, PlayerMemory>) RadixNettyIO.readObject(buffer);
		// playerMemories = recvMemories;
		setDoDisplay(true);

	}

	public boolean getDoDisplay() {
		return dataManager.get(DO_DISPLAY);
	}

	public void setDoDisplay(boolean value) {
		dataManager.set(DO_DISPLAY, value);
	}
}
