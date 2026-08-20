package com.nxfx21.infabric.mixin;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.effects.InfuseEffect;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin {
    @Shadow @Final private ScreenHandlerContext context;

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private static void onUpdateResult(ScreenHandler handler, ServerWorld world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, RecipeEntry<CraftingRecipe> recipe, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        List<ItemStack> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            grid.add(craftingInventory.getStack(i));
        }

        InfuseEffect matchedEffect = Infuse.getInstance().getRecipeManager().matchRecipe(grid);
        if (matchedEffect != null) {
            ScreenHandlerContext ctx = ((CraftingScreenHandlerMixin) (Object) handler).context;
            boolean isBrewingStand = ctx.get((w, pos) -> w.getBlockState(pos).isOf(Blocks.BREWING_STAND)).orElse(false);

            if (!isBrewingStand) {
                resultInventory.setStack(0, ItemStack.EMPTY);
                serverPlayer.sendMessage(new Message(Message.MessageType.EFFECT_NO_BREWING).toComponent(), true);
                ci.cancel();
                return;
            }

            ItemStack result = Infuse.getInstance().getRecipeManager().getItemToCraft(matchedEffect);
            if (result != null) {
                resultInventory.setStack(0, result);
            } else {
                resultInventory.setStack(0, ItemStack.EMPTY);
            }
            ci.cancel();
        }
    }
}
