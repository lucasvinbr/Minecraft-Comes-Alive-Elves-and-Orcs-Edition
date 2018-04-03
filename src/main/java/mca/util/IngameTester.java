package mca.util;

import java.text.MessageFormat;

import mca.actions.ActionStoryProgression;
import mca.core.Constants;
import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.entity.passive.EntityVillagerMCA;
import mca.enums.EnumBabyState;
import mca.enums.EnumGender;
import mca.enums.EnumMarriageState;
import mca.enums.EnumProfession;
import mca.enums.EnumProgressionStep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLLog;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;
import radixcore.constant.Time;
import radixcore.math.Point3D;
import radixcore.modules.RadixMath;

public class IngameTester {
	private IngameTester() {
	}

	public static void stressTest(EntityPlayer player) {
		org.apache.logging.log4j.LogManager.getLogger(IngameTester.class.getName()).log(
				org.apache.logging.log4j.Level.ALL,
				MessageFormat.format("Running in-game stress test: \r\nPlayer{0}", player));
		long startTime = System.currentTimeMillis();
		int i = 0;
		while (System.currentTimeMillis() - startTime < 333L) {
			String msg = String.format("Running stress test %d", i);
			org.apache.logging.log4j.LogManager.getLogger(IngameTester.class.getName())
					.log(org.apache.logging.log4j.Level.ALL, msg);
			addMessage(msg, player);
			EntityVillager villager = new EntityVillager(player.getEntityWorld(), RadixMath.getNumberInRange(0, 5));
			Point3D pointOfSpawn = new Point3D(player.posX, player.posY, player.posZ);
			int xCoord = RadixMath.getNumberInRange(pointOfSpawn.iX() - 64, pointOfSpawn.iX() + 64);
			int yCoord = player.getEntityWorld().getActualHeight() + 64;
			int zCoord = RadixMath.getNumberInRange(pointOfSpawn.iZ() - 64, pointOfSpawn.iZ() + 64);
			villager.setPosition(xCoord, yCoord, zCoord);
			player.getEntityWorld().spawnEntity(villager);
			// try {
			// run(player);
			// }
			// catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			i++;
		}
		String msg = String.format("Spawned %d villagers in %d ms.", i, (System.currentTimeMillis() - startTime));
		org.apache.logging.log4j.LogManager.getLogger(IngameTester.class.getName())
				.log(org.apache.logging.log4j.Level.ALL, msg);
		addMessage(msg, player);
		System.gc();
	}

	public static void run(EntityPlayer player) {

		EntityVillagerMCA adam = new EntityVillagerMCA(player.world);
		EntityVillagerMCA eve = new EntityVillagerMCA(player.world);
		adam.setPosition(player.posX + 2, player.posY, player.posZ);
		eve.setPosition(player.posX + 1, player.posY, player.posZ);
		player.world.spawnEntity(adam);
		player.world.spawnEntity(eve);

		for (int i = 0; i < 5000; i++) {
			adam.attributes.assignRandomGender();
			adam.attributes.assignRandomName();
			adam.attributes.assignRandomPersonality();
			// adam.attributes.assignRandomScale();
			adam.attributes.assignRandomSkin();

			try {
				assertTrue(!adam.attributes.getName().isEmpty());
				assertTrue(adam.attributes.getGender() != EnumGender.UNASSIGNED);
				assertTrue(adam.attributes.getProfessionEnum() != EnumProfession.Unassigned);
				assertTrue(!adam.attributes.getClothesTexture().isEmpty());
				assertTrue(!adam.attributes.getHeadTexture().isEmpty());
			}
			catch (AssertionError e) {
				e.printStackTrace();
				String msg = String.format("Assertion Error occurred!%nMessage: %s%n", e.getLocalizedMessage());
				FMLLog.severe(msg, e);
				// java.util.logging.LogManager.getLogManager().getLogger(IngameTester.class.getName())
				// .log(java.util.logging.Level.SEVERE, msg, e);
				org.apache.logging.log4j.LogManager.getLogger(IngameTester.class.getName()).error(msg, e);
				failTest("Villager creation", player);
				adam.setDead();
				eve.setDead();
				return;
			}
		}

		adam.attributes.setGender(EnumGender.MALE);
		eve.attributes.setGender(EnumGender.FEMALE);
		adam.attributes.setName("Adam");
		eve.attributes.setName("Eve");
		adam.attributes.assignRandomSkin();
		eve.attributes.assignRandomPersonality();
		eve.attributes.assignRandomProfession();
		eve.attributes.assignRandomSkin();

		passTest("Villager creation", player);

		try {
			adam.startMarriage(Either.<EntityVillagerMCA, EntityPlayer>withL(eve));

			assertTrue(adam.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_VILLAGER);
			assertTrue(adam.attributes.getSpouseGender() == eve.attributes.getGender());
			assertTrue(adam.attributes.getSpouseName().equals(eve.attributes.getName()));
			assertTrue(adam.attributes.getSpouseUUID() == eve.getPersistentID());
			assertTrue(eve.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_VILLAGER);
			assertTrue(eve.attributes.getSpouseGender() == adam.attributes.getGender());
			assertTrue(eve.attributes.getSpouseName().equals(adam.attributes.getName()));
			assertTrue(eve.attributes.getSpouseUUID() == adam.getPersistentID());
			assertTrue(adam.getBehavior(ActionStoryProgression.class).getIsDominant());
			assertTrue(!eve.getBehavior(ActionStoryProgression.class).getIsDominant());
			assertTrue(adam.getBehaviors().getAction(ActionStoryProgression.class)
					.getProgressionStep() == EnumProgressionStep.TRY_FOR_BABY);
			assertTrue(eve.getBehaviors().getAction(ActionStoryProgression.class)
					.getProgressionStep() == EnumProgressionStep.TRY_FOR_BABY);

			// Place us at the end of the story progression threshold
			adam.attributes.setTicksAlive(MCA.getConfig().storyProgressionThreshold * Time.MINUTE);
			eve.attributes.setTicksAlive(MCA.getConfig().storyProgressionThreshold * Time.MINUTE);

			boolean success = false;
			ActionStoryProgression story = adam.getBehavior(ActionStoryProgression.class);

			for (int i = 0; i < 50000; i++) {
				adam.getBehaviors().onUpdate();

				if (story.getProgressionStep() == EnumProgressionStep.HAD_BABY) {
					success = true;
					break;
				}
			}

			assertTrue(success);

			story = eve.getBehavior(ActionStoryProgression.class);
			assertTrue(story.getProgressionStep() == EnumProgressionStep.HAD_BABY);
			assertTrue(eve.attributes.getBabyState() != EnumBabyState.NONE);

			adam.endMarriage();
			eve.endMarriage();
			assertTrue(story.getProgressionStep() == EnumProgressionStep.HAD_BABY);
			assertTrue(eve.attributes.getBabyState() != EnumBabyState.NONE);
			assertTrue(adam.getBehavior(ActionStoryProgression.class)
					.getProgressionStep() == EnumProgressionStep.SEARCH_FOR_PARTNER);

			eve.attributes.setBabyState(EnumBabyState.NONE);
		}
		catch (AssertionError e) {
			e.printStackTrace();
			String msg = String.format("Assertion Error occurred!%nMessage: %s%n", e.getLocalizedMessage());
			FMLLog.severe(msg, e);
			org.apache.logging.log4j.LogManager.getLogger(IngameTester.class.getName()).error(msg, e);
			failTest("Villager marriage and story simulation", player);
			return;
		}

		passTest("Villager marriage and story simulation", player);

		try {
			NBTPlayerData playerData = MCA.getPlayerData(player);
			playerData.setSpouse(Either.<EntityVillagerMCA, EntityPlayer>withL(eve));

			assertTrue(eve.attributes.getMarriageState() == EnumMarriageState.MARRIED_TO_PLAYER);
			assertTrue(
					eve.getBehavior(ActionStoryProgression.class).getProgressionStep() == EnumProgressionStep.FINISHED);
			assertTrue(eve.attributes.getSpouseGender() == playerData.getGender());
			assertTrue(eve.attributes.getSpouseName().equals(player.getName()));
			assertTrue(eve.attributes.getSpouseUUID().equals(playerData.getUUID()));
			assertTrue(playerData.getSpouseGender() == eve.attributes.getGender());
			assertTrue(playerData.getSpouseUUID().equals(eve.getPersistentID()));
			assertTrue(playerData.getSpouseName().equals(eve.attributes.getName()));

			eve.endMarriage();
			playerData.setSpouse(null);

			assertTrue(eve.attributes.getMarriageState() == EnumMarriageState.NOT_MARRIED);
			assertTrue(eve.getBehavior(ActionStoryProgression.class)
					.getProgressionStep() == EnumProgressionStep.SEARCH_FOR_PARTNER);
			assertTrue(eve.attributes.getSpouseGender() == EnumGender.UNASSIGNED);
			assertTrue(eve.attributes.getSpouseUUID() == Constants.EMPTY_UUID);
			assertTrue(eve.attributes.getSpouseName().isEmpty());
			assertTrue(playerData.getSpouseGender() == EnumGender.UNASSIGNED);
			assertTrue(playerData.getSpouseUUID().equals(Constants.EMPTY_UUID));
			assertTrue(playerData.getSpouseName().isEmpty());
		}

		catch (AssertionError e) {
			e.printStackTrace();
			failTest("Player marriage", player);
			return;
		}

		passTest("Player marriage", player);
	}

	public static void addMessage(String message, EntityPlayer player) {
		player.sendMessage(new TextComponentString(
				Color.GOLD + "[" + Color.DARKRED + "MCA" + Color.GOLD + "] " + Format.RESET + message));
	}

	private static void passTest(String testName, EntityPlayer player) {
		addMessage("- " + testName + ": " + Color.GREEN + "[PASS]", player);
	}

	private static void failTest(String testName, EntityPlayer player) {
		addMessage("- " + testName + ": " + Color.RED + "[FAIL]", player);
	}

	private static void assertTrue(boolean expression) throws AssertionError {
		if (!expression) {
			throw new AssertionError();
		}
	}

	private static void assertFalse(boolean expression) throws AssertionError {
		if (expression) {
			throw new AssertionError();
		}
	}
}
