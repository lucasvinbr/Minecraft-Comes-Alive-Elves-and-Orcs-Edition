package mca.core.minecraft;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class SoundsMCA {
	private static final ResourceLocation loc_reaper_scythe_out = new ResourceLocation("mca:reaper.scythe.out");
	private static final ResourceLocation loc_reaper_scythe_swing = new ResourceLocation("mca:reaper.scythe.swing");
	private static final ResourceLocation loc_reaper_idle = new ResourceLocation("mca:reaper.idle");
	private static final ResourceLocation loc_reaper_death = new ResourceLocation("mca:reaper.death");
	private static final ResourceLocation loc_reaper_block = new ResourceLocation("mca:reaper.block");
	private static final ResourceLocation loc_reaper_summon = new ResourceLocation("mca:reaper.summon");
	private static final ResourceLocation loc_femalehurt = new ResourceLocation("mca:femalehurt");
	private static final ResourceLocation loc_femalehurt1 = new ResourceLocation("mca:femalehurt1");
	private static final ResourceLocation loc_femalehurt2 = new ResourceLocation("mca:femalehurt2");
	private static final ResourceLocation loc_femalehurt3 = new ResourceLocation("mca:femalehurt3");
	private static final ResourceLocation loc_femalehurt4 = new ResourceLocation("mca:femalehurt4");
	private static final ResourceLocation loc_femalehurt5 = new ResourceLocation("mca:femalehurt5");
	private static final ResourceLocation loc_femalehurt6 = new ResourceLocation("mca:femalehurt6");
	private static final ResourceLocation loc_femaleattack1 = new ResourceLocation("mca:femaleattack1");
	private static final ResourceLocation loc_femaleattack2 = new ResourceLocation("mca:femaleattack2");
	private static final ResourceLocation loc_girl_villager_uhuh = new ResourceLocation("mca:girl.villager.uhuh");

	private static final ResourceLocation loc_evil_female_attack_1 = new ResourceLocation("mca:evil.female.attack.1");
	private static final ResourceLocation loc_evil_female_attack_2 = new ResourceLocation("mca:evil.female.attack.2");
	private static final ResourceLocation loc_evil_female_death_1 = new ResourceLocation("mca:evil.female.death.1");
	private static final ResourceLocation loc_evil_female_death_2 = new ResourceLocation("mca:evil.female.death.2");
	private static final ResourceLocation loc_evil_female_hurt_1 = new ResourceLocation("mca:evil.female.hurt.1");
	private static final ResourceLocation loc_evil_female_hurt_2 = new ResourceLocation("mca:evil.female.hurt.2");
	private static final ResourceLocation loc_evil_male_attack_1 = new ResourceLocation("mca:evil.male.attack.1");
	private static final ResourceLocation loc_evil_male_attack_2 = new ResourceLocation("mca:evil.male.attack.2");
	private static final ResourceLocation loc_evil_male_death_1 = new ResourceLocation("mca:evil.male.death.1");
	private static final ResourceLocation loc_evil_male_death_2 = new ResourceLocation("mca:evil.male.death.2");
	private static final ResourceLocation loc_evil_male_hurt_1 = new ResourceLocation("mca:evil.male.hurt.1");
	private static final ResourceLocation loc_evil_male_hurt_2 = new ResourceLocation("mca:evil.male.hurt.2");
	private static final ResourceLocation loc_heroic_female_attack_1 = new ResourceLocation(
			"mca:heroic.female.attack.1");
	private static final ResourceLocation loc_heroic_female_attack_2 = new ResourceLocation(
			"mca:heroic.female.attack.2");
	private static final ResourceLocation loc_heroic_female_death_1 = new ResourceLocation("mca:heroic.female.death.1");
	private static final ResourceLocation loc_heroic_female_death_2 = new ResourceLocation("mca:heroic.female.death.2");
	private static final ResourceLocation loc_heroic_female_hurt_1 = new ResourceLocation("mca:heroic.female.hurt.1");
	private static final ResourceLocation loc_heroic_female_hurt_2 = new ResourceLocation("mca:heroic.female.hurt.2");
	private static final ResourceLocation loc_heroic_male_attack_1 = new ResourceLocation("mca:heroic.male.attack.1");
	private static final ResourceLocation loc_heroic_male_attack_2 = new ResourceLocation("mca:heroic.male.attack.2");
	private static final ResourceLocation loc_heroic_male_death_1 = new ResourceLocation("mca:heroic.male.death.1");
	private static final ResourceLocation loc_heroic_male_death_2 = new ResourceLocation("mca:heroic.male.death.2");
	private static final ResourceLocation loc_heroic_male_hurt_1 = new ResourceLocation("mca:heroic.male.hurt.1");
	private static final ResourceLocation loc_heroic_male_hurt_2 = new ResourceLocation("mca:heroic.male.hurt.2");
	private static final ResourceLocation loc_villager_female_death_1 = new ResourceLocation(
			"mca:villager.female.death.1");
	private static final ResourceLocation loc_villager_female_death_2 = new ResourceLocation(
			"mca:villager.female.death.2");
	private static final ResourceLocation loc_villager_female_heh = new ResourceLocation("mca:villager.female.heh");
	private static final ResourceLocation loc_villager_female_hurt_1 = new ResourceLocation(
			"mca:villager.female.hurt.1");
	private static final ResourceLocation loc_villager_female_hurt_2 = new ResourceLocation(
			"mca:villager.female.hurt.2");
	private static final ResourceLocation loc_villager_male_death_1 = new ResourceLocation("mca:villager.male.death.1");
	private static final ResourceLocation loc_villager_male_death_2 = new ResourceLocation("mca:villager.male.death.2");
	private static final ResourceLocation loc_villager_male_heh = new ResourceLocation("mca:villager.male.heh");
	private static final ResourceLocation loc_villager_male_hurt_1 = new ResourceLocation("mca:villager.male.hurt.1");
	private static final ResourceLocation loc_villager_male_hurt_2 = new ResourceLocation("mca:villager.male.hurt.2");
	private static final ResourceLocation loc_malehurt = new ResourceLocation("mca:malehurt");

	public static final SoundEvent reaper_scythe_out = new SoundEvent(loc_reaper_scythe_out);
	public static final SoundEvent reaper_scythe_swing = new SoundEvent(loc_reaper_scythe_swing);
	public static final SoundEvent reaper_idle = new SoundEvent(loc_reaper_idle);
	public static final SoundEvent reaper_death = new SoundEvent(loc_reaper_death);
	public static final SoundEvent reaper_block = new SoundEvent(loc_reaper_block);
	public static final SoundEvent reaper_summon = new SoundEvent(loc_reaper_summon);

	public static final SoundEvent femalehurt = new SoundEvent(loc_femalehurt);
	public static final SoundEvent femalehurt1 = new SoundEvent(loc_femalehurt1);
	public static final SoundEvent femalehurt2 = new SoundEvent(loc_femalehurt2);
	public static final SoundEvent femalehurt3 = new SoundEvent(loc_femalehurt3);
	public static final SoundEvent femalehurt4 = new SoundEvent(loc_femalehurt4);
	public static final SoundEvent femalehurt5 = new SoundEvent(loc_femalehurt5);
	public static final SoundEvent femalehurt6 = new SoundEvent(loc_femalehurt6);
	public static final SoundEvent femaleattack1 = new SoundEvent(loc_femaleattack1);
	public static final SoundEvent femaleattack2 = new SoundEvent(loc_femaleattack2);
	public static final SoundEvent girl_villager_uhuh = new SoundEvent(loc_girl_villager_uhuh);

	public static final SoundEvent evil_female_attack_1 = new SoundEvent(loc_evil_female_attack_1);
	public static final SoundEvent evil_female_attack_2 = new SoundEvent(loc_evil_female_attack_2);
	public static final SoundEvent evil_female_death_1 = new SoundEvent(loc_evil_female_death_1);
	public static final SoundEvent evil_female_death_2 = new SoundEvent(loc_evil_female_death_2);
	public static final SoundEvent evil_female_hurt_1 = new SoundEvent(loc_evil_female_hurt_1);
	public static final SoundEvent evil_female_hurt_2 = new SoundEvent(loc_evil_female_hurt_2);
	public static final SoundEvent evil_male_attack_1 = new SoundEvent(loc_evil_male_attack_1);
	public static final SoundEvent evil_male_attack_2 = new SoundEvent(loc_evil_male_attack_2);
	public static final SoundEvent evil_male_death_1 = new SoundEvent(loc_evil_male_death_1);
	public static final SoundEvent evil_male_death_2 = new SoundEvent(loc_evil_male_death_2);
	public static final SoundEvent evil_male_hurt_1 = new SoundEvent(loc_evil_male_hurt_1);
	public static final SoundEvent evil_male_hurt_2 = new SoundEvent(loc_evil_male_hurt_2);
	public static final SoundEvent heroic_female_attack_1 = new SoundEvent(loc_heroic_female_attack_1);
	public static final SoundEvent heroic_female_attack_2 = new SoundEvent(loc_heroic_female_attack_2);
	public static final SoundEvent heroic_female_death_1 = new SoundEvent(loc_heroic_female_death_1);
	public static final SoundEvent heroic_female_death_2 = new SoundEvent(loc_heroic_female_death_2);
	public static final SoundEvent heroic_female_hurt_1 = new SoundEvent(loc_heroic_female_hurt_1);
	public static final SoundEvent heroic_female_hurt_2 = new SoundEvent(loc_heroic_female_hurt_2);
	public static final SoundEvent heroic_male_attack_1 = new SoundEvent(loc_heroic_male_attack_1);
	public static final SoundEvent heroic_male_attack_2 = new SoundEvent(loc_heroic_male_attack_2);
	public static final SoundEvent heroic_male_death_1 = new SoundEvent(loc_heroic_male_death_1);
	public static final SoundEvent heroic_male_death_2 = new SoundEvent(loc_heroic_male_death_2);
	public static final SoundEvent heroic_male_hurt_1 = new SoundEvent(loc_heroic_male_hurt_1);
	public static final SoundEvent heroic_male_hurt_2 = new SoundEvent(loc_heroic_male_hurt_2);
	public static final SoundEvent villager_female_death_1 = new SoundEvent(loc_villager_female_death_1);
	public static final SoundEvent villager_female_death_2 = new SoundEvent(loc_villager_female_death_2);
	public static final SoundEvent villager_female_hurt_1 = new SoundEvent(loc_villager_female_hurt_1);
	public static final SoundEvent villager_female_hurt_2 = new SoundEvent(loc_villager_female_hurt_2);
	public static final SoundEvent villager_male_death_1 = new SoundEvent(loc_villager_male_death_1);
	public static final SoundEvent villager_male_death_2 = new SoundEvent(loc_villager_male_death_2);
	public static final SoundEvent villager_female_heh = new SoundEvent(loc_villager_female_heh);
	public static final SoundEvent villager_male_hurt_1 = new SoundEvent(loc_villager_male_hurt_1);
	public static final SoundEvent villager_male_hurt_2 = new SoundEvent(loc_villager_male_hurt_2);
	public static final SoundEvent villager_male_heh = new SoundEvent(loc_villager_male_heh);
	public static final SoundEvent malehurt = new SoundEvent(loc_malehurt);

	private SoundsMCA() {
	};

	public static void register(RegistryEvent.Register<SoundEvent> event) {
		IForgeRegistry<SoundEvent> registry = event.getRegistry();
		reaper_scythe_out.setRegistryName(loc_reaper_scythe_out);
		reaper_scythe_swing.setRegistryName(loc_reaper_scythe_swing);
		reaper_idle.setRegistryName(loc_reaper_idle);
		reaper_death.setRegistryName(loc_reaper_death);
		reaper_block.setRegistryName(loc_reaper_block);
		reaper_summon.setRegistryName(loc_reaper_summon);

		girl_villager_uhuh.setRegistryName(loc_girl_villager_uhuh);
		femalehurt.setRegistryName(loc_femalehurt);
		femalehurt1.setRegistryName(loc_femalehurt1);
		femalehurt2.setRegistryName(loc_femalehurt2);
		femalehurt3.setRegistryName(loc_femalehurt3);
		femalehurt4.setRegistryName(loc_femalehurt4);
		femalehurt5.setRegistryName(loc_femalehurt5);
		femalehurt6.setRegistryName(loc_femalehurt6);
		femaleattack1.setRegistryName(loc_femaleattack1);
		femaleattack2.setRegistryName(loc_femaleattack2);
		malehurt.setRegistryName(loc_malehurt);
		evil_female_attack_1.setRegistryName(loc_evil_female_attack_1);
		evil_female_attack_2.setRegistryName(loc_evil_female_attack_2);
		evil_female_death_1.setRegistryName(loc_evil_female_death_1);
		evil_female_death_2.setRegistryName(loc_evil_female_death_2);
		evil_female_hurt_1.setRegistryName(loc_evil_female_hurt_1);
		evil_female_hurt_2.setRegistryName(loc_evil_female_hurt_2);
		evil_male_attack_1.setRegistryName(loc_evil_male_attack_1);
		evil_male_attack_2.setRegistryName(loc_evil_male_attack_2);
		evil_male_death_1.setRegistryName(loc_evil_male_death_1);
		evil_male_death_2.setRegistryName(loc_evil_male_death_2);
		evil_male_hurt_1.setRegistryName(loc_evil_male_hurt_1);
		evil_male_hurt_2.setRegistryName(loc_evil_male_hurt_2);
		heroic_female_attack_1.setRegistryName(loc_heroic_female_attack_1);
		heroic_female_attack_2.setRegistryName(loc_heroic_female_attack_2);
		heroic_female_death_1.setRegistryName(loc_heroic_female_death_1);
		heroic_female_death_2.setRegistryName(loc_heroic_female_death_2);
		heroic_female_hurt_1.setRegistryName(loc_heroic_female_hurt_1);
		heroic_female_hurt_2.setRegistryName(loc_heroic_female_hurt_2);
		heroic_male_attack_1.setRegistryName(loc_heroic_male_attack_1);
		heroic_male_attack_2.setRegistryName(loc_heroic_male_attack_2);
		heroic_male_death_1.setRegistryName(loc_heroic_male_death_1);
		heroic_male_death_2.setRegistryName(loc_heroic_male_death_2);
		heroic_male_hurt_1.setRegistryName(loc_heroic_male_hurt_1);
		heroic_male_hurt_2.setRegistryName(loc_heroic_male_hurt_2);
		villager_female_death_1.setRegistryName(loc_villager_female_death_1);
		villager_female_death_2.setRegistryName(loc_villager_female_death_2);
		villager_female_hurt_1.setRegistryName(loc_villager_female_hurt_1);
		villager_female_hurt_2.setRegistryName(loc_villager_female_hurt_2);
		villager_female_heh.setRegistryName(loc_villager_female_heh);
		villager_male_heh.setRegistryName(loc_villager_male_heh);
		villager_male_death_1.setRegistryName(loc_villager_male_death_1);
		villager_male_death_2.setRegistryName(loc_villager_male_death_2);
		villager_male_hurt_1.setRegistryName(loc_villager_male_hurt_1);
		villager_male_hurt_2.setRegistryName(loc_villager_male_hurt_2);

		registry.register(reaper_scythe_out);
		registry.register(reaper_scythe_swing);
		registry.register(reaper_idle);
		registry.register(reaper_death);
		registry.register(reaper_block);
		registry.register(reaper_summon);

		registry.register(femalehurt);
		registry.register(femalehurt1);
		registry.register(femalehurt2);
		registry.register(femalehurt3);
		registry.register(femalehurt4);
		registry.register(femalehurt5);
		registry.register(femalehurt6);
		registry.register(femaleattack1);
		registry.register(femaleattack2);
		registry.register(girl_villager_uhuh);
		registry.register(malehurt);
		registry.register(evil_female_attack_1);
		registry.register(evil_female_attack_2);
		registry.register(evil_female_death_1);
		registry.register(evil_female_death_2);
		registry.register(evil_female_hurt_1);
		registry.register(evil_female_hurt_2);
		registry.register(evil_male_attack_1);
		registry.register(evil_male_attack_2);
		registry.register(evil_male_death_1);
		registry.register(evil_male_death_2);
		registry.register(evil_male_hurt_1);
		registry.register(evil_male_hurt_2);
		registry.register(heroic_female_attack_1);
		registry.register(heroic_female_attack_2);
		registry.register(heroic_female_death_1);
		registry.register(heroic_female_death_2);
		registry.register(heroic_female_hurt_1);
		registry.register(heroic_female_hurt_2);
		registry.register(heroic_male_attack_1);
		registry.register(heroic_male_attack_2);
		registry.register(heroic_male_death_1);
		registry.register(heroic_male_death_2);
		registry.register(heroic_male_hurt_1);
		registry.register(heroic_male_hurt_2);
		registry.register(villager_female_death_1);
		registry.register(villager_female_death_2);
		registry.register(villager_female_heh);
		registry.register(villager_female_hurt_1);
		registry.register(villager_female_hurt_2);
		registry.register(villager_male_heh);
		registry.register(villager_male_death_1);
		registry.register(villager_male_death_2);
		registry.register(villager_male_hurt_1);
		registry.register(villager_male_hurt_2);
	}
}
