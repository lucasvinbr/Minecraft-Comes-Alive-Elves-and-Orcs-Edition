package mca.client.model;

import org.lwjgl.opengl.GL11;

import mca.entity.monster.EntityWitchMCA;
import mca.enums.EnumGender;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelWitch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class ModelWitchMCA extends ModelWitch {
	private ModelRenderer breasts;
	public ModelRenderer bipedRightArm;
	public ModelRenderer bipedLeftArm;
	public ModelBiped.ArmPose leftArmPose;
	public ModelBiped.ArmPose rightArmPose;
	// public boolean holdingItem;
	private final ModelRenderer mole = (new ModelRenderer(this)).setTextureSize(64, 128);
	private final ModelRenderer witchHat;
	boolean isFemale = true;
	public boolean isSneak;

	public ModelWitchMCA(float scale) {
		super(scale);
		this.textureWidth = 64;
		this.textureHeight = 128;
		this.mole.setRotationPoint(0.0F, -2.0F, 0.0F);
		this.mole.setTextureOffset(0, 0).addBox(0.0F, 3.0F, -6.75F, 1, 1, 1, -0.25F);
		this.villagerNose.addChild(this.mole);
		this.witchHat = (new ModelRenderer(this)).setTextureSize(64, 128);
		this.witchHat.setRotationPoint(-5.0F, -10.03125F, -5.0F);
		this.witchHat.setTextureOffset(0, 64).addBox(0.0F, 0.0F, 0.0F, 10, 2, 10);
		this.villagerHead.addChild(this.witchHat);
		ModelRenderer modelrenderer = (new ModelRenderer(this)).setTextureSize(64, 128);
		modelrenderer.setRotationPoint(1.75F, -4.0F, 2.0F);
		modelrenderer.setTextureOffset(0, 76).addBox(0.0F, 0.0F, 0.0F, 7, 4, 7);
		modelrenderer.rotateAngleX = -0.05235988F;
		modelrenderer.rotateAngleZ = 0.02617994F;
		this.witchHat.addChild(modelrenderer);
		ModelRenderer modelrenderer1 = (new ModelRenderer(this)).setTextureSize(64, 128);
		modelrenderer1.setRotationPoint(1.75F, -4.0F, 2.0F);
		modelrenderer1.setTextureOffset(0, 87).addBox(0.0F, 0.0F, 0.0F, 4, 4, 4);
		modelrenderer1.rotateAngleX = -0.10471976F;
		modelrenderer1.rotateAngleZ = 0.05235988F;
		modelrenderer.addChild(modelrenderer1);
		ModelRenderer modelrenderer2 = (new ModelRenderer(this)).setTextureSize(64, 128);
		modelrenderer2.setRotationPoint(1.75F, -2.0F, 2.0F);
		modelrenderer2.setTextureOffset(0, 95).addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.25F);
		modelrenderer2.rotateAngleX = -0.20943952F;
		modelrenderer2.rotateAngleZ = 0.10471976F;
		modelrenderer1.addChild(modelrenderer2);
		
		this.villagerArms = (new ModelRenderer(this)).setTextureSize(textureWidth, textureHeight);
		this.villagerArms.setTextureOffset(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4, 8, 4, scale);
		this.villagerArms.setTextureOffset(44, 22).addBox(4.0F, -2.0F, -2.0F, 4, 8, 4, scale);
		this.villagerArms.setTextureOffset(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8, 4, 4, scale);

		this.bipedRightArm = new ModelRenderer(this, 0, 26);
		this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, scale);
		// this.bipedRightArm.setRotationPoint(-5.0F, 2.0F + scale, 0.0F);
		this.bipedLeftArm = new ModelRenderer(this, 40, 26);
		this.bipedLeftArm.mirror = true;
		this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, scale);
		// this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + scale, 0.0F);

		breasts = new ModelRenderer(this, 0, 26);
		breasts.addBox(-3F, 0F, -1F, 6, 3, 3);
		breasts.setRotationPoint(0F, 3.5F, -3F);
		breasts.setTextureSize(64, 64);
		breasts.mirror = true;

		setRotation(breasts, 1.07818F, 0F, 0F);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {
		EntityWitchMCA witch = (EntityWitchMCA) entityIn;
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		this.villagerHead.render(scale);
		this.villagerBody.render(scale);
		this.rightVillagerLeg.render(scale);
		this.leftVillagerLeg.render(scale);
		this.villagerArms.render(scale);
		this.bipedRightArm.render(scale);
		this.bipedLeftArm.render(scale);
		if (witch.attributes.getGender() == EnumGender.FEMALE) {
			this.breasts.showModel = true;
			isFemale = true;
			GL11.glPushMatrix();
			{
				// Correct scaling and location.
				GL11.glTranslated(0.0D, 0.0D, 0.005D);
				GL11.glScaled(1.15D, 1.0D, 1.0D);
				breasts.render(scale);
			}
			GL11.glPopMatrix();
		}
		else {
			isFemale = false;
			this.villagerNose.showModel = true;
		}
		villagerArms.showModel = true;
		bipedLeftArm.showModel = false;
		bipedRightArm.showModel = false;
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are used
	 * for animating the movement of arms and legs, where par1 represents the
	 * time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		EntityWitchMCA witch = (EntityWitchMCA) entityIn;
		if (witch.attributes.getGender() == EnumGender.MALE) {
			this.villagerNose.offsetX = 0.0F;
			this.villagerNose.offsetY = 0.0F;
			this.villagerNose.offsetZ = 0.0F;
			float f = 0.01F * (entityIn.getEntityId() % 10);
			this.villagerNose.rotateAngleX = MathHelper.sin(entityIn.ticksExisted * f) * 4.5F * 0.017453292F;
			this.villagerNose.rotateAngleY = 0.0F;
			this.villagerNose.rotateAngleZ = MathHelper.cos(entityIn.ticksExisted * f) * 2.5F * 0.017453292F;
		}

		this.bipedRightArm.rotationPointZ = 0.0F;
		this.bipedRightArm.rotationPointX = -5.0F;
		this.bipedLeftArm.rotationPointZ = 0.0F;
		this.bipedLeftArm.rotationPointX = 5.0F;
		float f = 1.0F;

		this.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount
				* 0.5F / f;
		this.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
		this.bipedRightArm.rotateAngleZ = 0.0F;
		this.bipedLeftArm.rotateAngleZ = 0.0F;

		if (this.isRiding) {
			this.bipedRightArm.rotateAngleX += -((float) Math.PI / 5F);
			this.bipedLeftArm.rotateAngleX += -((float) Math.PI / 5F);
		}

		this.bipedRightArm.rotateAngleY = 0.0F;
		this.bipedRightArm.rotateAngleZ = 0.0F;

		// switch (this.leftArmPose) {
		// case EMPTY:
		// this.bipedLeftArm.rotateAngleY = 0.0F;
		// break;
		// case BLOCK:
		// this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * 0.5F -
		// 0.9424779F;
		// this.bipedLeftArm.rotateAngleY = 0.5235988F;
		// break;
		// case ITEM:
		// this.bipedLeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX * 0.5F -
		// ((float) Math.PI / 10F);
		// this.bipedLeftArm.rotateAngleY = 0.0F;
		// }

		// switch (this.rightArmPose) {
		// case EMPTY:
		// this.bipedRightArm.rotateAngleY = 0.0F;
		// break;
		// case BLOCK:
		// this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.5F -
		// 0.9424779F;
		// this.bipedRightArm.rotateAngleY = -0.5235988F;
		// break;
		// case ITEM:
		// this.bipedRightArm.rotateAngleX = this.bipedRightArm.rotateAngleX * 0.5F -
		// ((float) Math.PI / 10F);
		// this.bipedRightArm.rotateAngleY = 0.0F;
		// }

		if (this.swingProgress > 0.0F) {
			EnumHandSide enumhandside = this.getMainHand(entityIn);
			ModelRenderer modelrenderer = this.getArmForSide(enumhandside);
			float f1 = this.swingProgress;
			this.villagerBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt(f1) * ((float) Math.PI * 2F)) * 0.2F;

			if (enumhandside == EnumHandSide.LEFT) {
				this.villagerBody.rotateAngleY *= -1.0F;
			}

			this.bipedRightArm.rotationPointZ = MathHelper.sin(this.villagerHead.rotateAngleY) * 5.0F;
			this.bipedRightArm.rotationPointX = -MathHelper.cos(this.villagerBody.rotateAngleY) * 5.0F;
			this.bipedLeftArm.rotationPointZ = -MathHelper.sin(this.villagerBody.rotateAngleY) * 5.0F;
			this.bipedLeftArm.rotationPointX = MathHelper.cos(this.villagerBody.rotateAngleY) * 5.0F;
			this.bipedRightArm.rotateAngleY += this.villagerBody.rotateAngleY;
			this.bipedLeftArm.rotateAngleY += this.villagerBody.rotateAngleY;
			this.bipedLeftArm.rotateAngleX += this.villagerBody.rotateAngleX;
			f1 = 1.0F - this.swingProgress;
			f1 = f1 * f1;
			f1 = f1 * f1;
			f1 = 1.0F - f1;
			float f2 = MathHelper.sin(f1 * (float) Math.PI);
			float f3 = MathHelper.sin(this.swingProgress * (float) Math.PI) * -(this.villagerHead.rotateAngleX - 0.7F)
					* 0.75F;
			modelrenderer.rotateAngleX = (float) (modelrenderer.rotateAngleX - (f2 * 1.2D + f3));
			modelrenderer.rotateAngleY += this.villagerBody.rotateAngleY * 2.0F;
			modelrenderer.rotateAngleZ += MathHelper.sin(this.swingProgress * (float) Math.PI) * -0.4F;
		}

		if (this.isSneak) {
			this.villagerBody.rotateAngleX = 0.5F;
			this.bipedRightArm.rotateAngleX += 0.4F;
			this.bipedLeftArm.rotateAngleX += 0.4F;
			this.rightVillagerLeg.rotationPointZ = 4.0F;
			this.leftVillagerLeg.rotationPointZ = 4.0F;
			this.rightVillagerLeg.rotationPointY = 9.0F;
			this.leftVillagerLeg.rotationPointY = 9.0F;
			this.villagerHead.rotationPointY = 1.0F;
		}
		else {
			this.villagerBody.rotateAngleX = 0.0F;
			this.rightVillagerLeg.rotationPointZ = 0.1F;
			this.leftVillagerLeg.rotationPointZ = 0.1F;
			this.rightVillagerLeg.rotationPointY = 12.0F;
			this.leftVillagerLeg.rotationPointY = 12.0F;
			this.villagerHead.rotationPointY = 0.0F;
		}

		this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;

		if (this.rightArmPose == ModelBiped.ArmPose.BOW_AND_ARROW) {
			this.bipedRightArm.rotateAngleY = -0.1F + this.villagerHead.rotateAngleY;
			this.bipedLeftArm.rotateAngleY = 0.1F + this.villagerHead.rotateAngleY + 0.4F;
			this.bipedRightArm.rotateAngleX = -((float) Math.PI / 2F) + this.villagerHead.rotateAngleX;
			this.bipedLeftArm.rotateAngleX = -((float) Math.PI / 2F) + this.villagerHead.rotateAngleX;
		}
		else if (this.leftArmPose == ModelBiped.ArmPose.BOW_AND_ARROW) {
			this.bipedRightArm.rotateAngleY = -0.1F + this.villagerHead.rotateAngleY - 0.4F;
			this.bipedLeftArm.rotateAngleY = 0.1F + this.villagerHead.rotateAngleY;
			this.bipedRightArm.rotateAngleX = -((float) Math.PI / 2F) + this.villagerHead.rotateAngleX;
			this.bipedLeftArm.rotateAngleX = -((float) Math.PI / 2F) + this.villagerHead.rotateAngleX;
		}

		if (this.holdingItem) {
			// this.villagerNose.rotateAngleX = -0.9F;
			// this.villagerNose.offsetZ = -0.09375F;
			// this.villagerNose.offsetY = 0.1875F;
		}
	}

	public void postRenderArm(float scale, EnumHandSide side) {
		this.getArmForSide(side).postRender(scale);
	}

	protected ModelRenderer getArmForSide(EnumHandSide side) {
		return side == EnumHandSide.LEFT ? this.bipedLeftArm : this.bipedRightArm;
	}

	protected EnumHandSide getMainHand(Entity entityIn) {
		if (entityIn instanceof EntityLivingBase) {
			EntityLivingBase entitylivingbase = (EntityLivingBase) entityIn;
			EnumHandSide enumhandside = entitylivingbase.getPrimaryHand();
			return entitylivingbase.swingingHand == EnumHand.MAIN_HAND ? enumhandside : enumhandside.opposite();
		}
		else {
			return EnumHandSide.RIGHT;
		}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setVisible(boolean visible) {
		this.villagerHead.showModel = visible;
		this.witchHat.showModel = visible;
		this.villagerBody.showModel = visible;
		this.bipedRightArm.showModel = visible;
		this.bipedLeftArm.showModel = visible;
		this.leftVillagerLeg.showModel = visible;
		this.rightVillagerLeg.showModel = visible;
		this.breasts.showModel = true;
		// this.villagerNose.showModel = true;
	}
}
