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
	Guard(5),
	Child(0),
	Librarian(1),
	Miner(3),
	Priest(2),
	Smith(3),
	Warrior(5)/*,
	Elf(3),
	Orc(4)*/;

//	List<String> villagerSkinList;
//	List<String> orkSkinList;
//	List<String> elfSkinList;

	SkinManager villagerSkins;
	SkinManager elfSkins;
	SkinManager orcSkins;
	int vanillaId;


	class SkinManager {
		List<String> completeSkinList = new ArrayList<String>();
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

	public void addSkin(String filePath, EnumRace race) {
		String resourceLocation = filePath.replace("/assets/mca/", "mca:");
		String professionName = this.toString().toLowerCase();
		SkinManager skinManager;
		if(race == EnumRace.Orc) {
			if(filePath.toLowerCase().contains(race.toString().toLowerCase())) {
				professionName = race.toString().toLowerCase();
			}
			skinManager = orcSkins;
		} else if(race == EnumRace.Elf) {
			if(filePath.toLowerCase().contains(race.toString().toLowerCase())) {
				professionName = race.toString().toLowerCase();
			}
			skinManager = elfSkins;
		} else {
			skinManager = villagerSkins;
		}
		skinManager.completeSkinList.add(resourceLocation);
		String genderChar = resourceLocation.replace("mca:textures/skins/" + professionName, "").substring(0, 1);

		if (genderChar.equals("m")) {
			skinManager.maleSkinList.add(resourceLocation);
		} else if (genderChar.equals("f")) {
			skinManager.femaleSkinList.add(resourceLocation);
		}
	}

	public String getSkin(boolean isMale, EnumRace race) {
		SkinManager skinManager;
		if(race == EnumRace.Orc) {
			skinManager = orcSkins;
		} else if(race == EnumRace.Elf) {
			skinManager = elfSkins;
		} else {
			skinManager = villagerSkins;
		}
		List<String> skinList = isMale ? skinManager.maleSkinList : skinManager.femaleSkinList;
		String skin = new String();
		try {
			skin = skinList.get(RadixMath.getNumberInRange(0, skinList.size() - 1));
		} catch (Exception e) {
			LogManager.getLogger(this.getClass())
					.error("Unable to generate random skin for skin group <" + this.toString() + ">" + "!");
			LogManager.getLogger(this.getClass()).error(e);

			skin = "";
		}
		return skin;
	}

	public List<String> getSkinList(boolean isMale, EnumRace race) {
		SkinManager skinManager;
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

	public int getVanillaProfessionId() {
		return vanillaId;
	}
}
