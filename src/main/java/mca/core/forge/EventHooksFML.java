package mca.core.forge;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.minecraft.SoundsMCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerDataCollection;
import mca.entity.monster.EntityGrimReaper;
import mca.entity.monster.EntityWitchMCA;
import mca.entity.passive.EntityVillagerMCA;
import mca.entity.passive.EntityWolfMCA;
import mca.enums.EnumBabyState;
import mca.enums.EnumGender;
import mca.enums.EnumProfession;
import mca.enums.EnumProfessionSkinGroup;
import mca.packets.PacketPlayerDataLogin;
import mca.packets.PacketSpawnLightning;
import mca.packets.PacketSyncConfig;
import mca.util.Utilities;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;
import radixcore.modules.schematics.RadixSchematics;

public class EventHooksFML {
	private Logger logger = LogManager.getLogger(this.getClass());

	public static boolean playPortalAnimation;
	private static int summonCounter;
	private static Point3D summonPos;
	private static World summonWorld;

	private int clientTickCounter;
	private int serverTickCounter;

	@SubscribeEvent
	public void onConfigChanges(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.getModID().equals(MCA.ID)) {
			MCA.getConfig().getInstance().save();
			MCA.getConfig().syncConfiguration();
		}
	}

	@SubscribeEvent
	public void playerLoggedInEventHandler(PlayerLoggedInEvent event) {
		EntityPlayer player = event.player;
		PlayerDataCollection dataCollection = PlayerDataCollection.get();
		boolean setPermanentId = false;

		NBTPlayerData nbtData;

		if (dataCollection.getPlayerData(player.getUniqueID()) == null) {
			// A permanent ID is generated if no ID exists after reading from NBT.
			NBTPlayerData nbtPlayerData = new NBTPlayerData();
			dataCollection.putPlayerData(player.getUniqueID(), nbtPlayerData);
			nbtData = nbtPlayerData;
			setPermanentId = true;
		}
		else {
			nbtData = dataCollection.getPlayerData(player.getUniqueID());
		}

		// Sync the server's configuration, for display settings.
		MCA.getPacketHandler().sendPacketToPlayer(new PacketSyncConfig(MCA.getConfig()), event.player);

		// Send copy of the player data to the client.
		if (nbtData != null) {
			MCA.getPacketHandler().sendPacketToPlayer(new PacketPlayerDataLogin(nbtData), player);

			if (setPermanentId) {
				nbtData.setUUID(player.getUniqueID());
			}

			// Add the crystal ball to the inventory if needed.
			if (!nbtData.getHasChosenDestiny() && !player.inventory.hasItemStack(new ItemStack(ItemsMCA.CRYSTAL_BALL))
					&& MCA.getConfig().giveCrystalBall) {
				player.inventory.addItemStackToInventory(new ItemStack(ItemsMCA.CRYSTAL_BALL));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void clientTickEventHandler(ClientTickEvent event) {
		MCA.getPacketHandler().processPackets(Side.CLIENT);

		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
		net.minecraft.client.gui.GuiScreen currentScreen = mc.currentScreen;

		if (currentScreen instanceof net.minecraft.client.gui.GuiMainMenu && MCA.myPlayerData != null) {
			playPortalAnimation = false;
			MCA.destinyCenterPoint = null;
			MCA.destinySpawnFlag = false;
			MCA.myPlayerData = null;
			MCA.resetConfig();
		}

		// Check for setting/processing the flag for loading language again.
		if (currentScreen instanceof net.minecraft.client.gui.GuiLanguage) {
			MCA.reloadLanguage = true;
		}
		else if (MCA.reloadLanguage) {
			MCA.reloadLanguage = false;
			MCA.getLocalizer().onLanguageChange();
		}

		if (playPortalAnimation) {
			EntityPlayerSP player = mc.player;

			if (player == null) {
				return; // Crash when kicked from a server while using the ball. Client-side, so just
						// throw it out.
			}

			player.prevTimeInPortal = player.timeInPortal;
			player.timeInPortal -= 0.0125F;

			if (player.timeInPortal <= 0.0F) {
				playPortalAnimation = false;
			}
		}

		if (clientTickCounter <= 0) {
			clientTickCounter = Time.SECOND / 2;

			if (MCA.destinySpawnFlag) {
				RadixSchematics.spawnStructureRelativeToPoint("/assets/mca/schematic/destiny-test.schematic",
						MCA.destinyCenterPoint, mc.world);
			}
		}
		else {
			clientTickCounter--;
		}
	}

	@SubscribeEvent
	public void serverTickEventHandler(ServerTickEvent event) {
		MCA.getPacketHandler().processPackets(Side.SERVER);

		// This block prevents the long-standing issue of crashing while using a world
		// that previously contained villagers.
		// It will check every second for a villager that has not been converted, and
		// see if it should be. These villagers
		// are identified by having the value of 3577 for watched object number 28.
		if (serverTickCounter % 40 == 0) {
			for (World world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds) {
				for (int i = 0; i < world.loadedEntityList.size(); i++) {
					Entity entity = world.loadedEntityList.get(i);
					Biome biome = entity.world.getBiome(entity.getPosition());
					if (entity instanceof EntityVillager && !(entity instanceof EntityVillagerMCA)) {
						EntityVillager villager = (EntityVillager) entity;
						logger.trace(MessageFormat.format("Villager Data: {0}", villager));
						logger.debug(String.format("Profession Number: %d, Profession Name: %s",
								villager.getProfession(), villager.getDisplayName()));
						logger.trace(
								MessageFormat.format("Villager Forge Profession: {0}", villager.getProfessionForge()));
						try {
							if (villager.getProfession() == 5) {

								// Biome biome = villager.world.getBiome(villager.getPos());
								logger.info(String.format("Spawning villager in %s biome ", biome.getBiomeName()));
								if (biome.getBiomeName().toLowerCase().contains("swamp")) {
									doOverwriteVillagerWithOrc(villager);
									continue;
								}
								else if (biome.getBiomeName().toLowerCase().contains("forest")) {
									doOverwriteVillagerWithElf(villager);
									continue;
								}
								else {
									doOverwriteVillager(villager);
								}
							} 
							if (villager.getDataManager().get(Constants.OVERWRITE_KEY) == 3577) {
								doOverwriteVillager(villager);
								if (RadixLogic.getBooleanWithProbability(1)) {
									MCA.naturallySpawnCats(new Point3D(villager.getHomePosition().getX(),
											villager.getHomePosition().getY(), villager.getHomePosition().getZ()),
											world, false);
								}
							} else {
								// logger.warn("Villager's Data manager doesn't have overwrite key.");
								doOverwriteVillager(villager);
							}
						}
						catch (Exception e) {
//							logger.warn(e.getLocalizedMessage(), e);
						}
					}
					else if (entity instanceof EntityWitch && !(entity instanceof EntityWitchMCA)) {
						EntityWitch witch = (EntityWitch) entity;
						if (RadixLogic.getBooleanWithProbability(1)) {
							logger.info(String.format("Spawning witch in %s biome ", biome.getBiomeName()));
							doOverwriteWitchWithPrettyWitch(witch);
						}
					}
					else if (entity instanceof EntityWolf && !(entity instanceof EntityWolfMCA)) {
						if (RadixLogic.getBooleanWithProbability(100)) {
							doOverwriteWolf((EntityWolf) entity);
						}
					}
				}
			}
		}

		// Tick down reaper counter.
		if (summonCounter > 0) {
			summonCounter--;

			// Spawn particles around the summon point.
			Utilities.spawnParticlesAroundPointS(EnumParticleTypes.PORTAL, summonWorld, summonPos.iX(), summonPos.iY(),
					summonPos.iZ(), 2);

			// Lightning will strike periodically.
			if (summonCounter % (Time.SECOND * 2) == 0) {
				double dX = summonPos.iX()
						+ (summonWorld.rand.nextInt(6) * (RadixLogic.getBooleanWithProbability(50) ? 1 : -1));
				double dZ = summonPos.iZ()
						+ (summonWorld.rand.nextInt(6) * (RadixLogic.getBooleanWithProbability(50) ? 1 : -1));
				double y = RadixLogic.getSpawnSafeTopLevel(summonWorld, (int) dX, (int) dZ);
				NetworkRegistry.TargetPoint lightningTarget = new NetworkRegistry.TargetPoint(
						summonWorld.provider.getDimension(), dX, y, dZ, 64);
				EntityLightningBolt lightning = new EntityLightningBolt(summonWorld, dX, y, dZ, false);

				summonWorld.spawnEntity(lightning);
				MCA.getPacketHandler().sendPacketToAllAround(new PacketSpawnLightning(new Point3D(dX, y, dZ)),
						lightningTarget);

				// On the first lightning bolt, send the summon sound to all around the summon
				// point.
				if (summonCounter == 80) {
					NetworkRegistry.TargetPoint summonTarget = new NetworkRegistry.TargetPoint(
							summonWorld.provider.getDimension(), summonPos.iX(), summonPos.iY(), summonPos.iZ(), 32);
					summonWorld.playSound(null, new BlockPos(dX, y, dZ), SoundsMCA.reaper_summon, SoundCategory.HOSTILE,
							1.0F, 1.0F);
				}
			}

			if (summonCounter == 0) {
				EntityGrimReaper reaper = new EntityGrimReaper(summonWorld);
				reaper.setPosition(summonPos.iX(), summonPos.iY(), summonPos.iZ());
				summonWorld.spawnEntity(reaper);

				summonPos = null;
				summonWorld = null;
			}
		}

		if (serverTickCounter <= 0 && MCA.getConfig().guardSpawnRate > 0) {
			// Build a list of all humans on the server.
			List<EntityVillagerMCA> humans = new ArrayList<EntityVillagerMCA>();

			for (World world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds) {
				for (Object obj : world.loadedEntityList) {
					if (obj instanceof EntityVillagerMCA) {
						humans.add((EntityVillagerMCA) obj);
					}
				}
			}

			if (!humans.isEmpty()) {
				// Pick three humans at random to perform guard spawning around.
				for (int i = 0; i < 3; i++) {
					EntityVillagerMCA human = humans.get(RadixMath.getNumberInRange(0, humans.size() - 1));

					// Don't count guards in the total count of villagers.
					List<EntityVillagerMCA> villagersAroundMe = RadixLogic
							.getEntitiesWithinDistance(EntityVillagerMCA.class, human, 50);
					int numberOfGuardsAroundMe = getNumberOfGuardsFromEntityList(villagersAroundMe);
					int numberOfVillagersAroundMe = villagersAroundMe.size() - numberOfGuardsAroundMe;
					int neededNumberOfGuards = numberOfVillagersAroundMe / MCA.getConfig().guardSpawnRate;

					if (numberOfGuardsAroundMe < neededNumberOfGuards) {
						final EntityVillagerMCA guard = new EntityVillagerMCA(human.world);
						guard.attributes.assignRandomName();
						guard.attributes.assignRandomGender();
						guard.attributes.assignRandomPersonality();
						guard.attributes.setProfession(EnumProfession.Guard);
						guard.attributes.assignRandomSkin();

						final Vec3d pos = RandomPositionGenerator.findRandomTarget(human, 10, 1);

						if (pos != null) // Ensure a random position was actually found.
						{
							final Point3D posAsPoint = new Point3D(pos.x, pos.y, pos.z);

							// Check that we can see the sky, no guards in caves or stuck in blocks.
							if (human.world.canBlockSeeSky(posAsPoint.toBlockPos())) {
								guard.setPosition(pos.x, (int) human.posY, pos.z);
								human.world.spawnEntity(guard);
							}
						}
					}
				}
			}

			serverTickCounter = Time.MINUTE;
		}

		if (serverTickCounter <= 0 && MCA.getConfig().replenishEmptyVillages
				&& RadixLogic.getBooleanWithProbability(25)) {
			for (World world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds) {
				for (Object obj : world.villageCollection.getVillageList()) {
					Village village = (Village) obj;

					int populationCapacity = village.getNumVillageDoors();
					int population = 0;
					double posX = village.getCenter().getX();
					double posY = village.getCenter().getY();
					double posZ = village.getCenter().getZ();

					for (EntityVillagerMCA entity : RadixLogic.getEntitiesWithinDistance(EntityVillagerMCA.class, world,
							posX, posY, posZ, village.getVillageRadius())) {
						EntityVillagerMCA human = entity;

						// Count everyone except guards
						if (human.attributes.getProfessionSkinGroup() != EnumProfessionSkinGroup.Guard) {
							population++;
						}

						// Count babies with the villager population.
						if (human.attributes.getBabyState() != EnumBabyState.NONE) {
							population++;
						}
					}

					// If the village can support more villagers, spawn.
					int tries = 0;

					if (population < populationCapacity) {
						while (tries < 3) {
							posX = posX + (world.rand.nextInt(village.getVillageRadius()))
									* (RadixLogic.getBooleanWithProbability(50) ? 1 : -1);
							posZ = posZ + (world.rand.nextInt(village.getVillageRadius()))
									* (RadixLogic.getBooleanWithProbability(50) ? 1 : -1);

							// Offset to the center of the block
							posX += 0.5D;
							posZ += 0.5D;
							double dY = RadixLogic.getSpawnSafeTopLevel(world, (int) posX, (int) posZ);

							// Prevent spawning on roof by checking the safe spawn level against the center
							// level
							// and making sure it's not too high.
							if (dY - posY <= 4.0F) {
								Point3D pointOfSpawn = new Point3D(posX, dY, posZ);
								MCA.naturallySpawnVillagers(pointOfSpawn, world, -1);
								break;
							}
							else // Try again up to 3 times if not.
							{
								tries++;
							}
						}
					}
				}
			}
		}

		serverTickCounter--;
	}

	@SubscribeEvent
	public void itemCraftedEventHandler(ItemCraftedEvent event) {
		// Return damageable items to the inventory.
		for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
			ItemStack stack = event.craftMatrix.getStackInSlot(i);

			if (stack != null && (stack.getItem() == ItemsMCA.NEEDLE_AND_STRING)) {
				stack.damageItem(1, event.player);

				if (stack.getItemDamage() < stack.getMaxDamage()) {
					event.player.inventory.addItemStackToInventory(stack);
				}
			}
			break; //Why does he have this here?
		}
	}

	public void doOverwriteWolf(EntityWolf wolf) {
		MCA.naturallySpawnDogs(new Point3D(wolf.posX, wolf.posY, wolf.posZ), wolf.world, wolf.isChild());
		wolf.setDead();
	}

	public void doOverwriteCat(EntityOcelot cat) {
		// Biome biome = cat.world.getBiome(cat.getPosition());
		// logger.info(String.format("Spawning cat in %s biome ",
		// biome.getBiomeName()));
		MCA.naturallySpawnCats(new Point3D(cat.posX, cat.posY, cat.posZ), cat.world, cat.isChild());
		cat.setDead();
	}

	public void doOverwriteVillager(EntityVillager villager) {
		Biome biome = villager.world.getBiome(villager.getPos());
		logger.info(String.format("Spawning villager in %s biome ", biome.getBiomeName()));
		if (villager.getProfession() % 5 == 5) {
			if (biome.getBiomeName().toLowerCase().contains("swamp")) {
				doOverwriteVillagerWithOrc(villager);
			}
			else if (biome.getBiomeName().toLowerCase().contains("forest")) {
				doOverwriteVillagerWithElf(villager);
			}
		}
		else {
			villager.setDead();
			MCA.naturallySpawnVillagers(new Point3D(villager.posX, villager.posY, villager.posZ), villager.world,
					villager.getProfession() % 5);
		}
	}

	public void doOverwriteVillagerWithElf(EntityVillager villager) {
		villager.setDead();
		MCA.naturallySpawnElves(new Point3D(villager.posX, villager.posY, villager.posZ), villager.world,
				villager.getProfession());
	}

	public void doOverwriteVillagerWithOrc(EntityVillager villager) {
		villager.setDead();
		MCA.naturallySpawnOrcs(new Point3D(villager.posX, villager.posY, villager.posZ), villager.world,
				villager.getProfession());
	}

	public void doOverwriteWitchWithPrettyWitch(EntityWitch witch) {
		witch.setDead();
		
		MCA.naturallySpawnWitches(RadixLogic.getBooleanWithProbability(25) ? EnumGender.MALE : EnumGender.FEMALE,
				new Point3D(witch.posX, witch.posY, witch.posZ), witch.world);
	}

	public static void setReaperSummonPoint(World world, Point3D point) {
		summonWorld = world;
		summonPos = point;
		summonCounter = Time.SECOND * 6;
	}

	private int getNumberOfGuardsFromEntityList(List<EntityVillagerMCA> entityList) {
		int returnValue = 0;

		for (EntityVillagerMCA entity : entityList) {
			if (entity.attributes.getProfessionSkinGroup() == EnumProfessionSkinGroup.Guard) {
				returnValue++;
			}
		}

		return returnValue;
	}
}
