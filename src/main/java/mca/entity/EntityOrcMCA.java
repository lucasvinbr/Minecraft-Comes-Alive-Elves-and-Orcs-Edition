package mca.entity;

import mca.enums.EnumProfession;
import mca.enums.EnumRace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;

import static net.minecraftforge.fml.common.ObfuscationReflectionHelper.getPrivateValue;
import static net.minecraftforge.fml.common.ObfuscationReflectionHelper.setPrivateValue;

public class EntityOrcMCA extends EntityVillagerMCA {
	public EntityOrcMCA(World world) {
		super(world);
		maxSwingProgressTicks = 10;
		//		this.attributes.setProfession(EnumProfession.Orc);
		this.attributes.setProfession(EnumProfession.Warrior);
		this.attributes.setRace(EnumRace.Orc);
	}

	@Override
	public void say(String phraseId, EntityPlayer target, Object... arguments) {
		if (target == null) {
			return;
		}
//		String zombieMoan = RadixLogic.getBooleanWithProbability(33) ? "Raagh..." :
//				RadixLogic.getBooleanWithProbability(33) ? "Ughh..." : "Argh-gur...";
//		target.sendMessage(new TextComponentString(attributes.getTitle(target) + ": " + zombieMoan));
		this.playSound(SoundEvents.ENTITY_VILLAGER_NO, 0.5F, pitch);
	}

	@Override
	public void say(String phraseId, EntityPlayer target) {
		say(phraseId, target, this, target);
	}
//
//	/**
//	 * Sets the given entity to be the spouse of the current villager. This is symmetric against the provided entity.
//	 * If null is provided, this villager's spouse information will be reset. This is **NOT** symmetric.
//	 *
//	 * @param either Either object containing an MCA villager or a player.
//	 */
//	@Override
//	public void startMarriage(Either<EntityVillagerMCA, EntityPlayer> either) {
//		if (either.getLeft() != null) {
//			EntityVillagerMCA spouse = either.getLeft();
//			attributes.setSpouseName(spouse.attributes.getName());
//			attributes.setSpouseUUID(spouse.getUniqueID());
//			attributes.setSpouseGender(spouse.attributes.getGender());
//			attributes.setMarriageState(EnumMarriageState.MARRIED_TO_VILLAGER);
//			spouse.attributes.setSpouseName(this.attributes.getName());
//			spouse.attributes.setSpouseUUID(this.getUniqueID());
//			spouse.attributes.setSpouseGender(this.attributes.getGender());
//			spouse.attributes.setMarriageState(EnumMarriageState.MARRIED_TO_VILLAGER);
//			getBehaviors().onMarriageToVillager();
////			if(spouse.attributes.getGender() == EnumGender.FEMALE && this.attributes.getGender() == EnumGender.MALE) {
////
////			}
//		} else if (either.getRight() != null) {
//			EntityPlayer player = either.getRight();
//			NBTPlayerData playerData = MCA.getPlayerData(player);
//			PlayerMemory memory = attributes.getPlayerMemory(player);
//
//			attributes.setSpouseName(player.getName());
//			attributes.setSpouseUUID(player.getUniqueID());
//			attributes.setSpouseGender(playerData.getGender());
//			attributes.setMarriageState(EnumMarriageState.MARRIED_TO_PLAYER);
//			memory.setDialogueType(EnumDialogueType.SPOUSE);
//			memory.setRelation(attributes.getGender() == EnumGender.MALE ? EnumRelation.HUSBAND : EnumRelation.WIFE);
//
//			playerData.setSpouseName(this.getName());
//			playerData.setSpouseGender(attributes.getGender());
//			playerData.setSpouseUUID(this.getUniqueID());
//			playerData.setMarriageState(EnumMarriageState.MARRIED_TO_VILLAGER);
//
//			getBehaviors().onMarriageToPlayer();
//		} else {
//			throw new IllegalArgumentException("Marriage target cannot be null");
//		}
//	}

	//	public ItemStack getHeldItem(EnumHand hand) {
	//		EnumBabyState babyState = attributes.getBabyState();
	//		EnumProfession profession = attributes.getProfessionEnum();
	//
	//		if (babyState != EnumBabyState.NONE) {
	//			return new ItemStack(babyState == EnumBabyState.MALE ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);
	//		} else if (attributes.isMarriedToAPlayer() ||
	//				profession == EnumProfession.Child) {
	//			return getBehavior(ActionCombat.class).getHeldItem();
	//		}
	//		return new ItemStack(Items.STONE_SWORD);
	//	}
	//
	//	private <T, E> void setEntityVillagerField(int fieldIndex, Object value) {
	//		setPrivateValue(EntityVillager.class, this, value, fieldIndex);
	//	}
	//
	//	private <T, E> T getEntityVillagerField(int fieldIndex) {
	//		return getPrivateValue(EntityVillager.class, this, fieldIndex);
	//	}
}
