package mca.network.packets;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;
import java.util.Map;

import mca.core.MCA;
import mca.core.io.WorldPropertiesManager;
import mca.core.util.object.PlayerMemory;
import mca.entity.AbstractEntity;
import mca.enums.EnumRelation;
import mca.enums.EnumTrait;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.radixshock.radixcore.network.ByteBufIO;
import com.radixshock.radixcore.network.packets.AbstractPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSetFieldValue extends AbstractPacket implements IMessage, IMessageHandler<PacketSetFieldValue, IMessage>
{
	private int entityId;
	private String fieldName;
	private Object fieldValue;
	
	public PacketSetFieldValue()
	{
	}
	
	public PacketSetFieldValue(int entityId, String fieldName, Object fieldValue)
	{
		this.entityId = entityId;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}
	
	@Override
	public void fromBytes(ByteBuf byteBuf) 
	{
		entityId = byteBuf.readInt();
		fieldName = (String)ByteBufIO.readObject(byteBuf);
		fieldValue = ByteBufIO.readObject(byteBuf);
	}

	@Override
	public void toBytes(ByteBuf byteBuf) 
	{
		byteBuf.writeInt(entityId);
		ByteBufIO.writeObject(byteBuf, fieldName);
		ByteBufIO.writeObject(byteBuf, fieldValue);
	}

	@Override
	public IMessage onMessage(PacketSetFieldValue packet, MessageContext context) 
	{
		final EntityPlayer player = getPlayer(context);
		final WorldPropertiesManager manager = MCA.getInstance().playerWorldManagerMap.get(player.getCommandSenderName());
		
		if (MCA.getInstance().debugDoLogPackets && MCA.getInstance().inDebugMode)
		{
			MCA.getInstance().getLogger().log("\t" + entityId + " | " + fieldName + " | " + fieldValue.toString());
		}

		for (Object obj : player.worldObj.loadedEntityList)
		{
			try
			{
				final Entity entity = (Entity)obj;

				if (entity.getEntityId() == entityId)
				{
					final AbstractEntity abstractEntity = (AbstractEntity)entity;

					for (final Field f : entity.getClass().getFields())
					{
						if (!fieldName.equals("texture"))
						{
							if (f.getName().equals(fieldName))
							{
								//Achievements
								if (f.getName().equals("isPeasant") && fieldValue.toString().equals("true"))
								{
									manager.worldProperties.stat_villagersMadePeasants++;
									player.triggerAchievement(MCA.getInstance().achievementMakePeasant);

									if (manager.worldProperties.stat_villagersMadePeasants >= 20)
									{
										player.triggerAchievement(MCA.getInstance().achievementPeasantArmy);
									}

									manager.saveWorldProperties();
								}

								if (f.getName().equals("isKnight") && fieldValue.toString().equals("true"))
								{
									manager.worldProperties.stat_guardsMadeKnights++;
									player.triggerAchievement(MCA.getInstance().achievementMakeKnight);

									if (manager.worldProperties.stat_guardsMadeKnights >= 20)
									{
										player.triggerAchievement(MCA.getInstance().achievementKnightArmy);
									}

									manager.saveWorldProperties();
								}

								if (f.getName().equals("hasBeenExecuted") && fieldValue.toString().equals("true"))
								{
									manager.worldProperties.stat_villagersExecuted++;
									player.triggerAchievement(MCA.getInstance().achievementExecuteVillager);

									if (abstractEntity.familyTree.getRelationOf(MCA.getInstance().getIdOfPlayer(player)) == EnumRelation.Spouse)
									{
										manager.worldProperties.stat_wivesExecuted++;

										if (manager.worldProperties.stat_wivesExecuted >= 6)
										{
											player.triggerAchievement(MCA.getInstance().achievementMonarchSecret);
										}
									}
									manager.saveWorldProperties();

								}
								//Setting the value.
								if (f.getType().getName().contains("boolean"))
								{
									entity.getClass().getField(fieldName).set(entity, Boolean.parseBoolean(fieldValue.toString()));

									//Special condition. When isSpouse is changed, a villager's AI must be updated just in case it is a guard who is
									//either getting married or getting divorced.
									if (f.getName().equals("isSpouse"))
									{
										abstractEntity.addAI();
									}
								}

								else if (f.getType().getName().contains("int"))
								{
									entity.getClass().getField(fieldName).set(entity, Integer.parseInt(fieldValue.toString()));

									if (f.getName().equals("traitId"))
									{
										abstractEntity.trait = EnumTrait.getTraitById(abstractEntity.traitId);
									}

									if (f.getName().equals("profession"))
									{
										abstractEntity.addAI();
									}
								}

								else if (f.getType().getName().contains("double"))
								{
									entity.getClass().getField(fieldName).set(entity, Double.parseDouble(fieldValue.toString()));
								}

								else if (f.getType().getName().contains("float"))
								{
									entity.getClass().getField(fieldName).set(entity, Float.parseFloat(fieldValue.toString()));

									abstractEntity.setMoodByMoodPoints(false);
								}

								else if (f.getType().getName().contains("String"))
								{
									entity.getClass().getField(fieldName).set(entity, fieldValue.toString());
								}

								else if (f.getType().getName().contains("Map"))
								{
									if (f.getName().equals("playerMemoryMap"))
									{
										//Player name must be set if the map is a memory map since it is transient.
										Map<String, PlayerMemory> memoryMap = (Map<String, PlayerMemory>)fieldValue;
										PlayerMemory memory = memoryMap.get(player.getCommandSenderName());

										if (memory != null)
										{
											memory.playerName = player.getCommandSenderName();
											memoryMap.put(player.getCommandSenderName(), memory);
										}

										else
										{
											memoryMap.put(player.getCommandSenderName(), new PlayerMemory(player.getCommandSenderName()));
										}

										entity.getClass().getField(fieldName).set(entity, memoryMap);
									}

									else
									{
										entity.getClass().getField(fieldName).set(entity, fieldValue);
									}
								}
							}
						}

						else
						{
							((AbstractEntity)entity).setTexture(fieldValue.toString());
						}
					}

					break;
				}
			}

			catch (Throwable e)
			{
				MCA.getInstance().getLogger().log("Error setting field value.");
				MCA.getInstance().getLogger().log(e);
				continue;
			}
		}

		//Sync with all other players if server side.
		if (!player.worldObj.isRemote)
		{
			MCA.packetHandler.sendPacketToAllPlayersExcept(new PacketSetFieldValue(entityId, fieldName, fieldValue), (EntityPlayerMP) player);
		}
		
		return null;
	}
}