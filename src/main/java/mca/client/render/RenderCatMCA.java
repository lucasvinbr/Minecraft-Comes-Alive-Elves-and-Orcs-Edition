package mca.client.render;

import mca.client.model.ModelCatMCA;
import mca.entity.EntityCatMCA;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCatMCA extends RenderLiving<EntityCatMCA> {
	public static final ResourceLocation PATCHY_CAT = new ResourceLocation(EntityCatMCA.PATCHY_CAT_PATH);
	private static final ResourceLocation BLACK_CAT = new ResourceLocation(EntityCatMCA.BLACK_CAT_PATH);
	public static final ResourceLocation CALICO_CAT = new ResourceLocation(EntityCatMCA.CALICO_CAT_PATH);
	private static final ResourceLocation SIAMESE_CAT = new ResourceLocation(EntityCatMCA.SIAMESE_CAT_PATH);
	public static final ResourceLocation SNOW_CAT = new ResourceLocation(EntityCatMCA.SNOW_CAT_PATH);
	public static final ResourceLocation WHITE_CAT = new ResourceLocation(EntityCatMCA.WHITE_CAT_PATH);

	public RenderCatMCA(RenderManager manager) {
		super(manager, new ModelCatMCA(), 0.4F);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless
	 * you call Render.bindEntityTexture.
	 */
	@Override
	protected ResourceLocation getEntityTexture(EntityCatMCA cat) {
		if (cat.attributes.getTexture() == null) {
			switch (cat.getTameSkin()) {
			case 0:
			default:
				return PATCHY_CAT;
			case 1:
				return BLACK_CAT;
			case 2:
				return CALICO_CAT;
			case 3:
				return SIAMESE_CAT;
			case 4:
				return SNOW_CAT;
			case 5:
				return WHITE_CAT;
			}
		}
		else {
			return new ResourceLocation(cat.attributes.getTexture());
		}
	}

	/**
	 * Allows the render to do state modifications necessary before the model is
	 * rendered.
	 */
	@Override
	protected void preRenderCallback(EntityCatMCA entitylivingbaseIn, float partialTickTime) {
		super.preRenderCallback(entitylivingbaseIn, partialTickTime);

		if (entitylivingbaseIn.isTamed()) {
			GlStateManager.scale(0.8F, 0.8F, 0.8F);
		}
	}
}