package mca.entity;

import io.netty.buffer.ByteBuf;
import mca.actions.AbstractAction;
import mca.actions.ActionAttackResponse;
import mca.actions.ActionCombat;
import mca.actions.ActionSleep;
import mca.actions.ActionUpdateMood;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.data.TransitiveVillagerData;
import mca.enums.EnumBabyState;
import mca.enums.EnumDialogueType;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumMovementState;
import mca.enums.EnumProfession;
import mca.enums.EnumProfessionSkinGroup;
import mca.enums.EnumRace;
import mca.enums.EnumRelation;
import mca.items.ItemBaby;
import mca.items.ItemMemorial;
import mca.items.ItemVillagerEditor;
import mca.packets.PacketOpenGUIOnEntity;
import mca.util.Either;
import mca.util.Utilities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.apache.logging.log4j.LogManager;
import radixcore.constant.Font;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;

import java.util.ArrayList;
import java.util.List;

import static mca.core.Constants.EMPTY_UUID;
import static net.minecraftforge.fml.common.ObfuscationReflectionHelper.getPrivateValue;
import static net.minecraftforge.fml.common.ObfuscationReflectionHelper.setPrivateValue;

public class EntityOrcMCA extends EntityVillagerMCA {
	public EntityOrcMCA(World world) {
		super(world);
		maxSwingProgressTicks = 10;
//		this.attributes.setProfession(EnumProfession.Orc);
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
		this.playSound(SoundEvents.ENTITY_VILLAGER_NO, 0.5F, rand.nextFloat() + 0.5F);
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
