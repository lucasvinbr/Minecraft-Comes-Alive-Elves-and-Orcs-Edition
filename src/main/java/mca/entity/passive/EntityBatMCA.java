package mca.entity.passive;

import mca.entity.monster.EntityWitchMCA;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.world.World;

public class EntityBatMCA extends EntityBat {
	Entity rider;
	public EntityBatMCA(World worldIn) {
		super(worldIn);
	}
	
	/**
	 * @see net.minecraft.entity.Entity#addPassenger(net.minecraft.entity.Entity)
	 */
	@Override
	protected void addPassenger(Entity passenger) {
		if (passenger.getRidingEntity() != this) {
			if (passenger instanceof EntityWitchMCA) {
				((EntityWitchMCA) passenger).setRidingEntity(this);
			}
		}
		try {
			super.addPassenger(passenger);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void removeRider() {
		super.removePassenger(rider);
	}

	/**
	 * @param rider
	 *            of the wolf's back
	 */
	public void setRider(Entity rider) {
		this.rider = rider;
		addPassenger(rider);
	}
}
