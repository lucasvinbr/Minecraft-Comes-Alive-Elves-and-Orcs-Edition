package mca.enums;

import org.apache.logging.log4j.LogManager;
import radixcore.datastructures.CyclicIntList;
import radixcore.modules.RadixMath;

import java.util.ArrayList;
import java.util.List;

public enum EnumRaceSkinGroup {
	Unassigned(-1),
	Villager(0),
	Elf(1),
	Orc(2);

	private List<String> completeSkinList;
	private List<String> maleSkinList;
	private List<String> femaleSkinList;
	private int vanillaId;

	private EnumRaceSkinGroup(int vanillaId) {
		this.completeSkinList = new ArrayList<String>();
		this.maleSkinList = new ArrayList<String>();
		this.femaleSkinList = new ArrayList<String>();
		this.vanillaId = vanillaId;
	}

	public void addSkin(String locationInJAR) {
		String resourceLocation = locationInJAR.replace("/assets/mca/", "mca:");
		completeSkinList.add(resourceLocation);

		String
				genderChar =
				resourceLocation.replace("mca:textures/skins/" + this.toString().toLowerCase(), "").substring(0, 1);

		if (genderChar.equals("m")) {
			maleSkinList.add(resourceLocation);
		} else if (genderChar.equals("f")) {
			femaleSkinList.add(resourceLocation);
		}
	}

	private String getSkin(boolean isMale) {
		List<String> skinList = isMale ? maleSkinList : femaleSkinList;

		try {
			return skinList.get(RadixMath.getNumberInRange(0, skinList.size() - 1));
		} catch (Exception e) {
			LogManager.getLogger(this.getClass())
					.error("Unable to generate random skin for skin group <" + this.toString() + ">" + "!");
			LogManager.getLogger(this.getClass()).error(e);

			return "";
		}
	}

	public List<String> getSkinList(boolean isMale) {
		return isMale ? maleSkinList : femaleSkinList;
	}

	public CyclicIntList getListOfSkinIDs(boolean isMale) {
		List<String> textureList = getSkinList(isMale);
		List<Integer> ids = new ArrayList<Integer>();

		for (String texture : textureList) {
			int id = Integer.parseInt(texture.replaceAll("[^\\d]", ""));
			ids.add(id);
		}

		return CyclicIntList.fromList(ids);
	}
}
