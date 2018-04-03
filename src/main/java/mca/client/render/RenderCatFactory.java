package mca.client.render;

import mca.entity.passive.EntityCatMCA;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderCatFactory implements IRenderFactory<EntityCatMCA> {
	public static final RenderCatFactory INSTANCE = new RenderCatFactory();

	@Override
	public Render<? super EntityCatMCA> createRenderFor(RenderManager manager) {
		return new RenderCatMCA(manager);
	}
}
