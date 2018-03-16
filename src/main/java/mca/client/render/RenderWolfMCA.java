/**
 * 
 */
package mca.client.render;

import mca.entity.EntityWolfMCA;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Michael M. Adkins
 *
 */
@SideOnly(Side.CLIENT)
public class RenderWolfMCA<T extends EntityWolfMCA> extends RenderLiving<EntityWolfMCA> {
	private static final ResourceLocation WOLFMCA_TEXTURES = new ResourceLocation("mca:textures/husky_untamed.png");
	private static final ResourceLocation TAMED_WOLFMCA_TEXTURES = new ResourceLocation(
			"mca:textures/husky_tamed.png");
	private static final ResourceLocation ANRGY_WOLFMCA_TEXTURES = new ResourceLocation(
			"mca:textures/husky_angry.png");

	public RenderWolfMCA(RenderManager manager) {
		super(manager, new ModelWolf(), 0.5F);

	}

	/**
	 * Defines what float the third param in setRotationAngles of ModelBase is
	 */
	protected float handleRotationFloat(EntityWolf livingBase, float partialTicks) {
		return livingBase.getTailRotation();
	}

	/**
	 * Renders the desired {@code T} type Entity.
	 */
	@Override
	public void doRender(EntityWolfMCA entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if (entity.isWolfWet()) {
			float f = entity.getBrightness() * entity.getShadingWhileWet(partialTicks);
			GlStateManager.color(f, f, f);
		}

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless
	 * you call Render.bindEntityTexture.
	 */
	@Override
	protected ResourceLocation getEntityTexture(EntityWolfMCA entity) {
		if (entity.isTamed()) {
			return TAMED_WOLFMCA_TEXTURES;
		}
		else {
			return entity.isAngry() ? ANRGY_WOLFMCA_TEXTURES : WOLFMCA_TEXTURES;
		}
	}

}
