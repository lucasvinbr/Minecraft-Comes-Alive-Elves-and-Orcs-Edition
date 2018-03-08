package mca.enums;

import org.apache.logging.log4j.LogManager;
import radixcore.datastructures.CyclicIntList;
import radixcore.modules.RadixMath;

import java.util.ArrayList;
import java.util.List;

public enum EnumProfessionSkinGroup {
	Unassigned(-1),
	Farmer(0),
	Baker(0),
	Butcher(4),
	Guard(3),
	Child(0),
	Librarian(1),
	Miner(3),
	Priest(2),
	Smith(3),
	Warrior(3),
	Orc(5),
	Elf(5);

//	List<String> villagerSkinList;
//	List<String> orkSkinList;
//	List<String> elfSkinList;

	SkinManager villagerSkins;
	SkinManager elfSkins;
	SkinManager orcSkins;



	class SkinManager {
		List<String> completeSkinList;
		List<String> maleSkinList;
		List<String> femaleSkinList;
		int vanillaId;
		SkinManager(int vanillaId) {
			this.vanillaId = vanillaId;
			this.maleSkinList = new ArrayList<String>();
			this.femaleSkinList = new ArrayList<String>();
		}
	}

	EnumProfessionSkinGroup(int vanillaId) {
		villagerSkins = new SkinManager(vanillaId);
		elfSkins = new SkinManager(vanillaId);
		orcSkins = new SkinManager(vanillaId);
	}

	public void addSkin(String locationInJAR) {
		String resourceLocation = locationInJAR.replace("/assets/mca/", "mca:");
		SkinManager skinManager = null;
		if(resourceLocation.toLowerCase().contains("orc")) {
			skinManager = orcSkins;
		} else if(resourceLocation.toLowerCase().contains("elf")) {
			skinManager = elfSkins;
		} else {
			skinManager = villagerSkins;
		}
		skinManager.completeSkinList.add(resourceLocation);
		String genderChar = resourceLocation.replace("mca:textures/skins/" + this.toString().toLowerCase(), "").substring(0, 1);

		if (genderChar.equals("m")) {
			skinManager.maleSkinList.add(resourceLocation);
		} else if (genderChar.equals("f")) {
			skinManager.femaleSkinList.add(resourceLocation);
		}
	}

	String getSkin(boolean isMale, EnumRace race) {
		SkinManager skinManager = null;
		if(race == EnumRace.Orc) {
			skinManager = orcSkins;
		} else if(race == EnumRace.Elf) {
			skinManager = elfSkins;
		} else {
			skinManager = villagerSkins;
		}
		List<String> skinList = isMale ? skinManager.maleSkinList : skinManager.femaleSkinList;

		try {
			return skinList.get(RadixMath.getNumberInRange(0, skinList.size() - 1));
		} catch (Exception e) {
			LogManager.getLogger(this.getClass())
					.error("Unable to generate random skin for skin group <" + this.toString() + ">" + "!");
			LogManager.getLogger(this.getClass()).error(e);

			return "";
		}
	}

	public List<String> getSkinList(boolean isMale, EnumRace race) {
		SkinManager skinManager = null;
		if(race == EnumRace.Orc) {
			skinManager = orcSkins;
		} else if(race == EnumRace.Elf) {
			skinManager = elfSkins;
		} else {
			skinManager = villagerSkins;
		}
		return isMale ? skinManager.maleSkinList : skinManager.femaleSkinList;
	}

	public CyclicIntList getListOfSkinIDs(boolean isMale, EnumRace race) {
		List<String> textureList = getSkinList(isMale, race);
		List<Integer> ids = new ArrayList<Integer>();

		for (String texture : textureList) {
			int id = Integer.parseInt(texture.replaceAll("[^\\d]", ""));
			ids.add(id);
		}

		return CyclicIntList.fromList(ids);
	}

	public String getRandomMaleSkin(EnumRace race) {
		return getSkin(true, race);
	}

	public String getRandomFemaleSkin(EnumRace race) {
		return getSkin(false, race);
	}

//	public int getVanillaProfessionId() {
//		return vanillaId;
//	}
}
