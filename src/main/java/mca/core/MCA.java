package mca.core;

import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.icu.text.MessageFormat;

import mca.api.CookableFood;
import mca.api.CropEntry;
import mca.api.FishingEntry;
import mca.api.MiningEntry;
import mca.api.RegistryMCA;
import mca.api.WeddingGift;
import mca.api.WoodcuttingEntry;
import mca.api.enums.EnumCropCategory;
import mca.api.enums.EnumGiftCategory;
import mca.command.CommandMCA;
import mca.core.forge.EventHooksFML;
import mca.core.forge.EventHooksForge;
import mca.core.forge.GuiHandler;
import mca.core.forge.ServerProxy;
import mca.core.minecraft.BlocksMCA;
import mca.core.minecraft.ItemsMCA;
import mca.core.radix.CrashWatcher;
import mca.data.NBTPlayerData;
import mca.data.PlayerDataCollection;
import mca.entity.EntityChoreFishHook;
import mca.entity.EntityElfMCA;
import mca.entity.EntityGrimReaper;
import mca.entity.EntityOrcMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.EntityWolfMCA;
import mca.enums.EnumGender;
import mca.enums.EnumProfession;
import mca.enums.EnumRace;
import mca.network.PacketHandlerMCA;
import mca.tile.TileMemorial;
import mca.tile.TileTombstone;
import mca.util.Either;
import mca.util.SkinLoader;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.core.ModMetadataEx;
import radixcore.core.RadixCore;
import radixcore.math.Point3D;
import radixcore.modules.RadixLogic;
import radixcore.modules.RadixMath;
import radixcore.modules.gen.SimpleOreGenerator;
import radixcore.modules.updates.NoUpdateProtocol;
import radixcore.modules.updates.RDXUpdateProtocol;

@Mod(modid = MCA.ID, name = MCA.NAME, version = MCA.VERSION, dependencies = "required-after:radixcore@[1.12.x-2.2.1,)", acceptedMinecraftVersions = "[1.12,1.12.2]", guiFactory = "mca.core.forge.client.MCAGuiFactory")
public class MCA {
	public static final String ID = "mca";
	public static final String NAME = "Minecraft Comes Alive";
	public static final String VERSION = "@VERSION@";

	@Instance(ID)
	private static MCA instance;
	private static ModMetadata metadata;
	private static CreativeTabs creativeTab;
	private static Config clientConfig;
	private static Config config;
	private static Localizer localizer;
	private static PacketHandlerMCA packetHandler;
	private static CrashWatcher crashWatcher;
	private static long orcMatingSeasonStart;

	private static Logger logger = LogManager.getLogger(MCA.class);

	@SidedProxy(clientSide = "mca.core.forge.ClientProxy", serverSide = "mca.core.forge.ServerProxy")
	public static ServerProxy proxy;

	@SideOnly(Side.CLIENT)
	public static NBTPlayerData myPlayerData;
	@SideOnly(Side.CLIENT)
	public static Point3D destinyCenterPoint;
	@SideOnly(Side.CLIENT)
	public static boolean destinySpawnFlag;
	@SideOnly(Side.CLIENT)
	public static boolean reloadLanguage;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		instance = this;
		java.util.logging.LogManager.getLogManager().reset();
		java.util.logging.Logger.getLogger("global").setLevel(java.util.logging.Level.FINEST);
		java.util.logging.Logger.getLogger(this.getClass().getName()).setLevel(java.util.logging.Level.FINEST);
		LogManager.getRootLogger().debug(MessageFormat.format("Pre-Initialization... FMLPreInitializationEvent: {0}", event));
		metadata = event.getModMetadata();
		// logger = event.getModLog();
		config = new Config(event);
		clientConfig = config;
		localizer = new Localizer(event);
		crashWatcher = new CrashWatcher();
		packetHandler = new PacketHandlerMCA(ID);
		proxy.registerEntityRenderers();
		proxy.registerEventHandlers();

		creativeTab = new CreativeTabs("MCA") {
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(ItemsMCA.ENGAGEMENT_RING);
			}
		};

		ModMetadataEx exData = ModMetadataEx.getFromModMetadata(metadata);
		exData.updateProtocol = config.allowUpdateChecking ? new RDXUpdateProtocol() : new NoUpdateProtocol();
		exData.packetHandler = packetHandler;

		RadixCore.registerMod(exData);

		if (exData.updateProtocol == null) {
			logger.warn("Update checking is turned off. You will not be notified of any available updates for MCA.");
		}

		MinecraftForge.EVENT_BUS.register(new EventHooksForge());
		MinecraftForge.EVENT_BUS.register(new EventHooksFML());
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		startOrcMatingSeason();
		proxy.registerModelMeshers();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

		SkinLoader.loadSkins();

		// Entity registry
		EntityRegistry.registerModEntity(new ResourceLocation(ID, "VillagerMCA"), EntityVillagerMCA.class,
				EntityVillagerMCA.class.getSimpleName(), config.baseEntityId, this, 50, 2, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ID, "OrcMCA"), EntityOrcMCA.class,
				EntityOrcMCA.class.getSimpleName(), config.baseEntityId, this, 50, 2, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ID, "WolfMCA"), EntityWolfMCA.class,
				EntityWolfMCA.class.getSimpleName(), config.baseEntityId, this, 50, 2, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ID, "ElfMCA"), EntityElfMCA.class,
				EntityElfMCA.class.getSimpleName(), config.baseEntityId, this, 50, 2, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ID, "FishHookMCA"), EntityChoreFishHook.class,
				EntityChoreFishHook.class.getSimpleName(), config.baseEntityId + 1, this, 50, 2, true);
		EntityRegistry.registerModEntity(new ResourceLocation(ID, "GrimReaperMCA"), EntityGrimReaper.class,
				EntityGrimReaper.class.getSimpleName(), config.baseEntityId + 2, this, 50, 2, true);

		// Tile registry
		GameRegistry.registerTileEntity(TileTombstone.class, TileTombstone.class.getSimpleName());
		GameRegistry.registerTileEntity(TileMemorial.class, TileMemorial.class.getSimpleName());

		// Smeltings
		GameRegistry.addSmelting(BlocksMCA.rose_gold_ore, new ItemStack(ItemsMCA.ROSE_GOLD_INGOT), 5.0F);

		if (MCA.config.roseGoldSpawnWeight > 0) {
			SimpleOreGenerator.register(new SimpleOreGenerator(BlocksMCA.rose_gold_ore, 6, 12, 40, true, false),
					MCA.config.roseGoldSpawnWeight);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		RegistryMCA.addObjectAsGift(Items.WOODEN_SWORD, 3);
		RegistryMCA.addObjectAsGift(Items.WOODEN_AXE, 3);
		RegistryMCA.addObjectAsGift(Items.WOODEN_HOE, 3);
		RegistryMCA.addObjectAsGift(Items.WOODEN_SHOVEL, 3);
		RegistryMCA.addObjectAsGift(Items.STONE_SWORD, 5);
		RegistryMCA.addObjectAsGift(Items.STONE_AXE, 5);
		RegistryMCA.addObjectAsGift(Items.STONE_HOE, 5);
		RegistryMCA.addObjectAsGift(Items.STONE_SHOVEL, 5);
		RegistryMCA.addObjectAsGift(Items.WOODEN_PICKAXE, 3);
		RegistryMCA.addObjectAsGift(Items.BEEF, 2);
		RegistryMCA.addObjectAsGift(Items.CHICKEN, 2);
		RegistryMCA.addObjectAsGift(Items.PORKCHOP, 2);
		RegistryMCA.addObjectAsGift(Items.LEATHER, 2);
		RegistryMCA.addObjectAsGift(Items.LEATHER_CHESTPLATE, 5);
		RegistryMCA.addObjectAsGift(Items.LEATHER_HELMET, 5);
		RegistryMCA.addObjectAsGift(Items.LEATHER_LEGGINGS, 5);
		RegistryMCA.addObjectAsGift(Items.LEATHER_BOOTS, 5);
		RegistryMCA.addObjectAsGift(Items.REEDS, 2);
		RegistryMCA.addObjectAsGift(Items.WHEAT_SEEDS, 2);
		RegistryMCA.addObjectAsGift(Items.WHEAT, 3);
		RegistryMCA.addObjectAsGift(Items.BREAD, 6);
		RegistryMCA.addObjectAsGift(Items.COAL, 5);
		RegistryMCA.addObjectAsGift(Items.SUGAR, 5);
		RegistryMCA.addObjectAsGift(Items.CLAY_BALL, 2);
		RegistryMCA.addObjectAsGift(Items.DYE, 1);
		RegistryMCA.addObjectAsGift(Items.COOKED_BEEF, 7);
		RegistryMCA.addObjectAsGift(Items.COOKED_CHICKEN, 7);
		RegistryMCA.addObjectAsGift(Items.COOKED_PORKCHOP, 7);
		RegistryMCA.addObjectAsGift(Items.COOKIE, 10);
		RegistryMCA.addObjectAsGift(Items.MELON, 10);
		RegistryMCA.addObjectAsGift(Items.MELON_SEEDS, 5);
		RegistryMCA.addObjectAsGift(Items.IRON_HELMET, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_CHESTPLATE, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_LEGGINGS, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_BOOTS, 10);
		RegistryMCA.addObjectAsGift(Items.CAKE, 12);
		RegistryMCA.addObjectAsGift(Items.IRON_SWORD, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_AXE, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_HOE, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_PICKAXE, 10);
		RegistryMCA.addObjectAsGift(Items.IRON_SHOVEL, 10);
		RegistryMCA.addObjectAsGift(Items.FISHING_ROD, 3);
		RegistryMCA.addObjectAsGift(Items.BOW, 5);
		RegistryMCA.addObjectAsGift(Items.BOOK, 5);
		RegistryMCA.addObjectAsGift(Items.BUCKET, 3);
		RegistryMCA.addObjectAsGift(Items.MILK_BUCKET, 5);
		RegistryMCA.addObjectAsGift(Items.WATER_BUCKET, 2);
		RegistryMCA.addObjectAsGift(Items.LAVA_BUCKET, 2);
		RegistryMCA.addObjectAsGift(Items.MUSHROOM_STEW, 5);
		RegistryMCA.addObjectAsGift(Items.PUMPKIN_SEEDS, 8);
		RegistryMCA.addObjectAsGift(Items.FLINT_AND_STEEL, 4);
		RegistryMCA.addObjectAsGift(Items.REDSTONE, 5);
		RegistryMCA.addObjectAsGift(Items.BOAT, 4);
		RegistryMCA.addObjectAsGift(Items.OAK_DOOR, 4);
		RegistryMCA.addObjectAsGift(Items.IRON_DOOR, 6);
		RegistryMCA.addObjectAsGift(Items.MINECART, 7);
		RegistryMCA.addObjectAsGift(Items.FLINT, 2);
		RegistryMCA.addObjectAsGift(Items.GOLD_NUGGET, 4);
		RegistryMCA.addObjectAsGift(Items.GOLD_INGOT, 20);
		RegistryMCA.addObjectAsGift(Items.IRON_INGOT, 10);
		RegistryMCA.addObjectAsGift(Items.DIAMOND, 30);
		RegistryMCA.addObjectAsGift(Items.MAP, 10);
		RegistryMCA.addObjectAsGift(Items.CLOCK, 5);
		RegistryMCA.addObjectAsGift(Items.COMPASS, 5);
		RegistryMCA.addObjectAsGift(Items.BLAZE_ROD, 10);
		RegistryMCA.addObjectAsGift(Items.BLAZE_POWDER, 5);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_SWORD, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_AXE, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_SHOVEL, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_HOE, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_HELMET, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_CHESTPLATE, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_LEGGINGS, 15);
		RegistryMCA.addObjectAsGift(Items.DIAMOND_BOOTS, 15);
		RegistryMCA.addObjectAsGift(Items.PAINTING, 6);
		RegistryMCA.addObjectAsGift(Items.ENDER_PEARL, 5);
		RegistryMCA.addObjectAsGift(Items.ENDER_EYE, 10);
		RegistryMCA.addObjectAsGift(Items.POTIONITEM, 3);
		RegistryMCA.addObjectAsGift(Items.SLIME_BALL, 3);
		RegistryMCA.addObjectAsGift(Items.SADDLE, 5);
		RegistryMCA.addObjectAsGift(Items.GUNPOWDER, 7);
		RegistryMCA.addObjectAsGift(Items.GOLDEN_APPLE, 25);
		RegistryMCA.addObjectAsGift(Items.RECORD_11, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_13, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_WAIT, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_CAT, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_CHIRP, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_FAR, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_MALL, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_MELLOHI, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_STAL, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_STRAD, 15);
		RegistryMCA.addObjectAsGift(Items.RECORD_WARD, 15);
		RegistryMCA.addObjectAsGift(Items.EMERALD, 25);
		RegistryMCA.addObjectAsGift(Blocks.RED_FLOWER, 5);
		RegistryMCA.addObjectAsGift(Blocks.YELLOW_FLOWER, 5);
		RegistryMCA.addObjectAsGift(Blocks.PLANKS, 5);
		RegistryMCA.addObjectAsGift(Blocks.LOG, 3);
		RegistryMCA.addObjectAsGift(Blocks.PUMPKIN, 3);
		RegistryMCA.addObjectAsGift(Blocks.CHEST, 5);
		RegistryMCA.addObjectAsGift(Blocks.WOOL, 2);
		RegistryMCA.addObjectAsGift(Blocks.IRON_ORE, 4);
		RegistryMCA.addObjectAsGift(Blocks.GOLD_ORE, 7);
		RegistryMCA.addObjectAsGift(Blocks.REDSTONE_ORE, 3);
		RegistryMCA.addObjectAsGift(Blocks.RAIL, 3);
		RegistryMCA.addObjectAsGift(Blocks.DETECTOR_RAIL, 5);
		RegistryMCA.addObjectAsGift(Blocks.ACTIVATOR_RAIL, 5);
		RegistryMCA.addObjectAsGift(Blocks.FURNACE, 5);
		RegistryMCA.addObjectAsGift(Blocks.CRAFTING_TABLE, 5);
		RegistryMCA.addObjectAsGift(Blocks.LAPIS_BLOCK, 15);
		RegistryMCA.addObjectAsGift(Blocks.BOOKSHELF, 7);
		RegistryMCA.addObjectAsGift(Blocks.GOLD_BLOCK, 50);
		RegistryMCA.addObjectAsGift(Blocks.IRON_BLOCK, 25);
		RegistryMCA.addObjectAsGift(Blocks.DIAMOND_BLOCK, 100);
		RegistryMCA.addObjectAsGift(Blocks.BREWING_STAND, 12);
		RegistryMCA.addObjectAsGift(Blocks.ENCHANTING_TABLE, 25);
		RegistryMCA.addObjectAsGift(Blocks.BRICK_BLOCK, 15);
		RegistryMCA.addObjectAsGift(Blocks.OBSIDIAN, 15);
		RegistryMCA.addObjectAsGift(Blocks.PISTON, 10);
		RegistryMCA.addObjectAsGift(Blocks.GLOWSTONE, 10);
		RegistryMCA.addObjectAsGift(Blocks.EMERALD_BLOCK, 100);
		RegistryMCA.addObjectAsGift(BlocksMCA.rose_gold_block, 35);
		RegistryMCA.addObjectAsGift(BlocksMCA.rose_gold_ore, 7);
		RegistryMCA.addObjectAsGift(Blocks.REDSTONE_BLOCK, 20);

		RegistryMCA.addFishingEntryToFishingAI(0, new FishingEntry(Items.FISH));
		RegistryMCA.addFishingEntryToFishingAI(1,
				new FishingEntry(Items.FISH, ItemFishFood.FishType.CLOWNFISH.getMetadata()));
		RegistryMCA.addFishingEntryToFishingAI(2,
				new FishingEntry(Items.FISH, ItemFishFood.FishType.COD.getMetadata()));
		RegistryMCA.addFishingEntryToFishingAI(3,
				new FishingEntry(Items.FISH, ItemFishFood.FishType.PUFFERFISH.getMetadata()));
		RegistryMCA.addFishingEntryToFishingAI(4,
				new FishingEntry(Items.FISH, ItemFishFood.FishType.SALMON.getMetadata()));

		if (getConfig().additionalGiftItems.length > 0) {
			for (String entry : getConfig().additionalGiftItems) {
				try {
					String[] split = entry.split("\\|");
					int heartsValue = Integer.parseInt(split[1]);
					String itemName = split[0];

					if (!itemName.startsWith("#")) {
						Object item = Item.REGISTRY.getObject(new ResourceLocation(itemName));
						Object block = Block.REGISTRY.getObject(new ResourceLocation(itemName));
						Object addObject = item != null ? item : block != null ? block : null;

						if (addObject != null) {
							RegistryMCA.addObjectAsGift(addObject, heartsValue);
							logger.info("Successfully added " + itemName + " with hearts value of " + heartsValue
									+ " to gift registry.");
						}
						else {
							logger.error("Failed to find item by name provided. Gift entry not created: " + entry);
						}
					}
				}
				catch (Exception e) {
					logger.error(
							"Failed to add additional gift due to error. Use <item name>|<hearts value>: " + entry);
				}
			}
		}

		RegistryMCA.addBlockToMiningAI(1, new MiningEntry(Blocks.COAL_ORE, Items.COAL, 0.45F));
		RegistryMCA.addBlockToMiningAI(2, new MiningEntry(Blocks.IRON_ORE, 0.4F));
		RegistryMCA.addBlockToMiningAI(3, new MiningEntry(Blocks.LAPIS_ORE, new ItemStack(Items.DYE, 1, 4), 0.3F));
		RegistryMCA.addBlockToMiningAI(4, new MiningEntry(Blocks.GOLD_ORE, 0.05F));
		RegistryMCA.addBlockToMiningAI(5, new MiningEntry(Blocks.DIAMOND_ORE, Items.DIAMOND, 0.04F));
		RegistryMCA.addBlockToMiningAI(6, new MiningEntry(Blocks.EMERALD_ORE, Items.EMERALD, 0.03F));
		RegistryMCA.addBlockToMiningAI(7, new MiningEntry(Blocks.QUARTZ_ORE, Items.QUARTZ, 0.02F));
		RegistryMCA.addBlockToMiningAI(8, new MiningEntry(BlocksMCA.rose_gold_ore, 0.07F));

		RegistryMCA.addBlockToWoodcuttingAI(1, new WoodcuttingEntry(Blocks.LOG, 0, Blocks.SAPLING, 0));
		RegistryMCA.addBlockToWoodcuttingAI(2, new WoodcuttingEntry(Blocks.LOG, 1, Blocks.SAPLING, 1));
		RegistryMCA.addBlockToWoodcuttingAI(3, new WoodcuttingEntry(Blocks.LOG, 2, Blocks.SAPLING, 2));
		RegistryMCA.addBlockToWoodcuttingAI(4, new WoodcuttingEntry(Blocks.LOG, 3, Blocks.SAPLING, 3));
		RegistryMCA.addBlockToWoodcuttingAI(5, new WoodcuttingEntry(Blocks.LOG2, 0, Blocks.SAPLING, 4));
		RegistryMCA.addBlockToWoodcuttingAI(6, new WoodcuttingEntry(Blocks.LOG2, 1, Blocks.SAPLING, 5));

		RegistryMCA.addEntityToHuntingAI(EntitySheep.class);
		RegistryMCA.addEntityToHuntingAI(EntityCow.class);
		RegistryMCA.addEntityToHuntingAI(EntityPig.class);
		RegistryMCA.addEntityToHuntingAI(EntityChicken.class);
		RegistryMCA.addEntityToHuntingAI(EntityOcelot.class, false);
		RegistryMCA.addEntityToHuntingAI(EntityWolf.class, false);

		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.PORKCHOP, Items.COOKED_PORKCHOP));
		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.BEEF, Items.COOKED_BEEF));
		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.CHICKEN, Items.COOKED_CHICKEN));
		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.FISH, Items.COOKED_FISH));
		RegistryMCA.addFoodToCookingAI(new CookableFood(Items.POTATO, Items.BAKED_POTATO));

		RegistryMCA.addCropToFarmingAI(1, new CropEntry(EnumCropCategory.WHEAT, Blocks.WHEAT, Items.WHEAT_SEEDS,
				Blocks.WHEAT, 7, Items.WHEAT, 1, 4));
		RegistryMCA.addCropToFarmingAI(2, new CropEntry(EnumCropCategory.WHEAT, Blocks.POTATOES, Items.POTATO,
				Blocks.POTATOES, 7, Items.POTATO, 1, 4));
		RegistryMCA.addCropToFarmingAI(3, new CropEntry(EnumCropCategory.WHEAT, Blocks.CARROTS, Items.CARROT,
				Blocks.CARROTS, 7, Items.CARROT, 1, 4));
		RegistryMCA.addCropToFarmingAI(4, new CropEntry(EnumCropCategory.WHEAT, Blocks.BEETROOTS, Items.BEETROOT_SEEDS,
				Blocks.BEETROOTS, 7, Items.BEETROOT, 1, 4));
		RegistryMCA.addCropToFarmingAI(5, new CropEntry(EnumCropCategory.MELON, Blocks.MELON_STEM, Items.MELON_SEEDS,
				Blocks.MELON_BLOCK, 0, Items.MELON, 2, 6));
		RegistryMCA.addCropToFarmingAI(6, new CropEntry(EnumCropCategory.MELON, Blocks.PUMPKIN_STEM,
				Items.PUMPKIN_SEEDS, Blocks.PUMPKIN, 0, null, 1, 1));
		RegistryMCA.addCropToFarmingAI(7, new CropEntry(EnumCropCategory.SUGARCANE, Blocks.REEDS, Items.REEDS,
				Blocks.REEDS, 0, Items.REEDS, 1, 1));

		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.DIRT, 1, 6), EnumGiftCategory.BAD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.DEADBUSH, 1, 1), EnumGiftCategory.BAD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.CACTUS, 1, 3), EnumGiftCategory.BAD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.STICK, 1, 4), EnumGiftCategory.BAD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.ROTTEN_FLESH, 1, 4), EnumGiftCategory.BAD);

		RegistryMCA.addWeddingGift(new WeddingGift(Items.CLAY_BALL, 4, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.STONE_AXE, 1, 1), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.STONE_SWORD, 1, 1), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.STONE_SHOVEL, 1, 1), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.APPLE, 1, 4), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.ARROW, 8, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.STONE_PICKAXE, 1, 1), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.BOOK, 1, 2), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.REDSTONE, 8, 32), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COOKED_PORKCHOP, 3, 6), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COOKED_BEEF, 3, 6), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COOKED_CHICKEN, 3, 6), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.BREAD, 1, 3), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.PLANKS, 2, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.LOG, 2, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.COBBLESTONE, 2, 16), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COAL, 2, 8), EnumGiftCategory.GOOD);
		RegistryMCA.addWeddingGift(new WeddingGift(ItemsMCA.BOOK_ROSE_GOLD, 1, 1), EnumGiftCategory.BEST);

		RegistryMCA.addWeddingGift(new WeddingGift(Items.CLAY_BALL, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_AXE, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_SWORD, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_SHOVEL, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.ARROW, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_PICKAXE, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.REDSTONE, 8, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COOKED_PORKCHOP, 6, 8), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COOKED_BEEF, 6, 8), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COOKED_CHICKEN, 6, 8), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.PLANKS, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.LOG, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.COBBLESTONE, 16, 32), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COAL, 10, 16), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_HELMET, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_CHESTPLATE, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_BOOTS, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_LEGGINGS, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.MELON, 4, 8), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.BOOKSHELF, 2, 4), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.IRON_INGOT, 8, 16), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(ItemsMCA.BOOK_INFECTION, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(ItemsMCA.BOOK_ROMANCE, 1, 1), EnumGiftCategory.BETTER);
		RegistryMCA.addWeddingGift(new WeddingGift(ItemsMCA.BOOK_FAMILY, 1, 1), EnumGiftCategory.BETTER);

		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.BRICK_BLOCK, 32, 32), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_AXE, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_SWORD, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_SHOVEL, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.ARROW, 64, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_PICKAXE, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.PLANKS, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.LOG, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.COBBLESTONE, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.COAL, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_LEGGINGS, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_HELMET, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_BOOTS, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND_CHESTPLATE, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.ENDER_EYE, 4, 8), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.ENCHANTING_TABLE, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.MOSSY_COBBLESTONE, 32, 64), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.DIAMOND, 8, 16), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.JUKEBOX, 1, 1), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.DIAMOND_BLOCK, 1, 2), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.GOLD_BLOCK, 1, 4), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.IRON_BLOCK, 1, 8), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Blocks.OBSIDIAN, 4, 8), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(Items.EMERALD, 4, 6), EnumGiftCategory.BEST);
		RegistryMCA.addWeddingGift(new WeddingGift(ItemsMCA.BOOK_DEATH, 1, 1), EnumGiftCategory.BEST);
		startOrcMatingSeason();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		startOrcMatingSeason();
		event.registerServerCommand(new CommandMCA());
	}

	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event) {
	}

	public static MCA getInstance() {
		return instance;
	}

	public static Logger getLog() {
		return logger;
	}

	public static Config getConfig() {
		return config;
	}

	public static void setConfig(Config configObj) {
		config = configObj;
	}

	public static void resetConfig() {
		if (config != clientConfig) {
			logger.info("Resetting config to client-side values...");
			config = clientConfig;
		}
	}

	public static ModMetadata getMetadata() {
		return metadata;
	}

	public static CreativeTabs getCreativeTab() {
		return creativeTab;
	}

	public static void setCreativeTab(CreativeTabs tab) {
		creativeTab = tab;
	}

	public static Localizer getLocalizer() {
		return localizer;
	}

	public static PacketHandlerMCA getPacketHandler() {
		return packetHandler;
	}

	public static NBTPlayerData getPlayerData(EntityPlayer player) {
		if (!player.world.isRemote) {
			return PlayerDataCollection.get().getPlayerData(player.getUniqueID());
		} else {
			return myPlayerData;
		}
	}

	public static NBTPlayerData getPlayerData(World world, UUID uuid) {
		return PlayerDataCollection.get().getPlayerData(uuid);
	}

	public static void naturallySpawnOrcs(Point3D pointOfSpawn, World world, int originalProfession) {
		MCA.getLog().debug(String.format("Original Profession newly spawned orc: %d", originalProfession));
		boolean hasFamily = RadixLogic.getBooleanWithProbability(75);

		final EntityOrcMCA orc = new EntityOrcMCA(world);
		orc.attributes.setGender(EnumGender.MALE);
		orc.attributes.assignRandomName();
		orc.attributes.assignRandomSkin();
		orc.attributes.assignRandomPersonality();
		if (RadixLogic.getBooleanWithProbability(75)) {
			EntityWolf wolf = new EntityWolf(world);
			wolf.setPosition(orc.posX, orc.posY, orc.posZ);
			wolf.setTamed(false);
			wolf.setOwnerId(orc.getUniqueID());
			EntityAIBase aiFollowOwner = new EntityAIFollowOwner(wolf, 1.0D, 10.0F, 2.0F);
			wolf.tasks.addTask(1, aiFollowOwner);
			wolf.setCustomNameTag(String.format("%s's wolf", orc.getName()));
			orc.setPet(wolf);
			wolf.setDropItemsWhenDead(true);
			world.spawnEntity(wolf);
		}

		orc.setPosition(pointOfSpawn.dX(), pointOfSpawn.dY(), pointOfSpawn.dZ());
		if (hasFamily) {
			EntityVillagerMCA wench = new EntityOrcMCA(world);
			wench.attributes.setGender(EnumGender.FEMALE);
			wench.attributes.assignRandomName();
			wench.attributes.assignRandomSkin();
			wench.attributes.assignRandomPersonality();
			wench.setPosition(orc.posX, orc.posY, orc.posZ - 1);
			world.spawnEntity(wench);
			if (RadixLogic.getBooleanWithProbability(75)) {
				EntityOcelot cat = new EntityOcelot(world);
				cat.setPosition(wench.posX, wench.posY, wench.posZ);
				cat.setTamed(false);
				cat.setOwnerId(wench.getUniqueID());
				EntityAIBase aiFollowOwner = new EntityAIFollowOwner(cat, 1.0D, 10.0F, 2.0F);
				cat.tasks.addTask(1, aiFollowOwner);
				cat.setCustomNameTag(String.format("%s's cat", wench.getName()));
				wench.setPet(cat);
				world.spawnEntity(cat);
			}

			orc.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withL(wench));

			// Children
			for (int i = 0; i < 8; i++) {
				if (RadixLogic.getBooleanWithProbability(50)) {
					continue;
				}
				EntityOrcMCA brat = new EntityOrcMCA(world);
				boolean bratIsMale = RadixLogic.getBooleanWithProbability(75);
				wench.createChild(brat);
				brat.attributes.setGender(bratIsMale ? EnumGender.MALE : EnumGender.FEMALE);
				brat.attributes.assignRandomName();
				brat.attributes.assignRandomSkin();
				brat.attributes.assignRandomPersonality();
				brat.attributes.setMother(Either.<EntityVillagerMCA, EntityPlayer>withL(wench));
				brat.attributes.setFather(Either.<EntityVillagerMCA, EntityPlayer>withL(orc));
				brat.setGrowingAge(-100);
				brat.attributes.setIsChild(true);

				brat.setPosition(pointOfSpawn.dX(), pointOfSpawn.dY(), pointOfSpawn.dZ() + 1);
				world.spawnEntity(brat);
				if (RadixLogic.getBooleanWithProbability(75)) {
					EntityWolf wolf = new EntityWolf(world);
					// wolf.setGrowingAge(-100);
					wolf.setPosition(brat.posX, brat.posY, brat.posZ);
					// wolf.setGrowingAge(-100);
					wolf.setTamed(false);
					wolf.setOwnerId(brat.getUniqueID());
					EntityAIBase aiFollowOwner = new EntityAIFollowOwner(wolf, 1.0D, 10.0F, 2.0F);
					wolf.tasks.addTask(1, aiFollowOwner);
					wolf.setCustomNameTag(String.format("%s's wolf", brat.getName()));
					brat.setPet(wolf);
					wolf.getPassengers().add(brat);
					world.spawnEntity(wolf);
				}
			}
		}
		world.spawnEntity(orc);
	}

	public static void naturallySpawnElves(Point3D pointOfSpawn, World world, int originalProfession) {
		boolean hasFamily = RadixLogic.getBooleanWithProbability(20);

		final EntityElfMCA elf = new EntityElfMCA(world);
		elf.attributes.setGender(EnumGender.FEMALE);
		elf.attributes.assignRandomName();
		elf.attributes.assignRandomSkin();
		elf.attributes.assignRandomPersonality();

		elf.setPosition(pointOfSpawn.dX(), pointOfSpawn.dY(), pointOfSpawn.dZ());

		if (hasFamily) {
			final EntityVillagerMCA husband = new EntityElfMCA(world);
			husband.attributes.setGender(EnumGender.MALE);
			husband.attributes.assignRandomName();
			husband.attributes.assignRandomSkin();
			husband.attributes.assignRandomPersonality();
			husband.setPosition(elf.posX, elf.posY, elf.posZ - 1);
			world.spawnEntity(husband);

			elf.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withL(husband));

			final EntityVillagerMCA father = husband;
			final EntityVillagerMCA mother = elf;

			// Children
			for (int i = 0; i < 1; i++) {
				if (RadixLogic.getBooleanWithProbability(66)) {
					continue;
				}
				final EntityElfMCA child = new EntityElfMCA(world);
				child.attributes.assignRandomGender();
				child.attributes.assignRandomName();
				child.attributes.assignRandomSkin();
				child.attributes.assignRandomPersonality();
				child.attributes.setMother(Either.<EntityVillagerMCA, EntityPlayer>withL(mother));
				child.attributes.setFather(Either.<EntityVillagerMCA, EntityPlayer>withL(father));
				child.attributes.setIsChild(true);

				child.setPosition(pointOfSpawn.dX(), pointOfSpawn.dY(), pointOfSpawn.dZ() + 1);
				world.spawnEntity(child);
			}
		}

		world.spawnEntity(elf);
	}

	public static void naturallySpawnVillagers(Point3D pointOfSpawn, World world, int originalProfession) {
		// MCA.getLog().debug(String.format("Original Profession newly spawned villager:
		// %d", originalProfession));
		boolean hasFamily = RadixLogic.getBooleanWithProbability(20);
		boolean adult1IsMale = RadixLogic.getBooleanWithProbability(50);

		final EntityVillagerMCA villager = new EntityVillagerMCA(world);
		villager.attributes.setGender(adult1IsMale ? EnumGender.MALE : EnumGender.FEMALE);
		villager.attributes
				.setProfession(originalProfession != -1
						? EnumProfession.getNewProfessionFromVanilla(Math.abs(originalProfession % 5))
						: EnumProfession.getAtRandom());
		villager.attributes.assignRandomName();
		villager.attributes.assignRandomSkin();
		villager.attributes.assignRandomPersonality();

		villager.setPosition(pointOfSpawn.dX(), pointOfSpawn.dY(), pointOfSpawn.dZ());
//		EntityPlayer closestPlayer = villager.world.getClosestPlayerToEntity(villager, 500);
//		villager.sayRaw(String.format("Profession ID: %d ", originalProfession), closestPlayer);
		if (hasFamily) {
			final EntityVillagerMCA spouse = new EntityVillagerMCA(world);
			spouse.attributes.setGender(adult1IsMale ? EnumGender.FEMALE : EnumGender.MALE);
			int fatherCaste;
			int motherCaste;
			if (spouse.attributes.getGender() == EnumGender.MALE) {
				int caste = 0;
				try {
					caste = RadixMath.getNumberInRange(Math.abs(villager.getProfession()%5), 5)%5;
				}
				catch (Exception e) {
					caste = 0;
					e.printStackTrace();
				}
				if(caste < originalProfession) {
					logger.warn("");
				}
				fatherCaste = caste;
				motherCaste = originalProfession;
				spouse.setProfession(caste);
				spouse.attributes.setProfession(EnumProfession.getNewProfessionFromVanilla(caste));
			} else {
				int caste = 0;
				// I'm not letting the wife's caste exceed the husband's.
				try {
					caste = RadixMath.getNumberInRange(0, villager.getProfession())%5;
				}
				catch (Exception e) {
					caste = 0;
					e.printStackTrace();
				}
				if(caste > originalProfession) {
					logger.warn("");
				}
				fatherCaste = originalProfession;
				motherCaste = caste;
				spouse.setProfession(caste);
				spouse.attributes.setProfession(EnumProfession.getNewProfessionFromVanilla(caste));
			}
			spouse.attributes.assignRandomName();
			spouse.attributes.assignRandomSkin();
			spouse.attributes.assignRandomPersonality();
			spouse.setPosition(villager.posX, villager.posY, villager.posZ - 1);
			world.spawnEntity(spouse);

			villager.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withL(spouse));

			final EntityVillagerMCA father = adult1IsMale ? villager : spouse;
			final EntityVillagerMCA mother = father == villager ? spouse : villager;

			// Children
			for (int i = 0; i < 2; i++) {
				if (RadixLogic.getBooleanWithProbability(50)) {
					continue;
				}

				final EntityVillagerMCA child = new EntityVillagerMCA(world);
				child.attributes.assignRandomGender();
				child.attributes.assignRandomName(); 
				// child.attributes.assignRandomProfession();
				int childCaste;
				try {
					childCaste = new Random().nextInt((fatherCaste - motherCaste) + 1)
							+ motherCaste;
				}
				catch (Exception e) {
					childCaste = 0;
					e.printStackTrace();
				}
				child.setProfession(childCaste);
				child.attributes.setProfession(EnumProfession.getNewProfessionFromVanilla(childCaste));
//				child.sayRaw(String.format("Profession ID: %d ", childCaste), closestPlayer);
				child.attributes.assignRandomSkin();
				child.attributes.assignRandomPersonality();
				child.attributes.setMother(Either.<EntityVillagerMCA, EntityPlayer>withL(mother));
				child.attributes.setFather(Either.<EntityVillagerMCA, EntityPlayer>withL(father));
				child.attributes.setIsChild(true);

				child.setPosition(pointOfSpawn.dX(), pointOfSpawn.dY(), pointOfSpawn.dZ() + 1);
				world.spawnEntity(child);
			}
		}
		// Biome biome = villager.world.getBiome(villager.getPos());

//		villager.sayRaw(String.format("I was born in a %s biome ", biome.getBiomeName()), closestPlayer);
		world.spawnEntity(villager);
	}

	public static CrashWatcher getCrashWatcher() {
		return crashWatcher;
	}

	public static Entity getEntityByUUID(World world, UUID uuid) {
		for (Entity entity : world.loadedEntityList) {
			if (entity.getUniqueID() == uuid) {
				return entity;
			}
		}

		return null;
	}

	private static boolean matingSeasonStarted = true;
	public static void startOrcMatingSeason() {
		matingSeasonStarted = true;
		orcMatingSeasonStart = System.currentTimeMillis();
	}

	public static boolean isMyRacesMatingSeason(EnumRace race) {
		if (config.getSeasonalBreeders().contains(race)) {
			return isMatingSeason();
		}
		return true;
	}

	public static boolean isMatingSeason() {
		if (!matingSeasonStarted) {
			if (RadixLogic.getBooleanWithProbability(1)) {
				startOrcMatingSeason();
			}
			matingSeasonStarted = false;
		}
		else {
			long orcMatingSeasonDuration = config.getOrcMatingSeasonDuration() * 60000L;
			long matingSeasonTimeElapsed = (System.currentTimeMillis() - orcMatingSeasonStart);
			matingSeasonStarted = (matingSeasonTimeElapsed <= orcMatingSeasonDuration);
		}
		return matingSeasonStarted;
	}
}
