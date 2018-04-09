package mca.client.render;

import mca.entity.passive.EntityParrotMCA;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderParrotFactory implements IRenderFactory<EntityParrotMCA> {
	public static final RenderParrotFactory INSTANCE = new RenderParrotFactory();

	@Override
	public Render<? super EntityParrotMCA> createRenderFor(RenderManager manager) {
		return new RenderParrotMCA(manager);
	}
}
