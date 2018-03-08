package mca.items;

import java.util.List;
import java.util.UUID;

import mca.core.Constants;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
//import mca.entity.EntityElfMCA;
//import mca.entity.EntityOrcMCA;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumDialogueType;
import mca.enums.EnumGender;
import mca.enums.EnumProfession;
import mca.enums.EnumRace;
import mca.enums.EnumRelation;
import mca.packets.PacketOpenBabyNameGUI;
import mca.util.TutorialManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;
import radixcore.constant.Time;

public class ItemBaby extends Item {
	private static Logger logger = LogManager.getLogger(ItemBaby.class);
	private final boolean isBoy;

	private String motherName = "N/A";
	private UUID motherId = Constants.EMPTY_UUID;
	private EnumGender motherGender = EnumGender.UNASSIGNED;
	private EnumRace motherRace = EnumRace.Villager;
	private String fatherName = "N/A";
	private UUID fatherId = Constants.EMPTY_UUID;
	private EnumGender fatherGender = EnumGender.UNASSIGNED;
	private EnumRace fatherRace = EnumRace.Unassigned;

	public ItemBaby(boolean isBoy) {
		this.isBoy = isBoy;
		this.setMaxStackSize(1);
	}

//	public EntityVillagerMCA getFather() {
//		return father;
//	}
//
	public void setFather(EntityVillagerMCA father) {
		fatherName = father.attributes.getName();
		fatherId = father.getUniqueID();
		fatherGender = father.attributes.getGender();
		fatherRace = father.attributes.getRaceEnum();
	}
//
//	public EntityVillagerMCA getMother() {
//		return mother;
//	}
//
	public void setMother(EntityVillagerMCA mother) {
		motherName = mother.attributes.getSpouseName();
		motherId = mother.getUniqueID();
		motherGender = mother.attributes.getSpouseGender();
		motherRace = mother.attributes.getRaceEnum();
	}

	@Override
	public void onUpdate(ItemStack itemStack, World world, Entity entity, int unknownInt, boolean unknownBoolean) {
		super.onUpdate(itemStack, world, entity, unknownInt, unknownBoolean);

		if (!world.isRemote) {
			if (!itemStack.hasTagCompound()) {
				String
						ownerName =
						entity instanceof EntityPlayer ?
						entity.getName() :
						entity instanceof EntityVillagerMCA ?
						((EntityVillagerMCA) entity).attributes.getSpouseName() :
						"Unknown";

				NBTTagCompound compound = new NBTTagCompound();

				compound.setString("name", "Unnamed");
				compound.setInteger("age", 0);
				compound.setString("owner", ownerName);
				compound.setBoolean("isInfected", false);

				itemStack.setTagCompound(compound);

				if (entity instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entity;

					if (player.capabilities.isCreativeMode) {
						TutorialManager.sendMessageToPlayer(player,
						                                    "You can name a baby retrieved from",
						                                    "creative mode by right-clicking the air.");
					}
				}
			} else {
				updateBabyGrowth(itemStack);
			}
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player,
	                                  World world,
	                                  BlockPos pos,
	                                  EnumHand hand,
	                                  EnumFacing side,
	                                  float hitX,
	                                  float hitY,
	                                  float hitZ) {
		logger.debug("onItemUse");
		MCA.getLog().debug("onItemUse");
		if (hand == EnumHand.OFF_HAND) {
			return EnumActionResult.FAIL;
		}

		ItemStack stack = player.getHeldItem(hand);
		EnumActionResult result = EnumActionResult.FAIL;
		if(hasMother() && hasFather()) {
			logger.debug("Has both NPC parents.");
			MCA.getLog().debug("Has both NPC parents.");
			EntityVillagerMCA mother = new EntityVillagerMCA(world);
			mother.attributes.setRace(motherRace);
			mother.attributes.setName(motherName);
			mother.attributes.setGender(motherGender);
			mother.setUniqueId(motherId);
			EntityVillagerMCA father = new EntityVillagerMCA(world);
			father.attributes.setRace(fatherRace);
			father.attributes.setName(fatherName);
			father.attributes.setGender(fatherGender);
			father.setUniqueId(fatherId);

			result = onItemUseByVillager(mother, father, world, stack, pos);

		} else if(hasMother()) {
			logger.debug("Only has NPC mother");
			MCA.getLog().debug("Only has NPC mother");
			EntityVillagerMCA mother = new EntityVillagerMCA(world);
			mother.attributes.setRace(motherRace);
			mother.attributes.setName(motherName);
			mother.attributes.setGender(motherGender);

			result = onItemUseByVillager(mother, world, stack, pos);
		} else if (hasFather()) {
			logger.debug("Only has NPC father");
			MCA.getLog().debug("Only has NPC father");
			EntityVillagerMCA father = new EntityVillagerMCA(world);
			father.attributes.setRace(fatherRace);
			father.attributes.setName(fatherName);
			father.attributes.setGender(fatherGender);
			father.setUniqueId(fatherId);

			result = onItemUseByVillager(father, world, stack, pos);
		} else {
			logger.debug("Player's child.");
			MCA.getLog().debug("Player's child.");
			int posX = pos.getX();
			int posY = pos.getY();
			int posZ = pos.getZ();

			if (!world.isRemote && isReadyToGrowUp(stack)) {
				ItemBaby baby = (ItemBaby) stack.getItem();
				NBTPlayerData data = MCA.getPlayerData(player);
				boolean isPlayerMale = data.getGender() == EnumGender.MALE;


				if (isPlayerMale) {
					motherName = data.getSpouseName();
					motherId = data.getSpouseUUID();
					motherGender = data.getSpouseGender();
					fatherName = player.getName();
					fatherId = data.getUUID();
					fatherGender = data.getGender();
					fatherRace = data.getRace();
				} else {
					fatherName = data.getSpouseName();
					fatherId = data.getSpouseUUID();
					fatherGender = data.getSpouseGender();
					motherName = player.getName();
					motherId = data.getUUID();
					motherGender = data.getGender();
					motherRace = data.getSpouseRace();
				}

				final EntityVillagerMCA child = new EntityVillagerMCA(world);
				child.attributes.setGender(baby.isBoy ? EnumGender.MALE : EnumGender.FEMALE);
				child.attributes.setIsChild(true);
				child.attributes.setName(stack.getTagCompound().getString("name"));
				child.attributes.setProfession(EnumProfession.Child);
				child.attributes.assignRandomSkin();
				child.attributes.assignRandomScale();
				child.attributes.setMotherGender(motherGender);
				child.attributes.setMotherName(motherName);
				child.attributes.setMotherUUID(motherId);
				child.attributes.setFatherGender(fatherGender);
				child.attributes.setFatherName(fatherName);
				child.attributes.setFatherUUID(fatherId);

				child.setPosition(posX, posY + 1, posZ);

				if (stack.getTagCompound().getBoolean("isInfected")) {
					child.attributes.setIsInfected(true);
				}

				world.spawnEntity(child);

				PlayerMemory childMemory = child.attributes.getPlayerMemory(player);
				childMemory.setHearts(100);
				childMemory.setDialogueType(EnumDialogueType.CHILDP);
				childMemory.setRelation(child.attributes.getGender() == EnumGender.MALE ?
				                        EnumRelation.SON :
				                        EnumRelation.DAUGHTER);

				//player.addStat(AchievementsMCA.babyToChild);

				data.setOwnsBaby(false);
			}
		}
		if(result == EnumActionResult.PASS) {
			player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
		}
		return result;
	}

	public boolean hasFather() {
		return fatherId != Constants.EMPTY_UUID;
	}

	public boolean hasMother() {
		return motherId != Constants.EMPTY_UUID;
	}

	public EnumActionResult onItemUseByVillager(EntityVillagerMCA villager, World world, ItemStack babyStack, BlockPos pos) {
		int posX = pos.getX();
		int posY = pos.getY();
		int posZ = pos.getZ();

		if (!world.isRemote /*&& isReadyToGrowUp(babyStack)*/) {
			ItemBaby baby = (ItemBaby) babyStack.getItem();
			boolean isVIllagerMale = villager.attributes.getGender() == EnumGender.MALE;


			if (isVIllagerMale) {
				fatherName = villager.attributes.getName();
				fatherId = villager.getUniqueID();
				fatherGender = villager.attributes.getGender();
				fatherRace = villager.attributes.getRaceEnum();
			} else {
				motherName = villager.attributes.getName();
				motherId = villager.getUniqueID();
				motherGender = villager.attributes.getGender();
				motherRace = villager.attributes.getRaceEnum();
			}

			EntityVillagerMCA child = null;
			child = new EntityVillagerMCA(world);
			if(villager.attributes.getRaceEnum() == EnumRace.Orc) {
				if(baby.isBoy) {
					child.attributes.setProfession(EnumProfession.Guard);
					child.attributes.setRace(EnumRace.Orc);
				} else {
					child.attributes.setProfession(EnumProfession.Child);
					child.attributes.setRace(EnumRace.Villager);
				}
			} else {
				child.attributes.setProfession(EnumProfession.Child);
				if(villager.attributes.getRaceEnum() == EnumRace.Elf) {
					child.attributes.setProfession(baby.isBoy ? EnumProfession.Guard : EnumProfession.Archer);
					child.attributes.setRace(baby.isBoy ? EnumRace.Villager : EnumRace.Elf);
				}
			}
			child.attributes.setGender(baby.isBoy ? EnumGender.MALE : EnumGender.FEMALE);
			child.attributes.setIsChild(true);
			child.attributes.assignRandomName();
//			child.attributes.setName(babyStack.getTagCompound().getString("name"));
			child.attributes.assignRandomSkin();
			child.attributes.assignRandomScale();
			child.attributes.setMotherGender(motherGender);
			child.attributes.setMotherName(motherName);
			child.attributes.setMotherUUID(motherId);
			child.attributes.setFatherGender(fatherGender);
			child.attributes.setFatherName(fatherName);
			child.attributes.setFatherUUID(fatherId);

			child.setPosition(posX, posY + 1, posZ);

			if (babyStack.getTagCompound().getBoolean("isInfected")) {
				child.attributes.setIsInfected(true);
			}

			world.spawnEntity(child);
			//TODO: Remove two lines below
			PotionEffect glowing = new PotionEffect(Potion.getPotionById(24), 1000);
			child.addPotionEffect(glowing);
			//player.addStat(AchievementsMCA.babyToChild);
			return EnumActionResult.PASS;
		}
		return EnumActionResult.FAIL;
	}

	public EnumActionResult onItemUseByVillager(EntityVillagerMCA mother, EntityVillagerMCA father, World world, ItemStack babyStack, BlockPos pos) {

		int posX = pos.getX();
		int posY = pos.getY();
		int posZ = pos.getZ();

		if (!world.isRemote /*&& isReadyToGrowUp(babyStack)*/) {
			ItemBaby baby = (ItemBaby) babyStack.getItem();

			motherName = mother.attributes.getSpouseName();
			motherId = mother.getUniqueID();
			motherGender = mother.attributes.getSpouseGender();
			motherRace = mother.attributes.getRaceEnum();

			fatherName = father.attributes.getName();
			fatherId = father.getUniqueID();
			fatherGender = father.attributes.getGender();
			fatherRace = father.attributes.getRaceEnum();

			EntityVillagerMCA child = new EntityVillagerMCA(world);
			if(father.attributes.getRaceEnum() == EnumRace.Orc && mother.attributes.getRaceEnum() == EnumRace.Orc) {
				child.attributes.setProfession(EnumProfession.Unassigned);
				child.attributes.setRace(EnumRace.Orc);
			} else if(father.attributes.getRaceEnum() == EnumRace.Elf && mother.attributes.getRaceEnum() == EnumRace.Elf) {
				child.attributes.setProfession(baby.isBoy ? EnumProfession.Guard : EnumProfession.Archer);
				child.attributes.setRace(EnumRace.Elf);
			} else {

				if(father.attributes.getRaceEnum() == EnumRace.Orc || mother.attributes.getRaceEnum() == EnumRace.Orc) {
					if(baby.isBoy) {
						child.attributes.setProfession(EnumProfession.Unassigned);
						child.attributes.setRace(EnumRace.Orc);
					} else {
						child.attributes.setProfession(EnumProfession.Child);
						child.attributes.setRace(EnumRace.Villager);
					}
				} else {
					child = new EntityVillagerMCA(world);
					child.attributes.setProfession(EnumProfession.Child);
					if(father.attributes.getRaceEnum() == EnumRace.Elf || mother.attributes.getRaceEnum() == EnumRace.Elf) {
						child.attributes.setProfession(baby.isBoy ? EnumProfession.Guard : EnumProfession.Archer);
						child.attributes.setRace(baby.isBoy ? EnumRace.Villager : EnumRace.Elf);
					}
				}
			}
			child.attributes.setIsChild(true);
			child.attributes.setGender(baby.isBoy ? EnumGender.MALE : EnumGender.FEMALE);
//			child.attributes.setName(babyStack.getTagCompound().getString("name"));
			child.attributes.assignRandomName();
			child.attributes.assignRandomSkin();
			child.attributes.assignRandomScale();
			child.attributes.setMotherGender(motherGender);
			child.attributes.setMotherName(motherName);
			child.attributes.setMotherUUID(motherId);
			child.attributes.setFatherGender(fatherGender);
			child.attributes.setFatherName(fatherName);
			child.attributes.setFatherUUID(fatherId);

			child.setPosition(posX, posY + 1, posZ);

			if (babyStack.getTagCompound().getBoolean("isInfected")) {
				child.attributes.setIsInfected(true);
			}

			world.spawnEntity(child);
			//TODO: Remove two lines below
			PotionEffect glowing = new PotionEffect(Potion.getPotionById(24), 1000);
			child.addPotionEffect(glowing);
			//player.addStat(AchievementsMCA.babyToChild);
			return EnumActionResult.PASS;
		} else {
			return EnumActionResult.FAIL;
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		if (!world.isRemote && stack.getTagCompound().getString("name").equals("Unnamed")) {
			ItemBaby baby = (ItemBaby) stack.getItem();
			MCA.getPacketHandler().sendPacketToPlayer(new PacketOpenBabyNameGUI(baby.isBoy), (EntityPlayerMP) player);
		}

		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		//Happens on servers for some reason.
		if (entityItem.getItem() != null && !entityItem.world.isRemote) {
			updateBabyGrowth(entityItem.getItem());
		}

		return super.onEntityItemUpdate(entityItem);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);

		if (stack.hasTagCompound()) {
			//Text color is blue for boys, purple for girls.
			String textColor = ((ItemBaby) stack.getItem()).isBoy ? Color.AQUA : Color.LIGHTPURPLE;
			int ageInMinutes = stack.getTagCompound().getInteger("age");

			//Owner name is You for the current owner. Otherwise, the player's name.
			String ownerName = stack.getTagCompound().getString("owner");
			ownerName = ownerName.equals(Minecraft.getMinecraft().player.getName()) ? "You" : ownerName;

			tooltip.add(textColor + "Name: " + Format.RESET + stack.getTagCompound().getString("name"));
			tooltip.add(textColor +
			            "Age: " +
			            Format.RESET +
			            ageInMinutes +
			            (ageInMinutes == 1 ? " minute" : " minutes"));
			tooltip.add(textColor + "Parent: " + Format.RESET + ownerName);

			if (stack.getTagCompound().getBoolean("isInfected")) {
				tooltip.add(Color.GREEN + "Infected!");
			}

			if (isReadyToGrowUp(stack)) {
				tooltip.add(Color.GREEN + "Ready to grow up!");
			}
		}
	}

	private void updateBabyGrowth(ItemStack itemStack) {
		if (itemStack != null &&
		    itemStack.hasTagCompound() &&
		    FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() % Time.MINUTE == 0) {
			int age = itemStack.getTagCompound().getInteger("age");
			age++;
			itemStack.getTagCompound().setInteger("age", age);
		}
	}

	private boolean isReadyToGrowUp(ItemStack itemStack) {
		if (itemStack != null && itemStack.hasTagCompound()) {
			final int ageInMinutes = itemStack.getTagCompound().getInteger("age");
			return ageInMinutes >= MCA.getConfig().babyGrowUpTime;
		}

		return false;
	}

	public boolean getIsBoy() {
		return isBoy;
	}
}
