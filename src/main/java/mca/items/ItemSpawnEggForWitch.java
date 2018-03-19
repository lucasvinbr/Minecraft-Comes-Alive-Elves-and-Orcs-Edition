package mca.items;

import mca.core.MCA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import radixcore.math.Point3D;

public class ItemSpawnEggForWitch extends Item {
	protected boolean isMale;

	public ItemSpawnEggForWitch() {
		this.setMaxStackSize(64);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		int posX = pos.getX();
		int posY = pos.getY() + 1;
		int posZ = pos.getZ();

		if (!world.isRemote) {
			double verticalOffset = 0.0D;

			spawnCreature(world, posX + 0.5D, posY + verticalOffset, posZ + 0.5D);

			if (!player.capabilities.isCreativeMode) {
				player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
			}
		}

		return EnumActionResult.PASS;
	}

	public void spawnCreature(World world, double posX, double posY, double posZ) {
		// EntityWitchMCA witch = null;
		// witch = new EntityWitchMCA(world);
		// if (witch != null) {
		// witch.setAggressive(new Random().nextBoolean());
		// witch.setPosition(posX, posY, posZ);
		// world.spawnEntity(witch);
		// Utilities.spawnParticlesAroundPointS(EnumParticleTypes.SPELL_WITCH, world,
		// witch.getPosition().getX(),
		// witch.getPosition().getY(), witch.getPosition().getZ(), 2);
		// }
		// // MCA.naturallySpawnWitches(new Point3D(posX, posY, posZ), world);
		// for (int i = 0; i < 2; i++) {
		// if (RadixLogic.getBooleanWithProbability(50)) {
		// MCA.unnaturallySpawnWitches(
		// new Point3D(posX, posY, posZ), world);
		// }
		// }
		MCA.unnaturallySpawnWitches(new Point3D(posX, posY, posZ), world);
	}
}
