package mca.items;

import mca.core.MCA;
import mca.entity.EntityElfMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.enums.EnumRace;
import net.minecraft.world.World;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;

public class ItemSpawnEggForElves extends ItemSpawnEgg {
	public ItemSpawnEggForElves(boolean isMale) {
		super(isMale);
	}

	@Override
	public void spawnCreature(World world, double posX, double posY, double posZ) {
		EntityVillagerMCA elf = new EntityElfMCA(world);
		
		elf.attributes.setGender(RadixLogic.getBooleanWithProbability(25) ? EnumGender.MALE : EnumGender.FEMALE);
		elf.attributes.setRace(EnumRace.Elf);
		elf.attributes.assignRandomName();
		elf.attributes.assignRandomProfession();
		elf.attributes.assignRandomPersonality();
		elf.attributes.assignRandomSkin();
		elf.setPosition(posX, posY, posZ);
		world.spawnEntity(elf);
		if (RadixLogic.getBooleanWithProbability(1)) {
			MCA.naturallySpawnElves(new Point3D(posX, posY, posZ), world, elf.getProfession());
		}
	}
}
