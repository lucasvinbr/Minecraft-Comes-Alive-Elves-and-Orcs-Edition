/**
 * 
 */
package mca.actions;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mca.core.Constants;
import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumBabyState;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumRace;
import mca.util.Utilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import radixcore.constant.Time;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

/**
 * @author Michael M. Adkins
 *
 */
public class ActionMate extends AbstractToggleAction {
	// private static final DataParameter<Boolean>
	// IS_MATING =
	// EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class,
	// DataSerializers.BOOLEAN);
	private static Logger logger = LogManager.getLogger(ActionMate.class);
	private static final int TARGET_SEARCH_INTERVAL = Time.MINUTE;
	// private static Logger logger = LogManager.getLogger(ActionMate.class);
	private EntityLiving mate;
	private int timeUntilTargetSearch;
	private int timeUntilTick = 0;

	public ActionMate(EntityVillagerMCA actor) {
		super(actor);
	}

	@Override
	public void onUpdateServer() {
		if (actor.getBehavior(ActionSleep.class).getIsSleeping()) {
			return;
		}
		if (timeUntilTick > 0) {
			timeUntilTick--;
			return;
		}
		else {
			timeUntilTick = 20;
		}
		if (timeUntilTargetSearch <= 0) {
			timeUntilTargetSearch = TARGET_SEARCH_INTERVAL;
			setIsActive(MCA.isMyRacesMatingSeason(actor.attributes.getRaceEnum()));
		}
		else {
			timeUntilTargetSearch--;
		}

		if (actor.getBehavior(ActionSleep.class).getIsSleeping()) {
			return;
		}
		if (MCA.isMyRacesMatingSeason(actor.attributes.getRaceEnum())) {
			if (mate != null) {
				double distanceToTarget = RadixMath.getDistanceToEntity(actor, mate);
				if (distanceToTarget <= 2.0F) {
					actor.swingItem();

					if (actor.onGround) {
						actor.motionY += 0.45F;
					}
					if (actor.attributes.getRaceEnum() == EnumRace.Orc) {
						// Utilities.spawnParticlesAroundPointS(EnumParticleTypes.HEART,
						// actor.getWorld(),
						// actor.getPositionVector().x, actor.getPositionVector().y,
						// actor.getPositionVector().z,
						// 10);
						if (mate instanceof EntityVillagerMCA) {
							EntityVillagerMCA partner = (EntityVillagerMCA) mate;
							if (partner.attributes.getGender() == EnumGender.FEMALE) {
								if (partner.attributes.getBabyState() == EnumBabyState.NONE) {
									// Utilities.spawnParticlesAroundPointS(EnumParticleTypes.HEART,
									// actor.getWorld(),
									// actor.getPositionVector().x, actor.getPositionVector().y,
									// actor.getPositionVector().z, 10);
									actor.swingArm(EnumHand.OFF_HAND);
									Utilities.spawnParticlesAroundPointS(EnumParticleTypes.CRIT, actor.getWorld(),
											mate.getPositionVector().x, mate.getPositionVector().y,
											mate.getPositionVector().z, 10);
									actor.mate(partner);
								}
								else {
									reset();
								}
							}
							else {
								if (actor.attributes.getBabyState() == EnumBabyState.NONE) {
									partner.mate(actor);
								}
								else {
									reset();
									setIsActive(false);
								}
							}
						}
					}
				}
				else if (distanceToTarget > 2.0F && actor.getNavigator().noPath()) {
					actor.getNavigator().tryMoveToEntityLiving(mate, Constants.SPEED_RUN);

				}
			}
			else {
				tryAssignMate();
			}
		}
		else {
			setIsActive(false);
			return;
		}

	}

	@Override
	public void reset() {
		mate = null;
	}

	private void tryAssignMate() {
		double closestDistance = 32.0D;
		List<EntityVillagerMCA> possibleTargets = RadixLogic.getEntitiesWithinDistance(EntityVillagerMCA.class, actor,
				32);
		if (!actor.attributes.getIsChild()) {
			if (actor.attributes.getGender() == EnumGender.MALE) {
				// EntityVillagerMCA partner = (EntityVillagerMCA)
				// RadixLogic.getClosestEntityExclusive(actor, 32, EntityVillagerMCA.class);
				for (EntityVillagerMCA partner : possibleTargets) {
					if (partner.canEntityBeSeen(actor)) {
						boolean partnerIsValid =
								partner.attributes.getGender() != actor.attributes.getGender() &&
										partner.attributes.getMarriageState() != EnumMarriageState.MARRIED_TO_ORC &&
										!partner.attributes.getIsChild()
						/*
						 * && (partner.attributes.getFatherUUID() != actor.attributes.getFatherUUID())
						 * && (partner.attributes.getMotherUUID() != actor.attributes.getMotherUUID())
						 */;
						double distance = RadixMath.getDistanceToEntity(actor, partner);
						if (partnerIsValid && distance < closestDistance) {
							mate = partner;
							closestDistance = distance;
						}
					}
				}

			}
			else {
				mate = actor.attributes.getVillagerSpouseInstance();
			}
		}
	}

	@Override
	public String getName() {
		return actor.getName();
	}
}
