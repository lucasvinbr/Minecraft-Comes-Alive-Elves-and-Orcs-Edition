package mca.client.render;

import mca.entity.passive.EntityBatMCA;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderBatFactory implements IRenderFactory<EntityBatMCA> {
	public static final RenderBatFactory INSTANCE = new RenderBatFactory();

	@Override
	public Render<? super EntityBatMCA> createRenderFor(RenderManager manager) {
		return new RenderBatMCA(manager);
	}
}
