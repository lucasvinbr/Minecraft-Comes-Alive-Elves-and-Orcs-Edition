package mca.client.render;

import mca.entity.EntityWitchMCA;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderWitchFactory implements IRenderFactory<EntityWitchMCA> {
	public static final RenderWitchFactory INSTANCE = new RenderWitchFactory();

	@Override
	public Render<? super EntityWitchMCA> createRenderFor(RenderManager manager) {
		return new RenderWitchMCA(manager);
	}
}
