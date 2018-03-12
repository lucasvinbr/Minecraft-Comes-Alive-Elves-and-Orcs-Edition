package mca.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.enums.EnumProfession;
import mca.enums.EnumProfessionSkinGroup;
import mca.enums.EnumRace;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import radixcore.constant.Time;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

public class ActionDefend extends AbstractAction {
	private static final int TARGET_SEARCH_INTERVAL = Time.SECOND * 1;
	private static Logger logger = LogManager.getLogger(ActionDefend.class);
	private EntityLiving target;
	private int timeUntilTargetSearch;
	private int rangedAttackTime;

	public ActionDefend(EntityVillagerMCA actor) {
		super(actor);
	}

	@Override
	public void onUpdateServer() {
		if (actor.getBehavior(ActionSleep.class).getIsSleeping()) {
			return;
		}
		if ((actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard
				|| actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Warrior
				|| actor.attributes.getRaceEnum() == EnumRace.Orc || actor.attributes.getRaceEnum() == EnumRace.Elf)
				&& !actor.attributes.getIsInfected()) {
			if (target == null) {
				if (timeUntilTargetSearch <= 0) {
					tryAssignTarget();
					timeUntilTargetSearch = TARGET_SEARCH_INTERVAL;
				}
				else {
					timeUntilTargetSearch--;
				}
			}
			else if (target != null) {
				double distanceToTarget = RadixMath.getDistanceToEntity(actor, target);

				if (target.isDead || distanceToTarget >= 15.0D) {
					reset();
					return;
				}

				if (actor.attributes.getProfessionEnum() == EnumProfession.Archer
						|| (actor.attributes.getRaceEnum() == EnumRace.Elf
								&& actor.attributes.getGender() == EnumGender.FEMALE)) {
					actor.getLookHelper().setLookPosition(target.posX, target.posY + (double) target.getEyeHeight(),
							target.posZ, 10.0F, actor.getVerticalFaceSpeed());

					if (rangedAttackTime <= 0) {
						attackTargetWithRangedAttack(actor, 3F);
						actor.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F,
								1.0F / (actor.getRNG().nextFloat() * 0.4F + 0.8F));
						rangedAttackTime = 60;
					}
					else {
						rangedAttackTime--;
					}
				}
				else {
					if (distanceToTarget <= 2.0F) {
						actor.swingItem();

						if (actor.onGround) {
							actor.motionY += 0.45F;
						}
						try {
							if (target instanceof EntityVillagerMCA) {
								EntityVillagerMCA mcaVillager = (EntityVillagerMCA) target;
								// if (mcaVillager.attributes.getGender() == EnumGender.FEMALE
								// && actor.attributes.getRaceEnum() == EnumRace.Orc) {
								// if (actor.attributes.getIsMale()) {
								// // Utilities.spawnParticlesAroundEntityC(EnumParticleTypes.HEART, actor, 10);
								// // Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.HEART, actor, 10);
								// Utilities.spawnParticlesAroundPointS(EnumParticleTypes.HEART,
								// actor.getWorld(),
								// actor.getPositionVector().x, actor.getPositionVector().y,
								// actor.getPositionVector().z, 10);
								// if (MCA.isOrcMatingSeason()) {
								// actor.swingArm(EnumHand.OFF_HAND);
								// Utilities.spawnParticlesAroundPointS(EnumParticleTypes.CRIT,
								// actor.getWorld(), mcaVillager.getPositionVector().x,
								// mcaVillager.getPositionVector().y,
								// mcaVillager.getPositionVector().z, 10);
								// actor.mate(mcaVillager);
								// }
								// else {
								// actor.swingArm(EnumHand.OFF_HAND);
								//
								// Utilities.spawnParticlesAroundPointS(EnumParticleTypes.CLOUD,
								// actor.getWorld(), mcaVillager.getPositionVector().x,
								// mcaVillager.getPositionVector().y,
								// mcaVillager.getPositionVector().z, 10);
								// mcaVillager.lieDown();
								// mcaVillager.getBehavior(ActionSleep.class).transitionSkinState(true);
								// }
								// reset();
								// return;
								// }
								// else {
								// if (!mcaVillager.attributes.getIsInfected()
								// && (mcaVillager.attributes.getRaceEnum() != EnumRace.Elf
								// && !mcaVillager.isFighter())) {
								// reset();
								// return;
								// }
								// }
								// }
								((EntityVillagerMCA) target).playHurtSound();
							}
							int attackDamage = MCA.getConfig().villagerAttackDamage;
							if (actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard) {
								attackDamage = MCA.getConfig().guardAttackDamage;
							}
							else if (actor.attributes.getRaceEnum() == EnumRace.Orc) {
								attackDamage = MCA.getConfig().orcAttackDamage;
							}
							else if (actor.attributes.getRaceEnum() == EnumRace.Elf) {
								attackDamage = MCA.getConfig().elfAttackDamage;
							}
							if (actor.attributes.getRaceEnum() == EnumRace.Orc
									&& actor.attributes.getGender() == EnumGender.MALE
									&& target instanceof EntityVillagerMCA
									&& ((EntityVillagerMCA) target).attributes.getGender() == EnumGender.FEMALE) {
								attackDamage = 0;
								reset();
							}
							if (actor.attributes.getIsChild()) {
								attackDamage = attackDamage / 2;
							}
							actor.swingArm(EnumHand.MAIN_HAND);
							target.attackEntityFrom(DamageSource.causeMobDamage(actor), attackDamage);
						}
						catch (NullPointerException e) // Noticing a crash with the human mob mod.
						{
							reset();
						}
						// if (/* target instanceof EntityOrcMCA || */ target instanceof
						// EntityVillagerMCA
						// && ((EntityVillagerMCA) target).attributes.getRaceEnum() == EnumRace.Orc) {
						// if (RadixLogic.getBooleanWithProbability(33)) {
						// EntityVillagerMCA punk = (EntityVillagerMCA) target;
						// EntityPlayer closestPlayer = actor.world.getClosestPlayerToEntity(actor,
						// 500);
						// actor.sayRaw("Get outta here, ya lowzy git!", closestPlayer);
						// reset();
						// }
						// }
					}
					else if (distanceToTarget > 2.0F && actor.getNavigator().noPath()) {
						actor.getNavigator().tryMoveToEntityLiving(target, Constants.SPEED_RUN);

					}
				}
			}
		}
	}

	@Override
	public void reset() {
		target = null;
		rangedAttackTime = 0;
	}

	private void tryAssignTarget() {
		List<EntityLivingBase> possibleTargets = new ArrayList<EntityLivingBase>();
		if (!actor.attributes.getIsChild()) {
			List<EntityMob> nearbyMobs = RadixLogic.getEntitiesWithinDistance(EntityMob.class, actor, 15);
			possibleTargets.addAll(nearbyMobs);
		}
		else {
			if (actor.getPet() != null) {
				EntityMob monster = (EntityMob) RadixLogic.getClosestEntityExclusive(actor, 15, EntityMob.class);
				actor.getPet().setAttackTarget(monster);
			}
		}
		// List<EntityCreature> possibleTargets =
		// RadixLogic.getEntitiesWithinDistance(EntityCreature.class, actor, 15);
		double closestDistance = 100.0D;
		if (actor.attributes.getRaceEnum() == EnumRace.Orc) {
			closestDistance = 75.0d;
			// PotionEffect strength = new PotionEffect(Potion.getPotionById(5), 200);
			// logger.trace(String.format("Strength Effect: %s", strength.getEffectName()));
			// actor.addPotionEffect(strength);

			if (!actor.attributes.getIsChild()) {
				List<EntityVillager> villagers = RadixLogic.getEntitiesWithinDistance(EntityVillager.class, actor, 15);
				for (EntityVillager villager : villagers) {
					if (villager instanceof EntityVillagerMCA) {
						EntityVillagerMCA mcaVillager = (EntityVillagerMCA) villager;
						if (mcaVillager.attributes.getRaceEnum() != EnumRace.Orc) {
							if (!mcaVillager.attributes.isMarriedToAnOrc()) {
								if (mcaVillager.attributes.getRaceEnum() != EnumRace.Elf || !EntityVillagerMCA
										.isProfessionSkinFighter(mcaVillager.attributes.getProfessionSkinGroup())) {
									if (mcaVillager.attributes.getGender() == EnumGender.FEMALE
											|| (RadixLogic.getBooleanWithProbability(50))) {
										 mcaVillager.flee();
									}
								}
								if (mcaVillager.attributes.getIsInfected()
										|| mcaVillager.attributes.getGender() != EnumGender.FEMALE
										|| (mcaVillager.attributes.getRaceEnum() == EnumRace.Elf
												|| EntityVillagerMCA.isProfessionSkinFighter(
														mcaVillager.attributes.getProfessionSkinGroup()))) {
									possibleTargets.add(mcaVillager);
								}
							}
						}
					}

				}
			}
			else {
				List<EntityAnimal> animals = RadixLogic.getEntitiesWithinDistance(EntityAnimal.class, actor, 15);
				for (EntityAnimal animal : animals) {
					if (!(animal instanceof AbstractChestHorse) && !(animal instanceof EntityTameable)) {
						possibleTargets.add(animal);
					}
				}
				if (actor.getPet() != null) {
					EntityAnimal animal = animals.get(RadixMath.getNumberInRange(0, animals.size() - 1));
					actor.getPet().setAttackTarget(animal);
				}
			}
		}
		else if (actor.attributes.getRaceEnum() == EnumRace.Elf) {
			closestDistance = 125.0;
			// PotionEffect speed = new PotionEffect(Potion.getPotionById(1), 200);
			// logger.trace(String.format("Speed Effect: %s", speed.getEffectName()));
			// actor.addPotionEffect(speed);
		}
		if (actor.attributes.getRaceEnum() == EnumRace.Elf || (actor.attributes.getRaceEnum() == EnumRace.Villager
				&& (actor.attributes.getProfessionEnum() == EnumProfession.Archer
						|| actor.attributes.getProfessionEnum() == EnumProfession.Guard
						|| actor.attributes.getProfessionEnum() == EnumProfession.Warrior))) {
			if (!actor.attributes.getIsChild()) {
				List<EntityVillagerMCA> villagers = RadixLogic.getEntitiesWithinDistance(EntityVillagerMCA.class, actor,
						50);
				for (EntityVillagerMCA villager : villagers) {
					if (villager.attributes.getRaceEnum() == EnumRace.Orc && !actor.attributes.isMarriedToAnOrc()) {
						// PotionEffect slowness = new PotionEffect(Potion.getPotionById(2), 200);
						// villager.addPotionEffect(slowness);
						if (actor.attributes.getRaceEnum() == EnumRace.Elf && villager.attributes.isMarriedToAnElf()) {
							continue;
						}
						possibleTargets.add(villager);
					}
				}
			}
		}
		// if (actor.attributes.getProfessionEnum() == EnumProfession.Archer
		// || actor.attributes.getProfessionEnum() == EnumProfession.Guard
		// || actor.attributes.getProfessionEnum() == EnumProfession.Warrior) {
		// EntityPlayer closestPlayer = actor.world.getClosestPlayerToEntity(actor,
		// closestDistance);
		// NBTPlayerData data = MCA.getPlayerData(closestPlayer);
		// if (data.getRace() != actor.attributes.getRaceEnum()) {
		// possibleTargets.add(closestPlayer);
		// }
		// }
		for (Entity entity : possibleTargets) {
			// if (!(entity instanceof EntityCreeper) && actor.canEntityBeSeen(entity)) {
			if (actor.canEntityBeSeen(entity)) {
				double distance = RadixMath.getDistanceToEntity(actor, entity);
				if (distance < closestDistance) {
					closestDistance = distance;
					if (!(entity instanceof EntityCreeper)) {
						target = (EntityLiving) entity;
					}
					else if ((actor.getProfession() == EnumProfession.Archer.getId()
							|| actor.attributes.getRaceEnum() == EnumRace.Elf)) {
						target = (EntityLiving) entity;
					}
				}
			}
		}
	}

	private void attackTargetWithRangedAttack(EntityVillagerMCA shooter, float velocity) {
		if (actor.getPet() != null) {
			actor.getPet().setAttackTarget(target);
		}
		EntityArrow entityarrow = new EntityTippedArrow(shooter.world, shooter);
		double d0 = target.posX - shooter.posX;
		double d1 = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - entityarrow.posY;
		double d2 = target.posZ - shooter.posZ;
		double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
		entityarrow.setThrowableHeading(d0, d1 + d3 * 0.2D, d2, 1.6F,
				(float) (14 - shooter.world.getDifficulty().getDifficultyId() * 4));
		int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, shooter);
		int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, shooter);
		entityarrow.setDamage((double) (velocity * 2.0F) + shooter.getRNG().nextGaussian() * 0.25D
				+ (double) ((float) shooter.world.getDifficulty().getDifficultyId() * 0.11F));

		if (i > 0) {
			entityarrow.setDamage(entityarrow.getDamage() + (double) i * 0.5D + 0.5D);
		}

		if (j > 0) {
			entityarrow.setKnockbackStrength(j);
		}
		actor.swingArm(EnumHand.OFF_HAND);
		shooter.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (shooter.getRNG().nextFloat() * 0.4F + 0.8F));
		shooter.world.spawnEntity(entityarrow);
	}
}
