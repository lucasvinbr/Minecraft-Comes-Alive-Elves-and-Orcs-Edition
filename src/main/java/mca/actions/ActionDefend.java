package mca.actions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.SoundsMCA;
import mca.data.NBTPlayerData;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumBabyState;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumProfession;
import mca.enums.EnumProfessionSkinGroup;
import mca.enums.EnumProgressionStep;
import mca.enums.EnumRace;
import mca.items.ItemBaby;
import mca.util.Either;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

import static mca.core.Constants.EMPTY_UUID;

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
		if ((actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard ||
				actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Warrior ||
				actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Orc ||
				actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Elf) &&
				!actor.attributes.getIsInfected()) {
			if (target == null) {
				if (timeUntilTargetSearch <= 0) {
					tryAssignTarget();
					timeUntilTargetSearch = TARGET_SEARCH_INTERVAL;
				} else {
					timeUntilTargetSearch--;
				}
			} else if (target != null) {
				double distanceToTarget = RadixMath.getDistanceToEntity(actor, target);

				if (target.isDead || distanceToTarget >= 15.0D) {
					reset();
					return;
				}

				if (actor.attributes.getProfessionEnum() == EnumProfession.Archer) {
					actor.getLookHelper()
							.setLookPosition(target.posX,
									target.posY + (double) target.getEyeHeight(),
									target.posZ,
									10.0F,
									actor.getVerticalFaceSpeed());

					if (rangedAttackTime <= 0) {
						attackTargetWithRangedAttack(actor, 3F);
						actor.playSound(SoundEvents.ENTITY_SKELETON_SHOOT,
								1.0F,
								1.0F / (actor.getRNG().nextFloat() * 0.4F + 0.8F));
						rangedAttackTime = 60;
					} else {
						rangedAttackTime--;
					}
				} else {
					if (distanceToTarget <= 2.0F) {
						actor.swingItem();

						if (actor.onGround) {
							actor.motionY += 0.45F;
						}

						if(target instanceof EntityVillagerMCA) {
							EntityPlayer closestPlayer = actor.world.getClosestPlayerToEntity(actor, 500);
							EntityVillagerMCA mcaVillager = (EntityVillagerMCA) target;
							if (mcaVillager.attributes.getGender() == EnumGender.FEMALE) {

								World world = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
								if(actor.attributes.getGender() == EnumGender.MALE) {

									if (!mcaVillager.attributes.getIsChild() && !actor.attributes.getIsChild()) {
										if (mcaVillager.attributes.getBabyState() == EnumBabyState.NONE) {
											if (mcaVillager.attributes.getSpouseUUID() != this.actor.getUniqueID()) {
												actor.getJumpHelper().doJump();
												PotionEffect poison = new PotionEffect(Potion.getPotionById(19), 200);
												mcaVillager.addPotionEffect(poison);
												actor.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withL(mcaVillager));
												actor.sayRaw("Letz git hitch'd!", SoundEvents.ENTITY_VILLAGER_YES, closestPlayer);
												mcaVillager.addPotionEffect(poison);
												mcaVillager.sayRaw("Yikes!", closestPlayer);
											}

											mcaVillager.getJumpHelper().doJump();
											actor.swingArm(EnumHand.OFF_HAND);
											actor.getBehaviors()
													.getAction(ActionStoryProgression.class)
													.setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);


											actor.getBehaviors().getAction(ActionProcreate.class).onUpdateServer();
											actor.swingArm(EnumHand.OFF_HAND);
											mcaVillager.swingArm(EnumHand.MAIN_HAND);
											mcaVillager.getBehaviors()
													.getAction(ActionStoryProgression.class)
													.setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
											mcaVillager.getBehaviors()
													.getAction(ActionProcreate.class)
													.onUpdateClient();

											mcaVillager.sayRaw("Uff!", mcaVillager.attributes.getProfessionSkinGroup() ==
													                           EnumProfessionSkinGroup.Orc ?
											                           (new Random().nextBoolean() ?
											                            SoundsMCA.femalehurt5 :
											                            SoundsMCA.femalehurt6) :
											                           (new Random().nextBoolean() ?
											                            SoundsMCA.femalehurt2 :
											                            SoundsMCA.femalehurt4), closestPlayer);

											mcaVillager.swingArm(EnumHand.OFF_HAND);
											mcaVillager.getJumpHelper().doJump();
											if (RadixLogic.getBooleanWithProbability(33)) {
												boolean isMale = RadixLogic.getBooleanWithProbability(25);
												ItemStack babyStack = new ItemStack(isMale ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);
												ItemBaby baby = (ItemBaby) babyStack.getItem();
												baby.setFather(actor);
												baby.setMother(mcaVillager);
												mcaVillager.attributes.getInventory().addItem(babyStack);
												//											mcaVillager.setHeldItem(baby);
											}
										} else {
											if (mcaVillager.attributes.getSpouseUUID() == this.actor.getUniqueID()) {
												ItemStack bratStack = mcaVillager.attributes.getInventory().getBestItemOfType(ItemBaby.class);
												if (bratStack.getItem() instanceof ItemBaby) {
													ItemBaby itemBrat = (ItemBaby) bratStack.getItem();
													bratStack.getTagCompound().setInteger("age", MCA.getConfig().babyGrowUpTime);
													itemBrat.onUpdate(bratStack, world, mcaVillager, 1, false);
//													actor.sayRaw("Delivering baby.", closestPlayer);
													if(itemBrat.getIsBoy()) {
														PotionEffect strength = new PotionEffect(Potion.getPotionById(5), 50);
														mcaVillager.addPotionEffect(strength);
													} else {
														PotionEffect weakness = new PotionEffect(Potion.getPotionById(18), 50);
														mcaVillager.addPotionEffect(weakness);
													}
													itemBrat.onItemUseByVillager(mcaVillager,
															actor,
															world,
															bratStack,
															mcaVillager.getPosition());

													mcaVillager.sayRaw("...", SoundEvents.ENTITY_CHICKEN_EGG, closestPlayer);

//													PotionEffect damage = new PotionEffect(Potion.getPotionById(7), 50);
//													mcaVillager.addPotionEffect(damage);
													mcaVillager.swingArm(EnumHand.OFF_HAND);
												}
											}
										}
									}//end if we're both not children
								} else {
									ItemStack bratStack = mcaVillager.attributes.getInventory().getBestItemOfType(ItemBaby.class);
									if (bratStack.getItem() instanceof ItemBaby) {
										ItemBaby itemBrat = (ItemBaby) bratStack.getItem();
										bratStack.getTagCompound().setInteger("age", MCA.getConfig().babyGrowUpTime);
										itemBrat.onUpdate(bratStack, world, mcaVillager, 1, false);
//										actor.sayRaw("Delivering baby.", closestPlayer);
										itemBrat.onItemUseByVillager(mcaVillager,
												world,
												bratStack,
												mcaVillager.getPosition());

										mcaVillager.sayRaw("...", SoundEvents.ENTITY_CHICKEN_EGG, closestPlayer);

//										PotionEffect damage = new PotionEffect(Potion.getPotionById(7), 50);
//										mcaVillager.addPotionEffect(damage);
										mcaVillager.swingArm(EnumHand.OFF_HAND);
									}
								}
								if(actor.attributes.getGender() == EnumGender.MALE) {
									reset();
									return;
								}
								if(mcaVillager.attributes.getRaceEnum() != EnumRace.Elf && mcaVillager.attributes.getProfessionEnum() != EnumProfession.Guard && mcaVillager.attributes.getProfessionEnum() != EnumProfession.Archer) {
									reset();
									return;
								}
//								MCA.getLog().debug("End of if I'm male");
							}// end if female
//							MCA.getLog().debug("End of if target is female");
						}

						try {
//							if(actor.attributes.getRaceEnum() == EnumRace.Elf) {
//								if(actor.attributes.getGender() == EnumGender.FEMALE) {
//									actor.playSound(new Random().nextBoolean() ? SoundsMCA.heroic_female_attack_1 : SoundsMCA.heroic_female_attack_2, 1.0f, actor.getPitch());
//								} else {
//									actor.playSound(new Random().nextBoolean() ? SoundsMCA.heroic_male_attack_1 : SoundsMCA.heroic_male_attack_2, 1.0f, actor.getPitch());
//								}
//							} else if(actor.attributes.getRaceEnum() == EnumRace.Orc) {
//								if(actor.attributes.getGender() == EnumGender.FEMALE) {
//									actor.playSound(new Random().nextBoolean() ? SoundsMCA.evil_female_attack_1 : SoundsMCA.evil_female_attack_2, 1.0f, actor.getPitch());
//								} else {
//									actor.playSound(new Random().nextBoolean() ? SoundsMCA.evil_male_attack_1 : SoundsMCA.evil_male_attack_2, 1.0f, actor.getPitch());
//								}
//							}
							MCA.getLog().debug("Applying damage");
							int attackDamage = MCA.getConfig().villagerAttackDamage;
							if (actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard) {
								attackDamage = MCA.getConfig().guardAttackDamage;
							} else if (actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Orc) {
								attackDamage = MCA.getConfig().orcAttackDamage;
							} else if (actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Elf) {
								attackDamage = MCA.getConfig().elfAttackDamage;
							}
							if (actor.attributes.getIsChild()) {
								attackDamage = attackDamage / 2;
							}
							target.attackEntityFrom(DamageSource.causeMobDamage(actor), attackDamage);
						} catch (NullPointerException e) //Noticing a crash with the human mob mod.
						{
							reset();
						}
						if(/*target instanceof EntityOrcMCA ||*/ target instanceof EntityVillagerMCA && ((EntityVillagerMCA) target).attributes.getRaceEnum() == EnumRace.Orc) {
							if (RadixLogic.getBooleanWithProbability(33)) {
								EntityVillagerMCA punk = (EntityVillagerMCA) target;
								EntityPlayer closestPlayer = actor.world.getClosestPlayerToEntity(actor, 500);
								actor.sayRaw("Get outta here, ya lowzy git!", closestPlayer);
								reset();
							}

						}

					} else if (distanceToTarget > 2.0F && actor.getNavigator().noPath()) {
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
		List<EntityCreature> possibleTargets = new ArrayList<EntityCreature>();
		if (!actor.attributes.getIsChild()) {
			List<EntityMob> nearbyMobs = RadixLogic.getEntitiesWithinDistance(EntityMob.class, actor, 15);
			possibleTargets.addAll(nearbyMobs);
		}
		//		List<EntityCreature> possibleTargets = RadixLogic.getEntitiesWithinDistance(EntityCreature.class, actor, 15);
		double closestDistance = 100.0D;
		if (actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Orc) {
			closestDistance = 75.0d;
			//			PotionEffect strength = new PotionEffect(Potion.getPotionById(5), 200);
			//			logger.trace(String.format("Strength Effect: %s", strength.getEffectName()));
			//			actor.addPotionEffect(strength);
			List<EntityAnimal> animals = RadixLogic.getEntitiesWithinDistance(EntityAnimal.class, actor, 15);
			for (EntityAnimal animal : animals) {
				if (!(animal instanceof AbstractChestHorse) && !(animal instanceof EntityTameable)) {
					possibleTargets.add(animal);
				}
			}
			if (!actor.attributes.getIsChild()) {
				//Extra looping, but I'm not sure about how I would make it a mob, atm.  I
				List<EntityVillager> villagers = RadixLogic.getEntitiesWithinDistance(EntityVillager.class, actor, 15);
				for (EntityVillager villager : villagers) {
					if (villager instanceof EntityVillagerMCA) {
						EntityVillagerMCA mcaVillager = (EntityVillagerMCA) villager;

						if(mcaVillager.attributes.getIsInfected()) {
							possibleTargets.add(mcaVillager);
						} else if ((mcaVillager.attributes.getGender() == EnumGender.MALE &&
								!mcaVillager.attributes.getIsChild()) &&
								actor.attributes.getGender() == EnumGender.MALE) {
							if (RadixLogic.getBooleanWithProbability(1)) {

								EntityPlayer closestPlayer = actor.world.getClosestPlayerToEntity(actor, 500);
//								actor.sayRaw("Whacha lookin at?!", closestPlayer);
//								mcaVillager.sayRaw("Wha?  I wuzzint lookin at nuttin!", closestPlayer);
							    possibleTargets.add(mcaVillager);
							}
						} else {
							possibleTargets.add(mcaVillager);
							if (mcaVillager.attributes.getGender() == EnumGender.FEMALE) {
								if(actor.attributes.getGender() == EnumGender.MALE) {
									if (mcaVillager.attributes.getMarriageState() == EnumMarriageState.NOT_MARRIED) {
										EntityPlayer closestPlayer = actor.world.getClosestPlayerToEntity(actor, 500);
										actor.getJumpHelper().doJump();
//										PotionEffect poison = new PotionEffect(Potion.getPotionById(19), 200);
//										mcaVillager.addPotionEffect(poison);
										actor.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withL(mcaVillager));
//										actor.sayRaw("Letz git hitch'd!", SoundEvents.ENTITY_VILLAGER_YES, closestPlayer);
//										mcaVillager.addPotionEffect(poison);
//										mcaVillager.sayRaw("Yikes!", closestPlayer);
									}
								}
							}
						}
					} else {
						possibleTargets.add(villager);
					}
				}
			}
		} else if (actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Elf) {
			closestDistance = 125.0;
			//			PotionEffect speed = new PotionEffect(Potion.getPotionById(1), 200);
			//			logger.trace(String.format("Speed Effect: %s", speed.getEffectName()));
			//			actor.addPotionEffect(speed);
			if (!actor.attributes.getIsChild()) {
				List<EntityVillager> villagers = RadixLogic.getEntitiesWithinDistance(EntityVillager.class, actor, 15);
				for (EntityVillager villager : villagers) {
					if (villager instanceof EntityVillagerMCA &&
							((EntityVillagerMCA) villager).attributes.getProfessionSkinGroup() ==
									EnumProfessionSkinGroup.Orc) {
						//					PotionEffect slowness = new PotionEffect(Potion.getPotionById(2), 200);
						//					villager.addPotionEffect(slowness);
						possibleTargets.add(villager);
					}
				}
			}
		}
		for (Entity entity : possibleTargets) {
			//			if (!(entity instanceof EntityCreeper) && actor.canEntityBeSeen(entity)) {
			if (actor.canEntityBeSeen(entity)) {
				double distance = RadixMath.getDistanceToEntity(actor, entity);
				if (distance < closestDistance) {
					//					closestDistance = distance;
					if (!(entity instanceof EntityCreeper)) {
						target = (EntityLiving) entity;
					} else if ((actor.getProfession() == EnumProfession.Archer.getId() ||
							actor.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Elf)) {
						target = (EntityLiving) entity;
					}
				}
			}
		}

	}

	private void attackTargetWithRangedAttack(EntityVillagerMCA shooter, float velocity) {
		EntityArrow entityarrow = new EntityTippedArrow(shooter.world, shooter);
		double d0 = target.posX - shooter.posX;
		double d1 = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - entityarrow.posY;
		double d2 = target.posZ - shooter.posZ;
		double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
		entityarrow.setThrowableHeading(d0,
				d1 + d3 * 0.2D,
				d2,
				1.6F,
				(float) (14 - shooter.world.getDifficulty().getDifficultyId() * 4));
		int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, shooter);
		int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, shooter);
		entityarrow.setDamage((double) (velocity * 2.0F) +
				shooter.getRNG().nextGaussian() * 0.25D +
				(double) ((float) shooter.world.getDifficulty().getDifficultyId() * 0.11F));

		if (i > 0) {
			entityarrow.setDamage(entityarrow.getDamage() + (double) i * 0.5D + 0.5D);
		}

		if (j > 0) {
			entityarrow.setKnockbackStrength(j);
		}

		shooter.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (shooter.getRNG().nextFloat() * 0.4F + 0.8F));
		shooter.world.spawnEntity(entityarrow);
	}
}
