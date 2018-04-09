// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Entity60mTitan.java

package mca.entity.monster;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

// Referenced classes of package mods.TheTitan:
//            EntityTitan

public class Entity60mTitan extends EntityTitan
{

    public Entity60mTitan(World par1World)
    {
        super(par1World);
        scale = 60;
        float moveSpeed = 0.4F;
        experienceValue = 50;
        stepHeight = 5F;
		// setTitanSize(60);
		// ignoreFrustumCheck = true;
		// tasks.addTask(1, new EntityAIAttackOnCollide(this,
		// net.minecraft.entity.player.EntityPlayer, moveSpeed, false));
		// tasks.addTask(2, new EntityAIAttackOnCollide(this,
		// net.minecraft.entity.passive.EntityVillager, moveSpeed, true));
		// tasks.addTask(2, new EntityAIMoveTowardsTarget(this, moveSpeed, 120F));
		// tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, moveSpeed));
		// tasks.addTask(6, new EntityAIWander(this, moveSpeed));
		// tasks.addTask(7, new EntityAIWatchClosest(this,
		// net.minecraft.entity.player.EntityPlayer, 8F));
		// tasks.addTask(7, new EntityAILookIdle(this));
		// targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		// targetTasks.addTask(3, new EntityAINearestAttackableTarget(this,
		// net.minecraft.entity.player.EntityPlayer, 0, true));
    }

    @Override
	protected boolean func_70650_aV()
    {
        return true;
    }

    @Override
	public void onLivingUpdate()
    {
        super.onLivingUpdate();
    }

    @Override
	public void onUpdate()
    {
        super.onUpdate();
    }

    @Override
	public boolean attackEntityAsMob(Entity par1Entity)
    {
        return super.attackEntityAsMob(par1Entity);
    }

    @Override
	protected Item func_146068_u()
    {
        return null;
    }

    @Override
	protected void func_70628_a(boolean flag, int i)
    {
    }

    @Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
    }

    @Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
    }

	public void destroyBlock()
    {
		// int x0 = MathHelper.func_76128_c(posX - getTitanSize() / 4D);
		// int y0 = MathHelper.func_76128_c(posY);
		// int z0 = MathHelper.func_76128_c(posZ - getTitanSize() / 4D);
		// int x1 = MathHelper.func_76128_c(posX + getTitanSize() / 4D);
		// int y1 = MathHelper.func_76128_c(posY + getTitanSize());
		// int z1 = MathHelper.func_76128_c(posZ + getTitanSize() / 4D);
		// for(int i = x0; i <= x1; i++)
		// {
		// for(int j = y0; j <= y1; j++)
		// {
		// for(int k = z0; k <= z1; k++)
		// {
		// Block block = world.func_147439_a(i, j, k);
		// if(block.func_149712_f(world, i, j, k) >= 50F)
		// continue;
		// if(!world.isRemote)
		// {
		// world.func_147468_f(i, j, k);
		// func_85030_a("random.explode", 1.0F, 1.0F);
		// continue;
		// }
		// for(int n = 0; n < 5; n++)
		// {
		// double xx = -2D + rand.nextFloat() * 4D;
		// double yy = -2D + rand.nextFloat() * 4D;
		// double zz = -2D + rand.nextFloat() * 4D;
		// world.func_72869_a("explode", posX + xx, posY + yy, posZ + zz, 0.0D, 0.0D,
		// 0.0D);
		// }
		//
		// }
		//
		// }
		//
		// }

    }

    protected static final int blockList2[] = {
        1, 2, 3, 4, 5, 6, 12, 13, 14, 15, 
        16, 17, 18, 20, 21, 22, 24, 26, 30, 31, 
        32, 35, 41, 42, 43, 44, 45, 46, 47, 48, 
        53, 54, 56, 57, 58, 63, 64, 67, 73, 74, 
        79, 80, 81, 82, 83, 85, 86, 87, 88, 89, 
        91, 92, 96, 97, 98, 99, 100, 102, 103, 107, 
        108, 109, 110, 111, 112, 113, 114, 123, 124, 125, 
        126, 128, 129, 133, 134, 135, 136, 139, 140, 144, 
        146
    };

}
