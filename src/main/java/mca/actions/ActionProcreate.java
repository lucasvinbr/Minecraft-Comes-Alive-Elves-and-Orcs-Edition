package mca.actions;

import java.util.Random;

import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.SoundsMCA;
import mca.data.NBTPlayerData;
import mca.entity.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.enums.EnumRace;
import mca.enums.EnumRace;
import mca.items.ItemBaby;
import mca.packets.PacketOpenBabyNameGUI;
import mca.util.Utilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import radixcore.constant.Time;

public class ActionProcreate extends AbstractAction {
	private static final DataParameter<Boolean>
			IS_PROCREATING =
			EntityDataManager.<Boolean>createKey(EntityVillagerMCA.class, DataSerializers.BOOLEAN);

	private boolean hasHadTwins;
	private int procreateTicks;

	public ActionProcreate(EntityVillagerMCA actor) {
		super(actor);
		setIsProcreating(false);
	}

	@Override
	public void onUpdateClient() {
		if (getIsProcreating()) {
			actor.rotationYawHead += 40;
			Utilities.spawnParticlesAroundEntityC(EnumParticleTypes.HEART, actor, 2);
		}
	}

	@Override
	public void onUpdateServer() {
		if (getIsProcreating()) {
			if(actor.attributes.getGender() == EnumGender.FEMALE) {
				actor.playSound(actor.attributes.getRace() ==
						                EnumRace.Orc ?
				                (new Random().nextBoolean() ?
				                 SoundsMCA.femalehurt5 :
				                 SoundsMCA.femalehurt6) :
				                (new Random().nextBoolean() ?
				                 SoundsMCA.femalehurt2 :
				                 SoundsMCA.femalehurt4), 2.0F, actor.getPitch());
			}
			procreateTicks++;

			if (procreateTicks >= Time.SECOND * 3) {
				setIsProcreating(false);
				procreateTicks = 0;
				actor.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
				if (actor.attributes.isMarriedToAPlayer()) {
					final EntityPlayer playerSpouse = actor.attributes.getPlayerSpouseInstance();

					if (playerSpouse != null) {
						NBTPlayerData data = MCA.getPlayerData(playerSpouse);
						data.setOwnsBaby(true);

						boolean isMale = new Random().nextBoolean();
						ItemStack babyStack = new ItemStack(isMale ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);

						boolean isPlayerInventoryFull = playerSpouse.inventory.getFirstEmptyStack() == -1;

						if (isPlayerInventoryFull) {
							actor.attributes.getInventory().addItem(babyStack);
						} else {
							playerSpouse.inventory.addItemStackToInventory(babyStack);
						}

						//Achievement achievement = isMale ? AchievementsMCA.babyBoy : AchievementsMCA.babyGirl;
						//playerSpouse.addStat(achievement);

						MCA.getPacketHandler()
						   .sendPacketToPlayer(new PacketOpenBabyNameGUI(isMale), playerSpouse);
					}
				} else {
					final EntityVillagerMCA spouse = actor.attributes.getVillagerSpouseInstance();

					if (spouse != null) {
						boolean isMale = new Random().nextBoolean();
						ItemStack babyStack = new ItemStack(isMale ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);
						ItemBaby baby = (ItemBaby) babyStack.getItem();
						if (spouse.attributes.getGender() == EnumGender.FEMALE) {
							baby.setFather(actor);
							baby.setMother(spouse);
							spouse.attributes.getInventory().addItem(babyStack);
							spouse.setHeldItem(baby);
						} else {
							actor.attributes.getInventory().addItem(babyStack);
							baby.setFather(spouse);
							baby.setMother(actor);
							actor.attributes.getInventory().addItem(babyStack);
							actor.setHeldItem(baby);
						}
					}
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("isProcreating", getIsProcreating());
		nbt.setBoolean("hasHadTwins", hasHadTwins);
		nbt.setInteger("procreateTicks", procreateTicks);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		setIsProcreating(nbt.getBoolean("isProcreating"));
		hasHadTwins = nbt.getBoolean("hasHadTwins");
		procreateTicks = nbt.getInteger("procreateTicks");
	}

	public void setIsProcreating(boolean value) {
		actor.getDataManager().set(IS_PROCREATING, value);
	}

	public boolean getIsProcreating() {
		return actor.getDataManager().get(IS_PROCREATING);
	}

	public boolean getHasHadTwins() {
		return hasHadTwins;
	}

	public void setHasHadTwins(boolean value) {
		hasHadTwins = value;
	}

	protected void registerDataParameters() {
		actor.getDataManager().register(IS_PROCREATING, false);
	}
}
