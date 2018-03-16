/**
 * 
 */
package mca.client.render;

import mca.entity.EntityWitchMCA;
import net.minecraft.client.model.ModelWitch;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Michael M. Adkins
 *
 */
@SideOnly(Side.CLIENT)
public class RenderWitchMCA extends RenderLiving<EntityWitchMCA> {
	private static final ResourceLocation WitchMCA_TEXTURES = new ResourceLocation("mca:textures/WitchMCA.png");
	private static final ResourceLocation ANRGY_WitchMCA_Textures = new ResourceLocation(
			"mca:textures/WitchMCA_angry.png");

	public RenderWitchMCA(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelWitch(0.0F), 0.5F);
		// this.addLayer(new LayerHeldItemWitch(this));
    }

	@Override
	public ModelWitch getMainModel() {
		return (ModelWitch) super.getMainModel();
	}

	/**
	 * Renders the desired {@code T} type Entity.
	 */
	@Override
	public void doRender(EntityWitchMCA entity, double x, double y, double z, float entityYaw, float partialTicks) {
		((ModelWitch) this.mainModel).holdingItem = !entity.getHeldItemMainhand().isEmpty();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless
	 * you call Render.bindEntityTexture.
	 */
	@Override
	protected ResourceLocation getEntityTexture(EntityWitchMCA entity) {
		return entity.isDrinkingPotion() ? ANRGY_WitchMCA_Textures : WitchMCA_TEXTURES;
	}

	@Override
	public void transformHeldFull3DItemLayer() {
		GlStateManager.translate(0.0F, 0.1875F, 0.0F);
	}

	/**
	 * Allows the render to do state modifications necessary before the model is
	 * rendered.
	 */
	@Override
	protected void preRenderCallback(EntityWitchMCA entitylivingbaseIn, float partialTickTime) {
		float f = 0.9375F;
		GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
	}
}
