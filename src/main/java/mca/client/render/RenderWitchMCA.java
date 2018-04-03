/**
 * 
 */
package mca.client.render;

import org.lwjgl.opengl.GL11;

import mca.client.model.ModelWitchMCA;
import mca.client.render.layers.LayerHeldItemWitchMCA;
import mca.entity.monster.EntityWitchMCA;
import mca.enums.EnumGender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.modules.RadixLogic;

/**
 * @author Michael M. Adkins
 *
 */
@SideOnly(Side.CLIENT)
public class RenderWitchMCA<T extends EntityWitchMCA> extends RenderLiving<T> {
	private static final ResourceLocation TEXTURE_A = new ResourceLocation("mca:textures/witch_a.png");
	private static final ResourceLocation TEXTURE_B = new ResourceLocation("mca:textures/witch_b.png");
	private static final ResourceLocation TEXTURE_C = new ResourceLocation("mca:textures/witch_c.png");
	private static final ResourceLocation WIZARD = new ResourceLocation("mca:textures/wizard.png");

	public RenderWitchMCA(RenderManager renderManagerIn) {
		super(renderManagerIn, new ModelWitchMCA(1.0F), 0.5F);
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

		textureManager.bindTexture(TEXTURE_A);
		textureManager.bindTexture(TEXTURE_B);
		textureManager.bindTexture(TEXTURE_C);
		textureManager.bindTexture(WIZARD);
		this.addLayer(new LayerHeldItemWitchMCA(this));
	}

	@Override
	public ModelWitchMCA getMainModel() {
		return (ModelWitchMCA) super.getMainModel();
	}

	/**
	 * Renders the desired {@code T} type Entity.
	 */
	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		((ModelWitchMCA) this.mainModel).holdingItem = !entity.getHeldItemMainhand().isEmpty();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless
	 * you call Render.bindEntityTexture.
	 */
	@Override
	protected ResourceLocation getEntityTexture(EntityWitchMCA witch) {
		if (witch.getTexture() == null) {
			if (witch.attributes.getGender() == EnumGender.FEMALE) {
				if (RadixLogic.getBooleanWithProbability(50)) {
					witch.setTexture(TEXTURE_A);
				}
				else if (RadixLogic.getBooleanWithProbability(50)) {
					witch.setTexture(TEXTURE_B);
				}
				return TEXTURE_C;
			}
			else {
				witch.setTexture(WIZARD);
			}
		}
		return witch.getTexture();
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
	protected void preRenderCallback(T entitylivingbaseIn, float partialTickTime) {
		GL11.glScaled(0.9375F, 0.9375F, 0.9375F);
	}
}
