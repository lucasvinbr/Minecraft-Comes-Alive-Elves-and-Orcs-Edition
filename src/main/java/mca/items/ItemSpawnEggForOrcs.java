package mca.items;

import mca.core.MCA;
import mca.entity.passive.EntityOrcMCA;
import mca.entity.passive.EntityVillagerMCA;
import mca.entity.passive.EntityWolfMCA;
import mca.enums.EnumGender;
import mca.enums.EnumRace;
import net.minecraft.world.World;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;

public class ItemSpawnEggForOrcs extends ItemSpawnEgg {
	public ItemSpawnEggForOrcs(boolean isMale) {
		super(isMale);
		maxStackSize = 64;
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

		if (RadixLogic.getBooleanWithProbability(100)) {
			EntityWolfMCA wolf = new EntityWolfMCA(world, orc);
			wolf.setPosition(orc.posX, orc.posY, orc.posZ + 1);
			wolf.setTamed(false);
			wolf.setOwnerId(orc.getUniqueID());
			wolf.attributes.setTexture("mca:textures/husky_untamed.png");
			wolf.attributes.setAngryTexture("mca:textures/husky_angry.png");
			wolf.setCustomNameTag(String.format("%s's wolf", orc.getName()));
			orc.setPet(wolf);
			world.spawnEntity(wolf);
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
