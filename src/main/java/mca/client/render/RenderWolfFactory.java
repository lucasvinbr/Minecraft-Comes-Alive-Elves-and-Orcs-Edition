package mca.client.render;

import mca.entity.EntityWolfMCA;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderWolfFactory implements IRenderFactory<EntityWolfMCA> {
	public static final RenderWolfFactory INSTANCE = new RenderWolfFactory();

	@Override
	public Render<? super EntityWolfMCA> createRenderFor(RenderManager manager) {
		return new RenderWolfMCA(manager);
	}
}
