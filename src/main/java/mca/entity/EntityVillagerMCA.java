package mca.entity;

import static mca.core.Constants.EMPTY_UUID;
import static net.minecraftforge.fml.common.ObfuscationReflectionHelper.getPrivateValue;
import static net.minecraftforge.fml.common.ObfuscationReflectionHelper.setPrivateValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.buffer.ByteBuf;
import mca.actions.AbstractAction;
import mca.actions.ActionAttackResponse;
import mca.actions.ActionCombat;
import mca.actions.ActionMate;
import mca.actions.ActionSleep;
import mca.actions.ActionStoryProgression;
import mca.actions.ActionUpdateMood;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.SoundsMCA;
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
import mca.enums.EnumProgressionStep;
import mca.enums.EnumRace;
import mca.enums.EnumRelation;
import mca.enums.EnumSleepingState;
import mca.items.ItemBaby;
import mca.items.ItemMemorial;
import mca.items.ItemTombstone;
import mca.items.ItemVillagerEditor;
import mca.packets.PacketOpenGUIOnEntity;
import mca.tile.TileTombstone;
import mca.util.Either;
import mca.util.Utilities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.EntityDataManager;
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
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.constant.Font.Color;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.modules.RadixBlocks;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;

/**
 * The main class of MCA's villager. The class itself handles events, getters,
 * setters, etc. overridden from Minecraft. Also any events/actions that can be
 * performed on a villager.
 * <p>
 * To avoid an absurdly large class, the rest of the villager is split into 2
 * components:
 * <p>
 * The VillagerBehaviors object handles custom villager behaviors that run each
 * tick.
 * <p>
 * The VillagerAttributes object holds all villager data and their
 * getters/setters.
 */
public class EntityVillagerMCA extends EntityVillager implements IEntityAdditionalSpawnData {
	@SideOnly(Side.CLIENT)
	public boolean isInteractionGuiOpen;
	private static Logger logger = LogManager.getLogger(EntityVillagerMCA.class);
	private int swingProgressTicks;
	public final VillagerAttributes attributes;
	protected final VillagerBehaviors behaviors;
	private final Profiler profiler;
	protected int maxSwingProgressTicks = 8;
	// Used for hooking into vanilla trades
	private int vanillaProfessionId;
	private static final int FIELD_INDEX_BUYING_PLAYER = 6;
	private static final int FIELD_INDEX_TIME_UNTIL_RESET = 8;
	private static final int FIELD_INDEX_NEEDS_INITIALIZATION = 9;
	private static final int FIELD_INDEX_IS_WILLING_TO_MATE = 10;
	private static final int FIELD_INDEX_WEALTH = 11;
	private static final int FIELD_INDEX_LAST_BUYING_PLAYER = 12;
	private int raceId = -1;
	protected float pitch = 1.0f;
	protected int deathCountDown = 3 * Time.SECOND;
	protected DamageSource damageSource; // for when they die. I'm going to have a death clock countdown.
	protected EntityTameable pet;
	private Entity ridingEntity;
	protected Random random = new Random();

	public EntityVillagerMCA(World world) {
		super(world);
		profiler = world.profiler;
		attributes = new VillagerAttributes(this);
		attributes.initialize();
		behaviors = new VillagerBehaviors(this);
		this.raceId = 0;
		this.attributes.setRace(EnumRace.Villager);
		this.attributes.setIsBeingChased(false);
		if (this.attributes.getGender() == EnumGender.FEMALE) {
			pitch = RadixMath.getNumberInRange(1.0f, 1.3f);
		}
		else {
			pitch = RadixMath.getNumberInRange(0.7f, 1.0f);
		}
		// pitch = (random.nextInt((13 - 7) + 1) + 7) / 10;
		addAI();
	}

	public static boolean isProfessionSkinFighter(EnumProfessionSkinGroup professionSkinGroup) {
		return professionSkinGroup == EnumProfessionSkinGroup.Guard
				|| professionSkinGroup == EnumProfessionSkinGroup.Warrior;
	}

	public void addAI() {
		this.tasks.taskEntries.clear();

		((PathNavigateGround) this.getNavigator()).setCanSwim(true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
		this.tasks.addTask(4, new EntityAIOpenDoor(this, true));

		int maxHealth = isProfessionSkinFighter(attributes.getProfessionSkinGroup())
				|| attributes.getRace() == EnumRace.Orc ? MCA.getConfig().guardMaxHealth
						: MCA.getConfig().villagerMaxHealth;
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
		if (this.getHealth() > maxHealth || isProfessionSkinFighter(attributes.getProfessionSkinGroup())) {
			this.setHealth(maxHealth);
		}
		if (!isProfessionSkinFighter(attributes.getProfessionSkinGroup()) || attributes.getRace() == EnumRace.Elf
				|| attributes.getRace() == EnumRace.Orc) {
			this.tasks.addTask(2, new EntityAIMoveIndoors(this));
		}
		behaviors.getAction(ActionMate.class)
				.setIsActive(MCA.getConfig().getSeasonalBreeders().contains(attributes.getRace()));
		// behaviors.getAction(ActionRetreat.class).setIsActive(false);
	}

	private void updateSwinging() {
		if (attributes.getIsSwinging()) {
			swingProgressTicks++;

			if (swingProgressTicks >= maxSwingProgressTicks) {
				swingProgressTicks = 0;
				attributes.setIsSwinging(false);
			}
		}
		else {
			swingProgressTicks = 0;
		}
		swingProgress = (float) swingProgressTicks / (float) maxSwingProgressTicks;
	}

	@Override
	public void onStruckByLightning(EntityLightningBolt entity) {
		this.setDead();
		MCA.naturallySpawnWitches(attributes.getGender(), new Point3D(this.posX, this.posY, this.posZ), world);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		profiler.startSection("MCA Villager Update");
		behaviors.onUpdate();
		updateSwinging();

		if (!world.isRemote) {
			attributes.incrementTicksAlive();

			// Tick player memories
			for (PlayerMemory memory : attributes.getPlayerMemories().values()) {
				memory.doTick();
			}

			// Tick babies in attributes.getInventory().
			for (int i = 0; i < attributes.getInventory().getSizeInventory(); i++) {
				ItemStack stack = attributes.getInventory().getStackInSlot(i);

				if (stack.getItem() instanceof ItemBaby) {
					ItemBaby item = (ItemBaby) stack.getItem();
					item.onUpdate(stack, world, this, 1, false);
				}
			}

			// Check if inventory should be opened for player.
			if (attributes.getDoOpenInventory()) {
				final EntityPlayer player = world.getClosestPlayerToEntity(this, 10.0D);

				if (player != null) {
					player.openGui(MCA.getInstance(), Constants.GUI_ID_INVENTORY, world, (int) posX, (int) posY,
							(int) posZ);
				}

				attributes.setDoOpenInventory(false);
			}
			// if (attributes.getRaceEnum() == EnumRace.Orc) {
			// behaviors.getAction(ActionMate.class).setIsActive(MCA.isOrcMatingSeason());
			// }
		}
		if (this.pet != null && !this.pet.isDead) {
			if (pet.getNavigator().noPath()) {
				pet.setSitting(false);
				pet.getNavigator().tryMoveToEntityLiving(this, Constants.SPEED_WALK);
			}
			pet.setGrowingAge(this.getGrowingAge());
		}
		if (this.dead) {
			if (deathCountDown >= 0) {
				deathCountDown--;
				Utilities.spawnParticlesAroundPointS(EnumParticleTypes.CLOUD, world, posX, posY, posZ, 10);
			}
			else {
				try {
					super.onDeath(damageSource);
				}
				catch (Exception e) {
					String msg = String.format("Exception occurred!%nMessage: %s%n", e.getLocalizedMessage());
					FMLLog.severe(msg, e);
					java.util.logging.LogManager.getLogManager().getLogger(this.getClass().getName()).severe(msg);
					org.apache.logging.log4j.LogManager.getLogger(this.getClass().getName()).error(msg, e);
					java.util.logging.Logger.getLogger(this.getClass().getName()).severe(msg);
				}
			}
		}
		profiler.endSection();
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (getRidingEntity() == player) // Dismounts from a player on right-click
		{
			dismountRidingEntity();
			dismountEntity(player);
			return true;
		}

		if (!world.isRemote) {
			ItemStack heldItem = player.getHeldItem(hand);
			Item item = heldItem.getItem();

			if (player.capabilities.isCreativeMode && item instanceof ItemMemorial && !heldItem.hasTagCompound()) {
				TransitiveVillagerData transitiveData = new TransitiveVillagerData(attributes);
				NBTTagCompound stackNBT = new NBTTagCompound();
				stackNBT.setUniqueId("ownerUUID", player.getUniqueID());
				stackNBT.setString("ownerName", player.getName());
				stackNBT.setInteger("relation", attributes.getPlayerMemory(player).getRelation().getId());
				transitiveData.writeToNBT(stackNBT);

				heldItem.setTagCompound(stackNBT);

				this.setDead();
			}
			else {
				int guiId = item instanceof ItemVillagerEditor ? Constants.GUI_ID_EDITOR : Constants.GUI_ID_INTERACT;
				MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenGUIOnEntity(this.getEntityId(), guiId), player);
			}
		}

		return true;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		// return attributes.getIsInfected() ? SoundEvents.ENTITY_ZOMBIE_HURT : null;
		return getHurtSound();
	}

	protected SoundEvent getHurtSound() {
		if (attributes.getIsInfected()) {
			return SoundEvents.ENTITY_ZOMBIE_HURT;
		}
		else {
			if (this.attributes.getRace() == EnumRace.Elf) {
				if (this.attributes.getGender() == EnumGender.FEMALE) {
					return random.nextBoolean() ? SoundsMCA.heroic_female_hurt_1 : SoundsMCA.heroic_female_hurt_2;
				}
				else {
					return random.nextBoolean() ? SoundsMCA.heroic_male_hurt_1 : SoundsMCA.heroic_male_hurt_2;
				}
			}
			else if (this.attributes.getRace() == EnumRace.Orc) {
				if (this.attributes.getGender() == EnumGender.FEMALE) {
					return random.nextBoolean() ? SoundsMCA.evil_female_hurt_1 : SoundsMCA.evil_female_hurt_2;
				}
				else {
					return random.nextBoolean() ? SoundsMCA.evil_male_hurt_1 : SoundsMCA.evil_male_hurt_2;
				}
			}
			else {
				if (this.attributes.getGender() == EnumGender.FEMALE) {
					return random.nextBoolean() ? SoundsMCA.villager_female_hurt_1
							: SoundsMCA.villager_female_hurt_2;
				}
				else {
					return random.nextBoolean() ? SoundsMCA.villager_male_hurt_1 : SoundsMCA.villager_male_hurt_2;
				}
			}
		}
	}

	public void playHurtSound() {
		this.playSound(getHurtSound(), 1.0f, pitch);
	}

	public void lieDown() {
		Point3D movePoint = new Point3D(this.getPositionVector().x, this.getPositionVector().y,
				this.getPositionVector().z);
		Point3D point = Utilities.movePointToGround(this, movePoint);
		this.attemptTeleport(point.dX(), point.dY(), point.dZ());
		this.getBehavior(ActionSleep.class).setIsInBed(true);
	}

	public void riseOutOfBed() {
		this.getBehavior(ActionSleep.class).setIsInBed(false);
	}

	public void toggleLieDown() {
		this.getBehavior(ActionSleep.class).setIsInBed(this.getBehavior(ActionSleep.class).getIsInBed());
	}

	public void mate(EntityVillagerMCA mate) {
		if (mate.attributes.getSpouseUUID() != this.getUniqueID()) {

			this.facePosition(new Point3D(mate.getPos().getX(), mate.getPos().getY(), mate.getPos().getZ()));
			// this.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withL(mate));
			Utilities.spawnParticlesAroundPointS(EnumParticleTypes.HEART, this.getWorld(), this.getPositionVector().x,
					this.getPositionVector().y, this.getPositionVector().z, 3);
			this.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withL(mate));
			mate.flee();
		}
		else if (mate.attributes.getGender() == EnumGender.FEMALE) {

			// mate.getJumpHelper().doJump();
			// this.swingArm(EnumHand.OFF_HAND);

			mate.facePosition(new Point3D(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()));
			mate.playSound(
					mate.attributes.getRace() == EnumRace.Orc
							? (random.nextBoolean() ? SoundsMCA.femalehurt5 : SoundsMCA.femalehurt6)
							: (random.nextBoolean() ? SoundsMCA.femalehurt2 : SoundsMCA.femalehurt4),
					1.0f, mate.getPitch());
			mate.rotationYawHead += 40;
			Utilities.spawnParticlesAroundPointS(EnumParticleTypes.VILLAGER_HAPPY, mate.getWorld(),
					mate.getPositionVector().x, mate.getPositionVector().y, mate.getPositionVector().z, 10);
			this.rotationYawHead += 40;
			Utilities.spawnParticlesAroundPointS(EnumParticleTypes.VILLAGER_HAPPY, world, posX, posY, posZ, 10);
			this.rotationPitch += 40;
			this.getBehaviors().getAction(ActionStoryProgression.class)
					.setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);
			mate.getBehaviors().getAction(ActionStoryProgression.class)
					.setProgressionStep(EnumProgressionStep.TRY_FOR_BABY);

			if (mate.attributes.getRace() == EnumRace.Orc) {
				mate.playSound((random.nextBoolean() ? SoundsMCA.femalehurt5 : SoundsMCA.femalehurt6), 1.0f,
						mate.getPitch());
			}
			else {
				mate.playSound(random.nextBoolean() ? SoundsMCA.femalehurt2 : SoundsMCA.femalehurt4, 1.0f,
						mate.getPitch());
			}

			for (int i = 0; i < 2; i++) {
				if (RadixLogic.getBooleanWithProbability(50)) {
					boolean isMale = random.nextBoolean();
					ItemStack babyStack = new ItemStack(isMale ? ItemsMCA.BABY_BOY_ORC : ItemsMCA.BABY_GIRL_ORC);
					ItemBaby baby = (ItemBaby) babyStack.getItem();
					baby.setFather(this);
					baby.setMother(mate);
					mate.attributes.getInventory().addItem(babyStack);
					mate.attributes.setBabyState(isMale ? EnumBabyState.MALE : EnumBabyState.FEMALE);
					mate.getBehavior(ActionStoryProgression.class).setForceNextProgress(true);
					mate.getVillagerInventory().addItem(babyStack);
					mate.setHeldItem(baby);
					// baby.setMaxStackSize(baby.getItemStackLimit()+1);
				}
			}

			if (mate.attributes.getBabyState() != EnumBabyState.NONE) {
				this.lieDown();
				mate.lieDown();
				getBehavior(ActionSleep.class).transitionSkinState(true);
				mate.getBehavior(ActionSleep.class).setSleepingState(EnumSleepingState.SLEEPING);
				mate.getBehavior(ActionSleep.class).transitionSkinState(true);
				Utilities.spawnParticlesAroundPointS(EnumParticleTypes.CRIT, mate.getWorld(),
						mate.getPositionVector().x, mate.getPositionVector().y, mate.getPositionVector().z, 300);
			}
			else {
				mate.riseOutOfBed();
			}
			if (mate.attributes.getRace() == EnumRace.Orc) {
				mate.riseOutOfBed();
			}
			this.riseOutOfBed();
		}
	}

	public void playDeathSound() {
		this.playSound(getDeathSound(), 1.0f, pitch);
	}

	@Override
	protected SoundEvent getDeathSound() {
		if (attributes.getIsInfected()) {
			return SoundEvents.ENTITY_ZOMBIE_DEATH;
		}
		if (attributes.getRace() == EnumRace.Elf) {
			if (attributes.getGender() == EnumGender.FEMALE) {
				return random.nextBoolean() ? SoundsMCA.heroic_female_death_1 : SoundsMCA.heroic_female_death_2;
			}
			else {
				return random.nextBoolean() ? SoundsMCA.heroic_male_death_1 : SoundsMCA.heroic_male_death_2;
			}
		}
		if (attributes.getRace() == EnumRace.Orc) {
			if (attributes.getGender() == EnumGender.FEMALE) {
				return random.nextBoolean() ? SoundsMCA.evil_female_death_1 : SoundsMCA.evil_female_death_2;
			}
			else {
				return random.nextBoolean() ? SoundsMCA.evil_male_death_1 : SoundsMCA.evil_male_death_2;
			}
		}
		if (attributes.getGender() == EnumGender.FEMALE) {
			return random.nextBoolean() ? SoundsMCA.villager_female_death_1 : SoundsMCA.villager_female_death_2;
		}
		else {
			return random.nextBoolean() ? SoundsMCA.villager_male_death_1 : SoundsMCA.villager_male_death_2;
		}
	}

	@Override
	public void onDeath(DamageSource damageSource) {

		this.damageSource = damageSource;
		if (!world.isRemote) {
			ItemTombstone tombstone = new ItemTombstone();
//			EntityDataManager data = getDataManager();
//			List<String> stats = new ArrayList<String>();
//			if(data.getAll() != null) {
//				for (int i = 0; i < data.getAll().size() - 1; i++) {
//					if (data.getAll().get(i) != null) {
//						String value = data.getAll().get(i).toString();
//						stats.add(value);
//					}
//				}
//			}
			ItemStack tombStack = new ItemStack(tombstone);
			//tombstone.addInformation(tombStack, world, stats, ITooltipFlag.TooltipFlags.NORMAL);
			getVillagerInventory().addItem(tombStack);
			playDeathSound();
			// Switch to the sleeping skin and disable all chores/toggle AIs so they won't
			// move
			behaviors.disableAllToggleActions();
			getBehavior(ActionSleep.class).transitionSkinState(true);

			// The death of a villager negatively modifies the mood of nearby villagers
			for (EntityVillagerMCA human : RadixLogic.getEntitiesWithinDistance(EntityVillagerMCA.class, this, 20)) {
				human.getBehavior(ActionUpdateMood.class).modifyMoodLevel(-2.0F);
			}

			// Drop all items in the inventory
			for (int i = 0; i < attributes.getInventory().getSizeInventory(); i++) {
				ItemStack stack = attributes.getInventory().getStackInSlot(i);

				if (stack != null) {
					entityDropItem(stack, 1.0F);
				}
			}

			// Reset the marriage stats of the player/villager this one was married to
			// If married to a player, this player takes priority in receiving the memorial
			// item for revival.
			boolean memorialDropped = false;

			if (attributes.isMarriedToAPlayer()) {
				NBTPlayerData playerData = MCA.getPlayerData(world, attributes.getSpouseUUID());

				playerData.setMarriageState(EnumMarriageState.NOT_MARRIED);
				playerData.setSpouseName("");
				playerData.setSpouseUUID(EMPTY_UUID);

				// Just in case something is added here later, be sure we're not false
				if (!memorialDropped) {
					createMemorialChest(attributes.getPlayerMemoryWithoutCreating(attributes.getSpouseUUID()),
							ItemsMCA.BROKEN_RING);
					memorialDropped = true;
				}
			}
			else {
				EntityVillagerMCA partner = attributes.getVillagerSpouseInstance();
				if (partner != null) {
					partner.getVillagerInventory().addItem(tombStack);
					partner.endMarriage();
				}
			}
			// Alert parents/spouse of the death if they are online and handle dropping
			// memorials
			// Test against new iteration of player memory list each time to ensure the
			// proper order
			// of handling notifications and memorial spawning

			for (PlayerMemory memory : attributes.getPlayerMemories().values()) {
				// Alert parents and spouse of the death.
				if (memory.getUUID().equals(attributes.getSpouseUUID())
						|| attributes.isPlayerAParent(memory.getUUID())) {
					EntityPlayer player = world.getPlayerEntityByUUID(memory.getUUID());

					// If we hit a parent
					if (attributes.isPlayerAParent(memory.getUUID()) && !memorialDropped) {
						createMemorialChest(memory,
								attributes.getGender() == EnumGender.MALE ? ItemsMCA.TOY_TRAIN : ItemsMCA.CHILDS_DOLL);
						memorialDropped = true;
					}

					if (player != null) {
						// The player may not be online
						player.sendMessage(
								new TextComponentString(Color.RED + attributes.getTitle(player) + " has died."));
					}
				}
			}
			if (!memorialDropped && attributes.isImportant()) {
				getVillagerInventory().addItem(tombStack);
				createTombstone(tombStack);
			}
			if (pet != null) {
				pet.setTamed(false);
				pet.setSitting(false);
				pet.setOwnerId(Constants.EMPTY_UUID);
				// if (attributes.getRace() == EnumRace.Orc) {
				// pet.isDead = true;
				// }
				if (pet instanceof EntityWolf) {
					((EntityWolf) pet).setCollarColor(EnumDyeColor.BLACK);
				}
			}
			this.dead = true;
		}

		try {
			super.onDeath(damageSource);
		}
		catch (Exception e) {
			String msg = String.format("Exception occurred!%nMessage: %s%n", e.getLocalizedMessage());
			FMLLog.severe(msg, e);
			java.util.logging.LogManager.getLogManager().getLogger(this.getClass().getName()).severe(msg);
			org.apache.logging.log4j.LogManager.getLogger(this.getClass().getName()).error(msg, e);
			java.util.logging.Logger.getLogger(this.getClass().getName()).severe(msg);
		}
	}

	private void createTombstone(ItemStack tombStack) {
		Point3D nearestWater = RadixLogic.getNearestBlock(this, 3, Blocks.WATER);
		Point3D nearestAir = RadixLogic.getNearestBlock(this, 3, Blocks.AIR);
		Point3D blockPoint = null;
		if (nearestAir == null && nearestWater == null) {
			logger.warn("No available location to spawn villager tombstone for " + this.getName());
		}
		else {
			if (nearestWater != null) {
				blockPoint = nearestWater;
			}
			else {
				blockPoint = nearestAir;
				int y = blockPoint.iY();
				Block block = Blocks.AIR;

				while (block == Blocks.AIR) {
					y--;
					block = world.getBlockState(new BlockPos(blockPoint.iX(), y, blockPoint.iZ())).getBlock();
				}

				y += 1;
				world.setBlockState(new BlockPos(blockPoint.iX(), y, blockPoint.iZ()),
						BlocksMCA.tombstone.getDefaultState());

				try {
					TileTombstone tomb = (TileTombstone) world.getTileEntity(blockPoint.toBlockPos());
					if (tomb != null) {
						tomb.signText[1] = new TextComponentString(
								RadixLogic.getBooleanWithProbability(50) ? MCA.getLocalizer().getString("name.male")
										: MCA.getLocalizer().getString("name.female"));
						tomb.signText[2] = new TextComponentString("RIP");
						RadixBlocks.setBlock(world,
								new Point3D(tomb.getPos().getX(), tomb.getPos().getY(), tomb.getPos().getZ()),
								BlocksMCA.tombstone);
					}
					TransitiveVillagerData data = new TransitiveVillagerData(attributes);

					NBTTagCompound stackNBT = new NBTTagCompound();

					stackNBT.setString("ownerName", data.getName());
					stackNBT.setUniqueId("ownerUUID", data.getUUID());
					data.writeToNBT(stackNBT);
					tombStack.setTagCompound(stackNBT);
					getVillagerInventory().addItem(tombStack);
					LogManager.getLogger(EntityVillagerMCA.class).info(
							"Spawned villager death chest at: " + blockPoint.iX() + ", " + y + ", " + blockPoint.iZ());
				}
				catch (Exception e) {
					LogManager.getLogger(EntityVillagerMCA.class)
							.error("Error spawning villager death chest: " + e.getMessage());
				}
			}
		}
	}

	private void createMemorialChest(PlayerMemory memory, ItemMemorial memorialItem) {
		Point3D nearestAir = RadixLogic.getNearestBlock(this, 3, Blocks.AIR);

		if (nearestAir == null) {
			logger.warn("No available location to spawn villager death chest for " + this.getName());
		}
		else {
			int y = nearestAir.iY();
			Block block = Blocks.AIR;

			while (block == Blocks.AIR) {
				y--;
				block = world.getBlockState(new BlockPos(nearestAir.iX(), y, nearestAir.iZ())).getBlock();
			}

			y += 1;
			world.setBlockState(new BlockPos(nearestAir.iX(), y, nearestAir.iZ()), Blocks.CHEST.getDefaultState());

			try {
				TileEntityChest chest = (TileEntityChest) world.getTileEntity(nearestAir.toBlockPos());
				TransitiveVillagerData data = new TransitiveVillagerData(attributes);
				ItemStack memorialStack = new ItemStack(memorialItem);
				NBTTagCompound stackNBT = new NBTTagCompound();

				stackNBT.setString("ownerName", memory.getPlayerName());
				stackNBT.setUniqueId("ownerUUID", memory.getUUID());
				stackNBT.setInteger("ownerRelation", memory.getRelation().getId());
				data.writeToNBT(stackNBT);
				memorialStack.setTagCompound(stackNBT);

				if (chest != null) {
					chest.setInventorySlotContents(0, memorialStack);
				}
				LogManager.getLogger(EntityVillagerMCA.class).info(
						"Spawned villager death chest at: " + nearestAir.iX() + ", " + y + ", " + nearestAir.iZ());
			}
			catch (Exception e) {
				LogManager.getLogger(EntityVillagerMCA.class)
						.error("Error spawning villager death chest: " + e.getMessage());
			}
		}
	}

	@Override
	protected void updateAITasks() {
		ActionSleep sleepAI = getBehavior(ActionSleep.class);
		EnumMovementState moveState = attributes.getMovementState();
		boolean isSleeping = sleepAI.getIsSleeping();

		if (isSleeping) {
			// Minecraft 1.8 moved the execution of tasks out of updateAITasks and into
			// EntityAITasks.updateTasks().
			// Get the 'tickCount' value per tick and set it to 1 when we don't want tasks
			// to execute. This prevents
			// The AI tasks from ever triggering an update.
			ObfuscationReflectionHelper.setPrivateValue(EntityAITasks.class, tasks, 1, 4);
		}

		if (!isSleeping && (moveState == EnumMovementState.MOVE || moveState == EnumMovementState.FOLLOW)) {
			super.updateAITasks();
		}

		if (moveState == EnumMovementState.STAY && !isSleeping) {
			tasks.onUpdateTasks();
			getLookHelper().onUpdateLook();
		}

		if (moveState == EnumMovementState.STAY || isSleeping) {
			getNavigator().clearPathEntity();
		}
	}

	@Override
	protected void damageEntity(DamageSource damageSource, float damageAmount) {
		super.damageEntity(damageSource, damageAmount);

		behaviors.getAction(ActionAttackResponse.class).startResponse(damageSource.getImmediateSource());
		behaviors.getAction(ActionSleep.class).onDamage();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		behaviors.writeToNBT(nbt);
		attributes.writeToNBT(nbt);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		behaviors.readFromNBT(nbt);
		attributes.readFromNBT(nbt);
		addAI();
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		attributes.writeSpawnData(buffer);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		try {
			attributes.readSpawnData(buffer);
		}
		catch (Exception e) {
			// System.out.println("Reader Index: " + buffer.readerIndex());
			// System.out.println("Writer Index: " + buffer.writerIndex());
			// TODO Auto-generated catch block
			String msg = String.format("Exception occurred!%nMessage: %s%n", e.getLocalizedMessage());
			FMLLog.warning(msg, e);
			java.util.logging.LogManager.getLogManager().getLogger(this.getClass().getName()).warning(msg);
			org.apache.logging.log4j.LogManager.getLogger(this.getClass().getName()).warn(msg);
			java.util.logging.Logger.getLogger(this.getClass().getName()).warning(msg);
		}
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	public boolean canBePushed() {
		final ActionSleep sleepAI = behaviors.getAction(ActionSleep.class);
		return !sleepAI.getIsSleeping();
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	public void sayRaw(String text, EntityPlayer target) {
		final StringBuilder sb = new StringBuilder();

		if (MCA.getConfig().villagerChatPrefix != null && !MCA.getConfig().villagerChatPrefix.equals("null")) {
			sb.append(MCA.getConfig().villagerChatPrefix);
		}

		if (target != null) {
			sb.append(attributes.getTitle(target));
		}
		sb.append(": ");
		sb.append(text);

		if (target != null) {
			target.sendMessage(new TextComponentString(sb.toString()));
		}

		behaviors.onSay();
	}

	public void sayRaw(String text, SoundEvent sound, EntityPlayer target) {
		sayRaw(text, target);
		this.playSound(sound, 1.0F, pitch);
	}

	public void say(String phraseId, EntityPlayer target, Object... arguments) {
		if (target == null) {
			return;
		}

		if (attributes.getIsInfected()) // Infected villagers moan when they speak, and will not say anything else.
		{
			String zombieMoan = RadixLogic.getBooleanWithProbability(33) ? "Raagh..."
					: RadixLogic.getBooleanWithProbability(33) ? "Ughh..." : "Argh-gur...";
			target.sendMessage(new TextComponentString(attributes.getTitle(target) + ": " + zombieMoan));
			this.playSound(SoundEvents.ENTITY_ZOMBIE_AMBIENT, 0.5F, pitch + 0.5F);
		}
		else {
			final StringBuilder sb = new StringBuilder();

			// Handle chat prefix.
			if (MCA.getConfig().villagerChatPrefix != null && !MCA.getConfig().villagerChatPrefix.equals("null")) {
				sb.append(MCA.getConfig().villagerChatPrefix);
			}

			// Add title and text.
			sb.append(attributes.getTitle(target));
			sb.append(": ");
			sb.append(MCA.getLocalizer().getString(phraseId, arguments));

			target.sendMessage(new TextComponentString(sb.toString()));

			behaviors.onSay();
		}
	}

	public void say(String phraseId, EntityPlayer target) {
		say(phraseId, target, this, target);
	}

	/**
	 * Sets the given entity to be the spouse of the current villager. This is
	 * symmetric against the provided entity. If null is provided, this villager's
	 * spouse information will be reset. This is **NOT** symmetric.
	 *
	 * @param either
	 *            Either object containing an MCA villager or a player.
	 */
	public void startMarriage(Either<EntityVillagerMCA, EntityPlayer> either) {
		if (either.getLeft() != null) {
			EntityVillagerMCA spouse = either.getLeft();

			attributes.setSpouseName(spouse.attributes.getName());
			attributes.setSpouseUUID(spouse.getUniqueID());
			attributes.setSpouseGender(spouse.attributes.getGender());

			if (spouse.attributes.getRace() == EnumRace.Elf) {
				attributes.setMarriageState(EnumMarriageState.MARRIED_TO_ELF);
			}
			else if (spouse.attributes.getRace() == EnumRace.Orc) {
				attributes.setMarriageState(EnumMarriageState.MARRIED_TO_ORC);
			}
			else {
				attributes.setMarriageState(EnumMarriageState.MARRIED_TO_VILLAGER);
			}

			spouse.attributes.setSpouseName(this.attributes.getName());
			spouse.attributes.setSpouseUUID(this.getUniqueID());
			spouse.attributes.setSpouseGender(this.attributes.getGender());
			if (attributes.getRace() == EnumRace.Elf) {
				spouse.attributes.setMarriageState(EnumMarriageState.MARRIED_TO_ELF);
			}
			else if (attributes.getRace() == EnumRace.Orc) {
				spouse.attributes.setMarriageState(EnumMarriageState.MARRIED_TO_ORC);
			}
			else {
				spouse.attributes.setMarriageState(EnumMarriageState.MARRIED_TO_VILLAGER);
			}

			getBehaviors().onMarriageToVillager(spouse);
		}
		else if (either.getRight() != null) {
			EntityPlayer player = either.getRight();
			NBTPlayerData playerData = MCA.getPlayerData(player);
			PlayerMemory memory = attributes.getPlayerMemory(player);

			attributes.setSpouseName(player.getName());
			attributes.setSpouseUUID(player.getUniqueID());
			attributes.setSpouseGender(playerData.getGender());
			attributes.setMarriageState(EnumMarriageState.MARRIED_TO_PLAYER);
			memory.setDialogueType(EnumDialogueType.SPOUSE);
			memory.setRelation(attributes.getGender() == EnumGender.MALE ? EnumRelation.HUSBAND : EnumRelation.WIFE);

			playerData.setSpouseName(this.getName());
			playerData.setSpouseGender(attributes.getGender());
			playerData.setSpouseUUID(this.getUniqueID());
			if (attributes.getRace() == EnumRace.Elf) {
				playerData.setMarriageState(EnumMarriageState.MARRIED_TO_ELF);
			}
			else if (attributes.getRace() == EnumRace.Orc) {
				playerData.setMarriageState(EnumMarriageState.MARRIED_TO_ORC);
			}
			else {
				playerData.setMarriageState(EnumMarriageState.MARRIED_TO_VILLAGER);
			}

			getBehaviors().onMarriageToPlayer();
		}
		else {
			throw new IllegalArgumentException("Marriage target cannot be null");
		}
	}

	public List<Point3D> findDoors(int maxDistance) {
		List<Point3D> doors = new ArrayList<Point3D>();
		List<Point3D> oakDoors = RadixLogic.getNearbyBlocks(this, Blocks.OAK_DOOR, maxDistance);
		List<Point3D> darkOakDoors = RadixLogic.getNearbyBlocks(this, Blocks.DARK_OAK_DOOR, maxDistance);
		List<Point3D> birchDoors = RadixLogic.getNearbyBlocks(this, Blocks.BIRCH_DOOR, maxDistance);
		List<Point3D> spruceDoors = RadixLogic.getNearbyBlocks(this, Blocks.SPRUCE_DOOR, maxDistance);
		doors.addAll(oakDoors);
		doors.addAll(darkOakDoors);
		doors.addAll(birchDoors);
		doors.addAll(spruceDoors);

		for (Point3D door : doors) {
			// Point3D door = doors.get(RadixMath.getNumberInRange(0, doors.size() - 1));
			// Only use the top of the door.
			if (Utilities.blockIsADoor(RadixBlocks.getBlock(this.world, door.iX(), door.iY() - 1, door.iZ()))) {
				door.set(door.iX(), door.iY() + 1, door.iZ());
			}
		}
		return doors;
	}

	public void endMarriage() {
		// Reset spouse information back to default
		attributes.setSpouseName("");
		attributes.setSpouseUUID(EMPTY_UUID);
		attributes.setSpouseGender(EnumGender.UNASSIGNED);
		attributes.setMarriageState(EnumMarriageState.NOT_MARRIED);

		getBehaviors().onMarriageEnded();
	}

	public void halt() {
		getNavigator().clearPathEntity();

		moveForward = 0.0F;
		moveStrafing = 0.0F;
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
	}

	public void facePosition(Point3D position) {
		double midX = position.dX() - this.posX;
		double midZ = position.dZ() - this.posZ;
		double d1 = 0;

		double d3 = MathHelper.sqrt(midX * midX + midZ * midZ);
		float f2 = (float) (Math.atan2(midZ, midX) * 180.0D / Math.PI) - 90.0F;
		float f3 = (float) (-(Math.atan2(d1, d3) * 180.0D / Math.PI));
		this.rotationPitch = this.updateRotation(this.rotationPitch, f3, 16.0F);
		this.rotationYaw = this.updateRotation(this.rotationYaw, f2, 16.0F);
	}

	private float updateRotation(float p_70663_1_, float p_70663_2_, float p_70663_3_) {
		float f3 = MathHelper.wrapDegrees(p_70663_2_ - p_70663_1_);

		if (f3 > p_70663_3_) {
			f3 = p_70663_3_;
		}

		if (f3 < -p_70663_3_) {
			f3 = -p_70663_3_;
		}

		return p_70663_1_ + f3;
	}

	public VillagerBehaviors getBehaviors() {
		return behaviors;
	}

	public <T extends AbstractAction> T getBehavior(Class<T> clazz) {
		return this.behaviors.getAction(clazz);
	}

	@Override
	public ItemStack getHeldItem(EnumHand hand) {
		EnumBabyState babyState = attributes.getBabyState();
		EnumProfession profession = attributes.getProfessionEnum();
		EnumRace race = attributes.getRace();
		ItemStack itemStack = ItemStack.EMPTY;
		if (babyState != EnumBabyState.NONE) {
			// VillagerInventory inventory = this.attributes.getInventory();
			// for (int i = 0; i < inventory.getSizeInventory(); i++) {
			// inventory.getStackInSlot(i);
			// Item item = inventory.getBestItemOfType(ItemBaby.class).getItem();
			// if (item.getClass() == ItemBaby.class) {
			// ItemBaby itemBrat = (ItemBaby) item;
			// }
			// }
			if (attributes.isMarriedToAnOrc()) {
				itemStack = new ItemStack(
						babyState == EnumBabyState.MALE ? ItemsMCA.BABY_BOY_ORC : ItemsMCA.BABY_GIRL_ORC);
			}
			else {
				itemStack = new ItemStack(babyState == EnumBabyState.MALE ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);
			}
			// if(attributes.getInventory().contains(ItemBaby.class)) {
			// itemStack = attributes.getInventory().getBestItemOfType(ItemBaby.class);
			// } else {
		}
		else if (attributes.getInventory().contains(ItemBaby.class)) {
			int slot = attributes.getInventory().getFirstSlotContainingItem(ItemsMCA.BABY_BOY);
			slot = slot == -1 ? attributes.getInventory().getFirstSlotContainingItem(ItemsMCA.BABY_GIRL) : slot;

			if (slot != -1) {
				itemStack = attributes.getInventory().getStackInSlot(slot);
			}
		}
		else if (attributes.getIsInfected()) {
			itemStack = ItemStack.EMPTY;
		}
		else if (race == EnumRace.Villager) {
			if (profession == EnumProfession.Guard || profession == EnumProfession.Warrior) {
				itemStack = new ItemStack(Items.IRON_SWORD);
			}
			else if (profession == EnumProfession.Archer) {
				itemStack = new ItemStack(Items.BOW);
			}
			else if (attributes.getHeldItemSlot() != -1 && behaviors.isToggleActionActive()) {
				itemStack = attributes.getInventory().getStackInSlot(attributes.getHeldItemSlot());
			}
			/*
			 * else if (profession == EnumProfession.Shepherd) { itemStack = new
			 * ItemStack(Items.SHEARS); } else if (profession == EnumProfession.Fisherman) {
			 * itemStack = new ItemStack(Items.FISHING_ROD); } else if (profession ==
			 * EnumProfession.Farmer) { itemStack = new ItemStack(Items.IRON_HOE); }
			 */
			// else if (profession == EnumProfession.Miner) {
			// itemStack = new ItemStack(Items.IRON_PICKAXE);
			// }
		}
		else if (race == EnumRace.Orc) {
			itemStack = new ItemStack(Items.STONE_SWORD);
			Enchantment knockback = Enchantment.getEnchantmentByID(19);
			itemStack.addEnchantment(knockback, knockback != null ? knockback.getMaxLevel() : 0);
		}
		else if (race == EnumRace.Elf) {
			if (attributes.getGender() == EnumGender.FEMALE) {
				itemStack = new ItemStack(Items.BOW);
				Enchantment power = Enchantment.getEnchantmentByID(48); // enchantment.arrowDamage
				Enchantment punch = Enchantment.getEnchantmentByID(49); // enchantment.arrowKnockback
				itemStack.addEnchantment(power, power != null ? power.getMaxLevel() : 0);
				itemStack.addEnchantment(punch, punch != null ? punch.getMaxLevel() : 0);
			}
			else {
				itemStack = new ItemStack(Items.WOODEN_SWORD);
				Enchantment unbreaking = Enchantment.getEnchantmentByID(34);
				// logger.trace(String.format("Unbreaking Enchantment: %s",
				// unbreaking.getName()));
				Enchantment smite = Enchantment.getEnchantmentByID(17);
				// logger.trace(String.format("Smite Enchantment: %s", smite.getName()));
				Enchantment sharpness = Enchantment.getEnchantmentByID(16);
				// logger.trace(String.format("Sharpness Enchantment: %s",
				// sharpness.getName()));
				Enchantment bane = Enchantment.getEnchantmentByID(18);
				// logger.trace(String.format("Bane Enchantment: %s", bane.getName()));
				itemStack.addEnchantment(unbreaking, unbreaking != null ? unbreaking.getMaxLevel() : 0);
				itemStack.addEnchantment(sharpness, sharpness != null ? sharpness.getMaxLevel() : 0);
				itemStack.addEnchantment(smite, smite != null ? smite.getMaxLevel() : 0);
				itemStack.addEnchantment(bane, bane != null ? bane.getMaxLevel() : 0);
			}
		}

		if (attributes.isMarriedToAPlayer() || profession == EnumProfession.Child) {
			// Spouses, and player children all use weapons from the combat AI.
			itemStack = getBehavior(ActionCombat.class).getHeldItem();
		}
		// logger.debug(MessageFormat.format("Returning Item Stack: {0}", itemStack));
		return itemStack;
	}

	public void setHeldItem(Item item) {
		setHeldItem(EnumHand.MAIN_HAND, new ItemStack(item));
	}

	public boolean damageHeldItem(int amount) {
		try {
			ItemStack heldItem = getHeldItem(EnumHand.MAIN_HAND);

			if (heldItem != null) {
				Item item = heldItem.getItem();
				int slot = attributes.getInventory().getFirstSlotContainingItem(item);

				ItemStack itemInSlot = attributes.getInventory().getStackInSlot(slot);

				if (itemInSlot != null) {
					itemInSlot.damageItem(amount, this);

					if (itemInSlot.getCount() == 0) {
						behaviors.disableAllToggleActions();
						attributes.getInventory().setInventorySlotContents(slot, ItemStack.EMPTY);
						return true;
					}
					else {
						attributes.getInventory().setInventorySlotContents(slot, itemInSlot);
						return false;
					}
				}
			}
		}
		catch (Exception e) {
			String msg = String.format("Exception occurred!%nMessage: %s%n", e.getLocalizedMessage());
			FMLLog.severe(msg, e);
			java.util.logging.LogManager.getLogManager().getLogger(this.getClass().getName()).severe(msg);
			org.apache.logging.log4j.LogManager.getLogger(this.getClass().getName()).error(msg, e);
			java.util.logging.Logger.getLogger(this.getClass().getName()).severe(msg);
		}

		return false;
	}

	@Override
	public Iterable<ItemStack> getHeldEquipment() {
		List<ItemStack> heldEquipment = new ArrayList<ItemStack>();
		heldEquipment.add(getHeldItem(EnumHand.MAIN_HAND));
		return heldEquipment;
	}

	@Override
	public Iterable<ItemStack> getArmorInventoryList() {
		List<ItemStack> armorInventory = new ArrayList<ItemStack>();
		armorInventory.add(attributes.getInventory().getStackInSlot(39));
		armorInventory.add(attributes.getInventory().getStackInSlot(38));
		armorInventory.add(attributes.getInventory().getStackInSlot(37));
		armorInventory.add(attributes.getInventory().getStackInSlot(36));

		return armorInventory;
	}

	@Override
	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
		switch (slotIn) {
		case HEAD:
			return attributes.getInventory().getStackInSlot(36);
		case CHEST:
			return attributes.getInventory().getStackInSlot(37);
		case LEGS:
			return attributes.getInventory().getStackInSlot(38);
		case FEET:
			return attributes.getInventory().getStackInSlot(39);
		case MAINHAND:
			return getHeldItem(EnumHand.MAIN_HAND);
		case OFFHAND:
			if (attributes.getProfessionEnum() == EnumProfession.Guard) {
				return new ItemStack(Items.SHIELD);
			}
			return ItemStack.EMPTY;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public int getTotalArmorValue() {
		int value = 0;

		for (int i = 36; i < 40; i++) {
			final ItemStack stack = attributes.getInventory().getStackInSlot(i);

			if (stack != null && stack.getItem() instanceof ItemArmor) {
				value += ((ItemArmor) stack.getItem()).damageReduceAmount;
			}
		}

		return value;
	}

	@Override
	public void damageArmor(float amount) {
		for (int i = 36; i < 40; i++) {
			final ItemStack stack = attributes.getInventory().getStackInSlot(i);

			if (stack != null && stack.getItem() instanceof ItemArmor) {
				stack.damageItem((int) amount, this);
			}
		}
	}

	public void swingItem() {
		this.swingArm(EnumHand.MAIN_HAND);
	}

	@Override
	public void swingArm(EnumHand hand) {
		if (!attributes.getIsSwinging() || swingProgressTicks >= 8 / 2 || swingProgressTicks < 0) {
			swingProgressTicks = -1;
			attributes.setIsSwinging(true);
		}
	}

	public void cureInfection() {
		attributes.setIsInfected(false);
		addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200, 0));
		world.playEvent(null, 1027, new BlockPos((int) this.posX, (int) this.posY, (int) this.posZ), 0);
		Utilities.spawnParticlesAroundEntityS(EnumParticleTypes.VILLAGER_HAPPY, this, 16);
	}

	public boolean isInOverworld() {
		return world.provider.getDimension() == 0;
	}

	public Profiler getProfiler() {
		return profiler;
	}

	public void setHitboxSize(float width, float height) {
		this.setSize(width, height);
	}

	@Override
	public String getName() {
		return this.attributes.getName();
	}

	/**
	 * Overrides from EntityVillager that allow trades to work. Issues arose from
	 * the profession not being set properly.
	 *
	 * @param professionId
	 *            Profession ID
	 */
	@Override
	public void setProfession(int professionId) {
		this.vanillaProfessionId = professionId % 5;

	}

	@Deprecated
	@Override
	public int getProfession() {
		return this.vanillaProfessionId;
	}

	@Override
	public void setProfession(net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession prof) {
		this.vanillaProfessionId = VillagerRegistry.getId(prof);
	}

	@Override
	public VillagerRegistry.VillagerProfession getProfessionForge() {
		VillagerRegistry.VillagerProfession profession = VillagerRegistry.getById(this.vanillaProfessionId);

		if (profession == null) {
			return VillagerRegistry.getById(0);
		}

		return profession;
	}

	@Override
	public void useRecipe(MerchantRecipe recipe) {
		recipe.incrementToolUses();
		int i = 3 + this.rand.nextInt(4);

		EntityPlayer buyingPlayer = getPrivateValue(EntityVillager.class, this, FIELD_INDEX_BUYING_PLAYER);

		if (recipe.getToolUses() == 1 || this.rand.nextInt(5) == 0) {
			// timeUntilReset = 40;
			setEntityVillagerField(FIELD_INDEX_TIME_UNTIL_RESET, Integer.valueOf(40));
			// needsInitialization = true;
			setEntityVillagerField(FIELD_INDEX_NEEDS_INITIALIZATION, true);
			// isWillingToMate = true; (replaced with false to prevent any possible vanilla
			// villager mating)
			setEntityVillagerField(FIELD_INDEX_IS_WILLING_TO_MATE, false);

			if (buyingPlayer != null) // this.buyingPlayer != null
			{
				// this.lastBuyingPlayer = this.buyingPlayer.getUniqueID();
				setEntityVillagerField(FIELD_INDEX_LAST_BUYING_PLAYER, buyingPlayer.getUniqueID());
			}
			else {
				// this.lastBuyingPlayer = null;
				setEntityVillagerField(FIELD_INDEX_LAST_BUYING_PLAYER, null);
			}

			i += 5;
		}

		if (recipe.getItemToBuy().getItem() == Items.EMERALD) {
			// wealth += recipe.getItemToBuy().getCount();
			int wealth = getEntityVillagerField(FIELD_INDEX_WEALTH);
			setEntityVillagerField(FIELD_INDEX_WEALTH, wealth + recipe.getItemToBuy().getCount());
		}

		if (recipe.getRewardsExp()) {
			this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY + 0.5D, this.posZ, i));
		}

		if (buyingPlayer instanceof EntityPlayerMP) {
			CriteriaTriggers.VILLAGER_TRADE.trigger((EntityPlayerMP) buyingPlayer, this, recipe.getItemToSell());
		}
	}

	private <T, E> void setEntityVillagerField(int fieldIndex, Object value) {
		setPrivateValue(EntityVillager.class, this, value, fieldIndex);
	}

	private <T, E> T getEntityVillagerField(int fieldIndex) {
		return getPrivateValue(EntityVillager.class, this, fieldIndex);
	}

	public float getPitch() {
		return pitch;
	}

	public void flee() {
		if (isPlayerSleeping()) {
			return;
		}
		if (navigator.noPath() && !attributes.getIsBeingChased()) {
			this.attributes.setIsBeingChased(true);
			List<Point3D> doors = this.findDoors(10);
			if (doors != null && doors.size() > 0) {
				Point3D door = doors.get(RadixMath.getNumberInRange(0, doors.size() - 1));
				navigator.tryMoveToXYZ(door.dX(), door.dY(), door.dZ(), Constants.SPEED_SPRINT);
			}
			else {
				List<EntityVillagerMCA> possibleTargets = RadixLogic.getEntitiesWithinDistance(EntityVillagerMCA.class,
						this, 10);
				for (EntityVillagerMCA target : possibleTargets) {
					if (target.attributes.getRace() == this.attributes.getRace() && target.isFighter()) {
						navigator.tryMoveToEntityLiving(target, Constants.SPEED_RUN);
						break;
					}
				}
				if (this.getHomePosition() != null) {
					if (this.getNavigator().noPath()) {
						this.getNavigator().tryMoveToXYZ(this.getHomePosition().getX(), this.getHomePosition().getY(),
								this.getHomePosition().getZ(), Constants.SPEED_RUN);
					}
				}
				// tryMoveToXYZ(door.dX(), door.dY(), door.dZ(), Constants.SPEED_SPRINT)
			}
		}
		this.attributes.setIsBeingChased(false);
		// this.behaviors.getAction(ActionRetreat.class).setIsActive(true);
	}

	// @Override
	// public int getGrowingAge() {
	// return attributes.getIsChild() ? -1 : attributes.getAge();
	// }

	@Override
	public boolean isChild() {
		return attributes.getIsChild();
	}

	public boolean isFighter() {
		return this.attributes.getProfessionEnum() == EnumProfession.Archer
				|| this.attributes.getProfessionEnum() == EnumProfession.Guard
				|| this.attributes.getProfessionEnum() == EnumProfession.Warrior;
	}

	public EntityTameable getPet() {
		return pet;
	}

	public void setPet(EntityTameable pet) {
		this.pet = pet;
	}

	/**
	 * @return the ridingEntity
	 */
	@Override
	public Entity getRidingEntity() {
		return this.ridingEntity;
	}

	/**
	 * @param ridingEntity
	 *            the ridingEntity to set
	 */
	public void setRidingEntity(Entity ridingEntity) {
		this.ridingEntity = ridingEntity;
	}

	@Override
	public void updateRidden() {
		Entity ride = this.getRidingEntity();

		if (this.isRiding() && (ride != null && ride.isDead)) {
			this.dismountRidingEntity();
		}
		else {
			this.motionX = 0.0D;
			this.motionY = 0.0D;
			this.motionZ = 0.0D;
			if (!updateBlocked)
				this.onUpdate();

			if (ride !=null && this.isRiding()) {
				ride.updatePassenger(this);
			}
		}
	}

	@Override
	public void dismountRidingEntity() {
		if (this.ridingEntity != null) {
			Entity ride = this.ridingEntity;
			if (!net.minecraftforge.event.ForgeEventFactory.canMountEntity(this, ride, false))
				return;
			this.ridingEntity = null;
			super.dismountRidingEntity();
		}
	}

	@Override
	public boolean startRiding(Entity entityIn, boolean force) {
		boolean flag = super.startRiding(entityIn, force);

		if (flag && this.getLeashed()) {
			this.clearLeashed(true, true);
		}

		return flag;
	}

	// public EnumRace getRace() {
	//
	// }
	//
	// //I probably don't want this method.
	// public void setRace(int race) {
	// this.raceId = race;
	// }
}
