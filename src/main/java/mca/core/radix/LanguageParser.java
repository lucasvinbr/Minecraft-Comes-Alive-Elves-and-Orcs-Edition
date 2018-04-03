package mca.core.radix;

import mca.core.MCA;
import mca.data.NBTPlayerData;
import mca.data.PlayerMemory;
import mca.entity.passive.EntityVillagerMCA;
import mca.enums.EnumGender;
import mca.enums.EnumRelation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLLog;
import radixcore.modules.lang.AbstractLanguageParser;

public class LanguageParser extends AbstractLanguageParser {
	@SuppressWarnings("null")
	@Override
	public String parsePhrase(String unparsedPhrase, Object[] arguments) {
		int passes = 0;
		EntityVillagerMCA
				entitySpeaker =
				(EntityVillagerMCA) this.getArgumentOfType(arguments, EntityVillagerMCA.class, 1);
		EntityVillagerMCA
				entitySecondary =
				(EntityVillagerMCA) this.getArgumentOfType(arguments, EntityVillagerMCA.class, 2);
		EntityPlayer playerTarget = (EntityPlayer) this.getArgumentOfType(arguments, EntityPlayer.class);
		PlayerMemory
				memory =
				(entitySpeaker != null && playerTarget != null) ?
				entitySpeaker.attributes.getPlayerMemory(playerTarget) :
				null;

		//Allow at most 10 passes to avoid infinite loops.
		while (unparsedPhrase.contains("%")) {
			try {
				if (unparsedPhrase.contains("%Name%")) {
					unparsedPhrase = unparsedPhrase.replace("%Name%",
							entitySpeaker != null && entitySpeaker.attributes != null ? entitySpeaker.attributes.getName() : null);
				} else if (unparsedPhrase.contains("%Profession%")) {
					unparsedPhrase = unparsedPhrase.replace("%Profession%",
							entitySpeaker != null && entitySpeaker.attributes != null ?
							entitySpeaker.attributes.getProfessionEnum().getUserFriendlyForm(entitySpeaker) :
							null);
				} else if (unparsedPhrase.contains("%FatherName%")) {
					String parentName = entitySpeaker != null ? entitySpeaker.attributes.getParentNames() : null;
					unparsedPhrase =
							unparsedPhrase.replace("%FatherName%", parentName.subSequence(0,
									parentName != null ? parentName.indexOf("|") : 0));
				} else if (unparsedPhrase.contains("%MotherName%")) {
					String parentNames = entitySpeaker != null ? entitySpeaker.attributes.getParentNames() : null;
					unparsedPhrase =
							unparsedPhrase.replace("%MotherName%",
									parentNames.subSequence((parentNames != null ? parentNames.indexOf("|") : 0) + 1, parentNames.length()));
				} else if (unparsedPhrase.contains("%PlayerName%")) {
					try {
						NBTPlayerData data = MCA.getPlayerData(playerTarget);
						unparsedPhrase = unparsedPhrase.replace("%PlayerName%", data.getMcaName());
					} catch (Exception e) {
						unparsedPhrase = unparsedPhrase.replace("%PlayerName%",
								playerTarget != null ? playerTarget.getName() : null);
					}
				} else if (unparsedPhrase.contains("%ParentOpposite%")) {
					boolean isPlayerMale = MCA.getPlayerData(playerTarget).getGender() == EnumGender.MALE;

					if (isPlayerMale) {
						unparsedPhrase =
								unparsedPhrase.replace("%ParentOpposite%", MCA.getLocalizer().getString("parser.mom"));
					} else {
						unparsedPhrase =
								unparsedPhrase.replace("%ParentOpposite%", MCA.getLocalizer().getString("parser.dad"));
					}
				} else if (unparsedPhrase.contains("%ParentTitle%")) {
					boolean isPlayerMale = MCA.getPlayerData(playerTarget).getGender() == EnumGender.MALE;

					if (!isPlayerMale) {
						unparsedPhrase =
								unparsedPhrase.replace("%ParentTitle%", MCA.getLocalizer().getString("parser.mom"));
					} else {
						unparsedPhrase =
								unparsedPhrase.replace("%ParentTitle%", MCA.getLocalizer().getString("parser.dad"));
					}
				} else if (unparsedPhrase.contains("%RelationToPlayer%")) {
					EnumRelation relation = memory != null ? memory.getRelation() : null;
					unparsedPhrase =
							unparsedPhrase.replace("%RelationToPlayer%",
									MCA.getLocalizer().getString(relation != null ? relation.getPhraseId() : null));
				} else if (unparsedPhrase.contains("%a1%")) {
					unparsedPhrase = unparsedPhrase.replace("%a1%", arguments[0].toString());
				} else if (unparsedPhrase.contains("%a2%")) {
					unparsedPhrase = unparsedPhrase.replace("%a2%", arguments[1].toString());
				}
			} catch (Exception e) {
				String msg = String.format("Exception occurred!%nMessage: %s%n", e.getLocalizedMessage());
				FMLLog.severe(msg, e);
				// java.util.logging.LogManager.getLogManager().getLogger(this.getClass().getName()).severe(msg);
				org.apache.logging.log4j.LogManager.getLogger(this.getClass().getName()).error(msg, e);
				// java.util.logging.Logger.getLogger(this.getClass().getName()).severe(msg);
			} finally {
				passes++;

				if (passes >= 10) {
					Throwable t = new Throwable();
					t.printStackTrace();
					//noinspection ContinueOrBreakFromFinallyBlock
					break;
				}
			}
		}
		return unparsedPhrase;
	}
}
