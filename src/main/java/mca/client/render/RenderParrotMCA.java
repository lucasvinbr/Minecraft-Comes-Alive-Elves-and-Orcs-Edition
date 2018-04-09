/**
 * 
 */
package mca.client.render;

import mca.client.model.ModelBirdMCA;
import mca.entity.passive.EntityParrotMCA;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * @author Michael M. Adkins
 *
 */
public class RenderParrotMCA extends RenderLiving<EntityParrotMCA> {
	public static final ResourceLocation[] PARROT_TEXTURES = new ResourceLocation[] {
			new ResourceLocation("mca:textures/birdred.png"), new ResourceLocation("mca:textures/birdblue.png"),
			new ResourceLocation("mca:textures/birdgreen.png"), new ResourceLocation("mca:textures/birdyellow.png"),
			new ResourceLocation("mca:textures/birdblack.png") };

	public RenderParrotMCA(RenderManager manager) {
		super(manager, new ModelBirdMCA(), 0.3F);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless
	 * you call Render.bindEntityTexture.
	 */
	@Override
	protected ResourceLocation getEntityTexture(EntityParrotMCA entity) {
		return PARROT_TEXTURES[entity.getVariant()];
	}

	/**
	 * Defines what float the third param in setRotationAngles of ModelBase is
	 */
	@Override
	public float handleRotationFloat(EntityParrotMCA livingBase, float partialTicks) {
		return this.getCustomBob(livingBase, partialTicks);
	}

	private float getCustomBob(EntityParrotMCA parrot, float partialTicks) {
		float f = parrot.oFlap + (parrot.flap - parrot.oFlap) * partialTicks;
		float f1 = parrot.oFlapSpeed + (parrot.flapSpeed - parrot.oFlapSpeed) * partialTicks;
		return (MathHelper.sin(f) + 1.0F) * f1;
	}

	@Override
	public void doRender(EntityParrotMCA parrot, double d, double d1, double d2, float f, float f1) {
		// if (!parrot.typechosen) {
		// parrot.chooseType();
		// }
		super.doRender(parrot, d, d1, d2, f, f1);
	}
}
