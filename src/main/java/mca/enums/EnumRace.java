package mca.enums;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public enum EnumRace {
	Unassigned(-1, EnumRaceSkinGroup.Unassigned),
	Villager(0, EnumRaceSkinGroup.Unassigned),
	Elf(1, EnumRaceSkinGroup.Elf),
	Orc(2, EnumRaceSkinGroup.Orc);
	private static Logger logger = LogManager.getLogger(EnumRace.class);
	private int id;
	private EnumRaceSkinGroup skinGroup;

	EnumRace(int id, EnumRaceSkinGroup skinGroup) {
		this.setId(id);
		this.setSkinGroup(skinGroup);
	}

	public EnumRaceSkinGroup getSkinGroup() {
		return skinGroup;
	}

	public static List<Integer> getListOfIds() {
		List<Integer> returnList = new ArrayList<Integer>();

		for (EnumRace race : EnumRace.values()) {
			if (race != Unassigned) {
				returnList.add(race.getId());
			}
		}

		return returnList;
	}

	public String getLocalizationId() {
		switch (this) {
			case Orc:
				return "race.orc";
			case Elf:
				return "race.elf";
			default:
				return "race.villager";
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setSkinGroup(EnumRaceSkinGroup skinGroup) {
		this.skinGroup = skinGroup;
	}

	public static EnumRace getRaceById(int id) {
		for (EnumRace race : EnumRace.values()) {
			if (race.getId() == id) {
				return race;
			}
		}

		return Villager;
	}

	public String getUserFriendlyForm(EntityVillagerMCA human) {
		//Player children have the "child" profession. Change their display title to "Villager" if they're grown up.
				//All children will show the "Child" title, regardless of underlying profession. 
		//When grown, their actual profession title will be shown.
			return MCA.getLocalizer().getString(getLocalizationId());
		
	}
}
