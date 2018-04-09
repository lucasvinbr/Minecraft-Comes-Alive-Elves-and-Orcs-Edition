package mca.entity.monster;

import mca.core.MCA;
import mca.entity.TitanAttributes;
import mca.entity.TitanCore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import radixcore.constant.Time;

public class EntityTitan extends EntityMob {
	private static final DataParameter<Byte> AI_FLAGS = EntityDataManager.<Byte>createKey(EntityLiving.class,
			DataSerializers.BYTE);
	protected static final int blockList1[] = { 3, 5, 6, 12, 13, 18, 20, 26, 30, 35, 47, 53, 54, 58, 63, 64, 80, 81, 83,
			85, 86, 88, 91, 96, 99, 100, 102, 103, 107, 111, 125, 126, 134, 135, 136, 146 };
	private static final float moveSpeed = 0.65F;
	public TitanAttributes attributes;
	int scale = 0;
	protected int deathCountDown = 3 * Time.SECOND;

	public EntityTitan(World par1World) {
		super(par1World);
		attributes = new TitanAttributes(this);
		setSize(1.0F, 1.5F);
		experienceValue = 20;
		stepHeight = 2.0F;

		ignoreFrustumCheck = true;
		tasks.addTask(0, new EntityAIAttackMelee(this, 0.64999997615814209D, false));
		tasks.addTask(1, new EntityAIAttackMelee(this, 0.64999997615814209D, true));
		tasks.addTask(3, new EntityAIMoveTowardsTarget(this, 0.64999997615814209D, 64F));
		tasks.addTask(4, new EntityAIMoveThroughVillage(this, 0.64999997615814209D, false));
		tasks.addTask(5, new EntityAIWander(this, 0.64999997615814209D));
		tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 16F));
		tasks.addTask(6, new EntityAILookIdle(this));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
		targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityVillager.class, false));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100D);
		getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64D);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.64999997615814209D);
		getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(getAttackStrength(this));
	}

	@Override
	public boolean attackEntityAsMob(Entity par1Entity) {
		boolean flag = super.attackEntityAsMob(par1Entity);
		// if (flag && rand.nextFloat() < (float) world.field_73013_u.getDifficultyId()
		// * 0.3F)
		// par1Entity.motionY += 0.40000000000000002D + scale * 0.01D;
		return flag;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (isEntityInvulnerable(source))
			return false;
		if (source.getTrueSource() instanceof EntityPlayer)
			setAttackTarget((EntityLivingBase) source.getTrueSource());
		return super.attackEntityFrom(source, amount);
	}

	public void createCore(World par1World) {
		TitanCore core = new TitanCore(this, par1World);
		// core.setLocationAndAngles(posX, posY + scale * 0.75F, posZ, 0.0F, 0.0F);
		if (!par1World.isRemote) {
			par1World.spawnEntity(core);
			// setCore(core);
			MCA.getLog().debug((new StringBuilder()).append("spawn core").append(getEntityId()).toString());
		}
	}

	// protected void destroyBlock() {
	// int x0 = (int) (posX - getTitanSize() / 4F);
	// int y0 = (int) posY;
	// int z0 = (int) (posZ - getTitanSize() / 4F);
	// int x1 = (int) (posX + getTitanSize() / 4F);
	// int y1 = (int) (posY + getTitanSize());
	// int z1 = (int) (posZ + getTitanSize() / 4F);
	// for (int i = x0; i < x1; i++) {
	// for (int j = y0; j < y1; j++) {
	// for (int k = z0; k < z1; k++) {
	// Block block = RadixBlocks.getBlock(world, i, j, k);
	// // if (block.func_149712_f(world, i, j, k) >= 20F)
	// if (block == Blocks.AIR)
	// continue;
	// if (!world.isRemote) {
	// // world.func_147468_f(i, j, k);
	// playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
	// continue;
	// }
	// for (int n = 0; n < 5; n++) {
	// double xx = -2D + rand.nextFloat() * 4D;
	// double yy = -2D + rand.nextFloat() * 4D;
	// double zz = -2D + rand.nextFloat() * 4D;
	// world.playSound(null, new BlockPos(posX + xx, posY + yy, posZ + zz),
	// SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.AMBIENT, 3.0F, 1.0F);
	// }
	//
	// }
	//
	// }
	//
	// }
	//
	// }

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(AI_FLAGS, Byte.valueOf((byte) 1));
		dataManager.register(AI_FLAGS, Byte.valueOf((byte) 0));
		// dataManager.register(AI_FLAGS, Integer.valueOf(0));
	}

	protected Item func_146068_u() {
		return Items.ROTTEN_FLESH;
	}

	protected String func_70621_aR() {
		return "mob.zombie.hurt";
	}

	protected void func_70628_a(boolean par1, int par2) {
		// dropItem(Items.ROTTEN_FLESH, rand.nextInt(2) * scale);
		// dropItem(Items.BONE, (rand.nextInt(2) + 1) * scale);
	}

	protected String func_70639_aQ() {
		return "mob.zombie.say";
	}

	protected boolean func_70650_aV() {
		return true;
	}

	public EnumCreatureAttribute func_70668_bt() {
		return EnumCreatureAttribute.UNDEAD;
	}

	protected String func_70673_aS() {
		return "mob.zombie.death";
	}

	public float getAttackStrength(Entity par1Entity) {
		return 10 + scale;
	}

	public TitanAttributes getCore() {
		// Entity entity = world.getEntityByID(dataManager.func_75679_c(19));
		TitanAttributes core = null;
		if (core instanceof TitanAttributes)
			return core;
		else return null;
	}

	// public String getSkin() {
	// return dataManager.get(TEXTURE);
	// }
	//
	// public int getTitanSize() {
	// return scale;
	// }
	//
	// @Override
	// protected void onDeathUpdate() {
	// super.onDeathUpdate();
	// if (this.dead) {
	// if (deathCountDown >= 0) {
	// deathCountDown--;
	// Utilities.spawnParticlesAroundPointS(EnumParticleTypes.CLOUD, world, posX,
	// posY, posZ, 10);
	// }
	// else {
	// try {
	// super.onDeath(damageSource);
	// }
	// catch (Exception e) {
	// String msg = String.format("Exception occurred!%nMessage: %s%n",
	// e.getLocalizedMessage());
	// FMLLog.severe(msg, e);
	// //
	// java.util.logging.LogManager.getLogManager().getLogger(this.getClass().getName()).severe(msg);
	// org.apache.logging.log4j.LogManager.getLogger(this.getClass().getName()).error(msg,
	// e);
	// // java.util.logging.Logger.getLogger(this.getClass().getName()).severe(msg);
	// }
	// }
	// }
	// }


	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		// if (world.rand.nextInt(4) == 0 && MCA.getConfig().isBreakBlock())
		// destroyBlock();
		if (getHealth() > 0.0F) {
			if (getHealth() < getMaxHealth())
				heal(2.0F);
			if (getCore() != null) {
				if (getAttackTarget() == null && world.rand.nextInt(6) == 0)
					setAttackTarget(world.getClosestPlayerToEntity(this, 64D));
				// getCore().posX = posX;
				// getCore().posY = posY + getTitanSize() * 0.75F + 0.125D;
				// getCore().posZ = posZ;
				// getCore().setPosition(getCore().posX, getCore().posY, getCore().posZ);
			}
			if (!world.isRemote)
				;
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}

	protected void playStepSound(int par1, int par2, int par3, int par4) {
		playSound(SoundEvents.ENTITY_ZOMBIE_STEP, 0.15F, 1.0F);
	}

}
