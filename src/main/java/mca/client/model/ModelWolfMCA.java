/**
 * 
 */
package mca.client.model;

import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * @author Michael M. Adkins
 *
 */
public class ModelWolfMCA extends ModelQuadruped {
	ModelRenderer snout;
	/** Wolf'se first leg */
	public ModelRenderer wolfLeg1;
	/** Wolf's second leg */
	public ModelRenderer wolfLeg2;
	/** Wolf's third leg */
	public ModelRenderer wolfLeg3;
	/** Wolf's fourth leg */
	public ModelRenderer wolfLeg4;
	/** The wolf's tail */
	ModelRenderer wolfTail;
	/** The wolf's mane */
	ModelRenderer wolfMane;
	public ModelWolfMCA() {
		super(10, 0);
		head = new ModelRenderer(this, 0, 0);
		head.addBox(-4F, -2F, -6F, 8, 8, 6, 0.0F);
		head.setRotationPoint(0.0F, 4F, -8F);
		snout = new ModelRenderer(this, 8, 15);
		snout.addBox(-2F, 3F, -12F, 4, 4, 6, 0.0F);
		snout.setRotationPoint(0.0F, 3F, -7F);
		body = new ModelRenderer(this, 28, 6);
		body.addBox(-5F, -8F, -9F, 10, 16, 6, 0.0F);
		body.setRotationPoint(0.0F, 5F, 2.0F);
		this.wolfMane = new ModelRenderer(this, 21, 0);
		this.wolfMane.addBox(-3.0F, -3.0F, -3.0F, 8, 6, 7, 0.0F);
		this.wolfMane.setRotationPoint(-1.0F, 14.0F, 2.0F);
		this.wolfLeg1 = new ModelRenderer(this, 0, 18);
		this.wolfLeg1.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.wolfLeg1.setRotationPoint(-2.5F, 16.0F, 7.0F);
		this.wolfLeg2 = new ModelRenderer(this, 0, 18);
		this.wolfLeg2.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.wolfLeg2.setRotationPoint(0.5F, 16.0F, 7.0F);
		this.wolfLeg3 = new ModelRenderer(this, 0, 18);
		this.wolfLeg3.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.wolfLeg3.setRotationPoint(-2.5F, 16.0F, -4.0F);
		this.wolfLeg4 = new ModelRenderer(this, 0, 18);
		this.wolfLeg4.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.wolfLeg4.setRotationPoint(0.5F, 16.0F, -4.0F);
		this.wolfTail = new ModelRenderer(this, 9, 18);
		this.wolfTail.addBox(0.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
		this.wolfTail.setRotationPoint(-1.0F, 12.0F, 8.0F);
		super.head.setTextureOffset(16, 14).addBox(-2.0F, -5.0F, 0.0F, 2, 2, 1, 0.0F);
		super.head.setTextureOffset(16, 14).addBox(2.0F, -5.0F, 0.0F, 2, 2, 1, 0.0F);
		super.head.setTextureOffset(0, 10).addBox(-0.5F, 0.0F, -5.0F, 3, 3, 4, 0.0F);
	}

	/**
	 * @param height
	 * @param scale
	 */
	public ModelWolfMCA(int height, float scale) {
		super(height, scale);
		head = new ModelRenderer(this, 0, 0);
		head.addBox(-4F, -2F, -6F, 8, 8, 6, 0.0F);
		head.setRotationPoint(0.0F, 4F, -8F);
		snout = new ModelRenderer(this, 8, 15);
		snout.addBox(-2F, 3F, -12F, 4, 4, 6, 0.0F);
		snout.setRotationPoint(0.0F, 3F, -7F);
		body = new ModelRenderer(this, 28, 6);
		body.addBox(-5F, -8F, -9F, 10, 16, 6, 0.0F);
		body.setRotationPoint(0.0F, 5F, 2.0F);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
	/**
	 * 
	 * @see net.minecraft.client.model.ModelQuadruped#render(net.minecraft.entity.Entity,
	 *      float, float, float, float, float, float)
	 */
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
	}

	/**
	 * @param entity
	 * @param f
	 * @param f1
	 * @param f2
	 * @param f3
	 * @param f4
	 * @param f5
	 */
	public void setRotationAngles(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.setRotationAngles(f5, f, f1, f2, f3, f4, entity);
		snout.rotateAngleY = head.rotateAngleY;
		snout.rotateAngleX = head.rotateAngleX;
	}
}
