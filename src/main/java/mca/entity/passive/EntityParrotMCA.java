/**
 * 
 */
package mca.entity.passive;

import mca.entity.PetAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.world.World;

/**
 * @author Michael M. Adkins
 *
 */
public class EntityParrotMCA extends EntityParrot {
	public PetAttributes attributes;
	Entity rider;
	/**
	 * @param world
	 */
	public EntityParrotMCA(World world) {
		super(world);
	}

	/**
	 * @see net.minecraft.entity.Entity#addPassenger(net.minecraft.entity.Entity)
	 */
	@Override
	protected void addPassenger(Entity passenger) {
		if (passenger.getRidingEntity() != this) {
			if (passenger instanceof EntityVillagerMCA) {
				((EntityVillagerMCA) passenger).setRidingEntity(this);
			}
		}
		try {
			super.addPassenger(passenger);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void removeRider() {
		try {
			super.removePassenger(rider);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
