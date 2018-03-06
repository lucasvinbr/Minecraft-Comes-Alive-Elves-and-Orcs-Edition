package mca.items;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.enums.EnumProfession;
import mca.enums.EnumRace;
import net.minecraft.world.World;

public class ItemSpawnEggForOrcs extends ItemSpawnEgg {
	public ItemSpawnEggForOrcs(boolean isMale) {
		super(isMale);
	}

	@Override
	public void spawnCreature(World world, double posX, double posY, double posZ) {
		EntityVillagerMCA orc = new EntityVillagerMCA(world);
		orc.attributes.setRace(EnumRace.Orc);
		orc.attributes.setGender(isMale ? EnumGender.MALE : EnumGender.FEMALE);
		orc.attributes.assignRandomName();
		orc.attributes.assignRandomPersonality();
		orc.attributes.assignRandomSkin();
		orc.attributes.setProfession(EnumProfession.Orc);
		orc.setPosition(posX, posY, posZ);
		world.spawnEntity(orc);
	}
}
