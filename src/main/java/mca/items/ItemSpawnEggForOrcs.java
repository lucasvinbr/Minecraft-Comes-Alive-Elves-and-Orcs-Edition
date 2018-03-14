package mca.items;

import mca.core.MCA;
import mca.entity.EntityOrcMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.enums.EnumRace;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.world.World;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;

public class ItemSpawnEggForOrcs extends ItemSpawnEgg {
	public ItemSpawnEggForOrcs(boolean isMale) {
		super(isMale);
	}

	@Override
	public void spawnCreature(World world, double posX, double posY, double posZ) {
		EntityVillagerMCA orc = new EntityOrcMCA(world);
		orc.attributes.setRace(EnumRace.Orc);
		orc.attributes.setGender(RadixLogic.getBooleanWithProbability(75) ? EnumGender.MALE : EnumGender.FEMALE);
		orc.attributes.assignRandomName();
		orc.attributes.assignRandomPersonality();
		orc.attributes.assignRandomSkin();
//		orc.attributes.setProfession(EnumProfession.Unassigned);
		orc.setPosition(posX, posY, posZ);
		world.spawnEntity(orc);

		if (RadixLogic.getBooleanWithProbability(75)) {
			if (orc.attributes.getGender() == EnumGender.MALE) {
				EntityWolf wolf = new EntityWolf(world);
				wolf.setPosition(orc.posX, orc.posY, orc.posZ + 1);
				wolf.setTamed(false);
				wolf.setOwnerId(orc.getUniqueID());
				// EntityAIBase aiFollowOwner = new EntityAIFollowOwner(wolf, 1.0D, 10.0F,
				// 2.0F);
				// wolf.tasks.addTask(1, aiFollowOwner);
				wolf.setCustomNameTag(String.format("%s's wolf", orc.getName()));
				orc.setPet(wolf);
				world.spawnEntity(wolf);
			}
			else {
				EntityOcelot cat = new EntityOcelot(world);
				cat.setPosition(orc.posX, orc.posY, orc.posZ);
				cat.setTamed(false);
				cat.setOwnerId(orc.getUniqueID());
				// EntityAIBase aiFollowOwner = new EntityAIFollowOwner(cat, 1.0D, 10.0F, 2.0F);
				// cat.tasks.addTask(1, aiFollowOwner);
				cat.setCustomNameTag(String.format("%s's cat", orc.getName()));
				orc.setPet(cat);
				world.spawnEntity(cat);
			}
		}
		// else {
		// EntityHorse horse = new EntityHorse(orc.world);
		// horse.setPosition(orc.posX, orc.posY, orc.posZ);
		// horse.setOwnerUniqueId(orc.getUniqueID());
		// horse.setHorseTamed(true);
		// horse.setHorseSaddled(true);
		// // horse.getPassengers().add(orc);
		// world.spawnEntity(horse);
		// }
		if (RadixLogic.getBooleanWithProbability(50)) {
			MCA.naturallySpawnOrcs(new Point3D(posX, posY, posZ), world, orc.getProfession());
			// MCA.startOrcMatingSeason();
		}
	}
}
