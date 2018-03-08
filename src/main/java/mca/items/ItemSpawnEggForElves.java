package mca.items;

import mca.entity.EntityElfMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.enums.EnumRace;
import net.minecraft.world.World;
import radixcore.modules.RadixLogic;

public class ItemSpawnEggForElves extends ItemSpawnEgg {
	public ItemSpawnEggForElves(boolean isMale) {
		super(isMale);
	}

	@Override
	public void spawnCreature(World world, double posX, double posY, double posZ) {
		EntityVillagerMCA villager = new EntityElfMCA(world);
		villager.attributes.setGender(RadixLogic.getBooleanWithProbability(25) ? EnumGender.MALE : EnumGender.FEMALE);
		villager.attributes.setRace(EnumRace.Elf);
		villager.attributes.assignRandomName();
		villager.attributes.assignRandomProfession();
		villager.attributes.assignRandomPersonality();
		villager.attributes.assignRandomSkin();
		villager.setPosition(posX, posY, posZ);
		world.spawnEntity(villager);
	}
}
