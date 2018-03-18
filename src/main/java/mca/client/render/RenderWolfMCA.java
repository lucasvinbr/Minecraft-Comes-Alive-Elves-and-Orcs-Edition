/**
 * 
 */
package mca.client.render;

import mca.client.model.ModelWolfMCA;
import mca.entity.EntityWolfMCA;
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
	public static final ResourceLocation WOLFMCA_TEXTURE = new ResourceLocation("mca:textures/wolf.png");
	public static final ResourceLocation TAMED_WOLFMCA_TEXTURE = new ResourceLocation("mca:textures/wolf_tame.png");
	public static final ResourceLocation ANRGY_WOLFMCA_TEXTURE = new ResourceLocation("mca:textures/wolf_angry.png");
	public static final ResourceLocation DOG_TEXTURE = new ResourceLocation("mca:textures/doggy.png");

	public RenderWolfMCA(RenderManager manager) {
		super(manager, new ModelWolfMCA(), 0.5F);
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
	protected ResourceLocation getEntityTexture(EntityWolfMCA wolf) {
		if (wolf.attributes.getTexture() != null) {
			return wolf.isAngry() ? new ResourceLocation(wolf.attributes.getAngryTexture())
					: new ResourceLocation(wolf.attributes.getTexture());
		}
		else {
			return wolf.isAngry() ? ANRGY_WOLFMCA_TEXTURE : wolf.isTamed() ? TAMED_WOLFMCA_TEXTURE : WOLFMCA_TEXTURE;
		}
	}

}
