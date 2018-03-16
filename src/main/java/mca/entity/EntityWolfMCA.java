/**
 * 
 */
package mca.entity;

import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

/**
 * @author Michael M. Adkins
 *
 */
public class EntityWolfMCA extends EntityWolf implements IJumpingMount {

	private static final DataParameter<Byte> STATUS = EntityDataManager.<Byte>createKey(AbstractHorse.class,
			DataSerializers.BYTE);
	private float jumpPower;
	/**
	 * @param worldIn
	 */
	public EntityWolfMCA(World worldIn) {
		super(worldIn);
	}

	/* (non-Javadoc)
	 * @see net.minecraft.entity.IJumpingMount#setJumpPower(int)
	 */
	@Override
	public void setJumpPower(int jumpPowerIn) {
			if (jumpPowerIn < 0) {
				jumpPowerIn = 0;
			}

			if (jumpPowerIn >= 90) {
				this.jumpPower = 1.0F;
			}
			else {
				this.jumpPower = 0.4F + 0.4F * jumpPowerIn / 90.0F;
			}

	}

	/* (non-Javadoc)
	 * @see net.minecraft.entity.IJumpingMount#canJump()
	 */
	@Override
	public boolean canJump() {
		return true;
	}

	protected boolean getHorseWatchableBoolean(int p_110233_1_) {
		return (this.dataManager.get(STATUS).byteValue() & p_110233_1_) != 0;
	}

	/* (non-Javadoc)
	 * @see net.minecraft.entity.IJumpingMount#handleStartJump(int)
	 */
	@Override
	public void handleStartJump(int p_184775_1_) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.minecraft.entity.IJumpingMount#handleStopJump()
	 */
	@Override
	public void handleStopJump() {
		// TODO Auto-generated method stub

	}

	public boolean setEntityOnShoulder(EntityPlayer p_191994_1_) {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setString("id", this.getEntityString());
		this.writeToNBT(nbttagcompound);

		if (p_191994_1_.addShoulderEntity(nbttagcompound)) {
			this.world.removeEntity(this);
			return true;
		}
		else {
			return false;
		}
	}
}
