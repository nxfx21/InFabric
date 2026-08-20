package com.nxfx21.infabric.mixin;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.InfuseEffect;
import net.minecraft.block.BlockState;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CrafterBlock.class)
public class CrafterBlockMixin {
    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    private void onCraft(BlockState state, ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (world.getBlockEntity(pos) instanceof CrafterBlockEntity crafter) {
            List<ItemStack> grid = new ArrayList<>(9);
            for (int i = 0; i < 9; i++) {
                grid.add(crafter.getStack(i));
            }
            InfuseEffect matched = Infuse.getInstance().getRecipeManager().matchRecipe(grid);
            if (matched != null) {
                // Prevent automatic crafters from crafting Infuse recipes
                ci.cancel();
            }
        }
    }
}
