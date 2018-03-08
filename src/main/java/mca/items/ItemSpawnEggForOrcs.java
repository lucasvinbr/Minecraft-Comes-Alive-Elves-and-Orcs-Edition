package mca.items;

import mca.entity.EntityOrcMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.enums.EnumProfession;
import mca.enums.EnumRace;
import net.minecraft.world.World;
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
	}
}
