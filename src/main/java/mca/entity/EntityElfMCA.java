package mca.entity;

import mca.enums.EnumGender;
import mca.enums.EnumProfession;
import mca.enums.EnumRace;
import net.minecraft.world.World;

public class EntityElfMCA extends EntityVillagerMCA {
	public EntityElfMCA(World world) {
		super(world);
		maxSwingProgressTicks = 10;
		this.attributes.setProfession(EnumProfession.Elf);
		if(this.attributes.getGender() == EnumGender.MALE) {
			this.attributes.setProfession(EnumProfession.Guard);
		} else {
			this.attributes.setProfession(EnumProfession.Archer);
		}
		this.attributes.setRace(EnumRace.Elf);
	}
}
