package mca.enums;

public enum EnumEditAction 
{
	RANDOM_NAME(0),
	SWAP_GENDER(1),
	TEXTURE_UP(2),
	TEXTURE_DOWN(3),
	PROFESSION_UP(4),
	PROFESSION_DOWN(5),
	RACE_UP(6),
	RACE_DOWN(7),
	TRAIT_UP(9),
	TRAIT_DOWN(9),
	HEIGHT_UP(10),
	HEIGHT_DOWN(11),
	GIRTH_UP(12),
	GIRTH_DOWN(13),
	TOGGLE_INFECTED(14),
	SET_NAME(15);
	
	int id;
	
	EnumEditAction(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
	
	public static EnumEditAction byId(int id)
	{
		for (EnumEditAction action : values()) 
		{
			if (action.id == id)
			{
				return action;
			}
		}
		
		return null;
	}
}
