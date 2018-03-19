/**
 * 
 */
package mca.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author Michael M. Adkins
 *
 */
public interface EntityPet {
	// public EntityDataManager getDataManager();

	public void setOwner(EntityLivingBase owner);

	public EntityPlayer getOwnerPlayer();

	public EntityVillagerMCA getVillagerOwnerInstance();
}
