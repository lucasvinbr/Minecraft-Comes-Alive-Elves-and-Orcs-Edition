package mca.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import mca.blocks.BlockTombstone;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.BlocksMCA;
import mca.data.TransitiveVillagerData;
import mca.enums.EnumRelation;
import mca.tile.TileTombstone;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import radixcore.constant.Font.Color;
import radixcore.constant.Font.Format;

public class ItemTombstone extends Item {
	public ItemTombstone() {
		super();
		maxStackSize = 1;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);

		if (side != EnumFacing.UP) {
			return EnumActionResult.PASS;
		}

		else {
			pos = pos.offset(side);

			if (!BlocksMCA.tombstone.canPlaceBlockAt(world, pos)) {
				return EnumActionResult.FAIL;
			}

			else {
				int i = MathHelper.floor((double) ((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
				world.setBlockState(pos,
						BlocksMCA.tombstone.getDefaultState().withProperty(BlockTombstone.ROTATION, i),
						3);
			}

			stack.shrink(-1);
			final TileTombstone tombstone = (TileTombstone) world.getTileEntity(pos);

			if (tombstone != null) {
				tombstone.setPlayer(player);
				player.openGui(MCA.getInstance(), Constants.GUI_ID_TOMBSTONE, world, pos.getX(), pos.getY(),
						pos.getZ());
			}

			return EnumActionResult.SUCCESS;
		}
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);

		if (stack.hasTagCompound()) {
			TransitiveVillagerData data = new TransitiveVillagerData(stack.getTagCompound());
			String ownerName = null;
			if(stack.getTagCompound() != null) {
				ownerName = stack.getTagCompound().getString("ownerName");
			}
			String name = data.getName();
			String relationId = EnumRelation.getById(stack.getTagCompound().getInteger("ownerRelation")).getPhraseId();

			tooltip.add(Color.WHITE + "Belonged to: ");

			if (!relationId.equals("relation.none")) {
				tooltip.add(Color.GREEN + name + ", " + MCA.getLocalizer().getString(relationId) + " of " + ownerName);
			}

			else {
				tooltip.add(Color.GREEN + name + " the "
						+ MCA.getLocalizer().getString(data.getProfession().getLocalizationId()));
				tooltip.add("Captured by: " + ownerName);
			}
		}

		else {
			tooltip.add(Color.GREEN + "CREATIVE " + Format.RESET + "- No villager attached.");
			tooltip.add("Right-click a villager to attach them");
			tooltip.add("to this object.");
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			tooltip.add("");
			tooltip.add("An item once owned by a");
			tooltip.add("villager who has died. Revive ");
			tooltip.add("them using the " + Color.YELLOW + "Staff of Life" + Color.GRAY + ".");
		}

		else {
			tooltip.add("");
			tooltip.add("Hold " + Color.YELLOW + "SHIFT" + Color.GRAY + " for info.");
		}
	}
}
