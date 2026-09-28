package de.theidler.create_mobile_packages.compat.fluidlogistics;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.yision.fluidlogistics.api.packager.PackageResourceCrafting;
import com.yision.fluidlogistics.api.packager.PackageResourceCraftingData;
import com.yision.fluidlogistics.config.Config;
import com.yision.fluidlogistics.item.CompressedTankItem;
import com.yision.fluidlogistics.registry.AllItems;
import com.yision.fluidlogistics.util.FluidAmountHelper;
import com.yision.fluidlogistics.util.FluidDisplayHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.support.BigGenericStack;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

import java.util.List;

public class CFLBridge {

    public static boolean isLoaded() {
        return de.theidler.create_mobile_packages.compat.Mods.FLUIDLOGISTICS.isLoaded();
    }

    public static boolean isVirtualFluid(ItemStack stack) {
        return stack.getItem() instanceof CompressedTankItem && CompressedTankItem.isFluidStack(stack);
    }

    public static boolean isVirtualFluid(GenericStack stack) {
        ItemStack itemStack = keyAsItemStack(stack);
        if (itemStack.getItem() instanceof CompressedTankItem) {
            return CompressedTankItem.isFluidStack(itemStack);
        }
        return false;
    }

    public static boolean isVirtualFluid(BigGenericStack entry) {
        return isVirtualFluid(entry.get());
    }

    public static GenericStack toVirtualFluidStack(FluidStack fluid, int amountMb) {
        ItemStack virtualTank = new ItemStack(AllItems.COMPRESSED_STORAGE_TANK.get());
        CompressedTankItem.setFluid(virtualTank, fluid.copyWithAmount(1));
        return GenericStack.wrap(virtualTank).withAmount(amountMb);
    }

    public static FluidStack fluidOf(GenericStack stack) {
        if (!isVirtualFluid(stack)) return FluidStack.EMPTY;
        return CompressedTankItem.getFluid(keyAsItemStack(stack));
    }

    public static FluidStack fluidOf(BigGenericStack entry) {
        return fluidOf(entry.get());
    }

    public static boolean containsVirtualFluid(GenericOrder order) {
        for (GenericStack stack : order.stacks()) {
            if (isVirtualFluid(stack)) return true;
        }
        return false;
    }

    public static int adjustFluidRequestAmount(int currentAmount, boolean forward, boolean shift, boolean control,
                                                int minAmount, int maxAmount, int steps) {
        return FluidAmountHelper.adjustFluidRequestAmount(currentAmount, forward, shift, control, minAmount, maxAmount, steps);
    }

    public static int adjustFluidRequestAmount(int currentAmount, boolean forward, boolean shift, boolean control,
                                                int minAmount, int maxAmount) {
        return FluidAmountHelper.adjustFluidRequestAmount(currentAmount, forward, shift, control, minAmount, maxAmount);
    }

    public static String formatFluidAmount(int amount) {
        return FluidAmountHelper.formatStockKeeper(amount);
    }

    public static int getFluidPerPackage() {
        return Config.getFluidPerPackage();
    }

    public static boolean hasCustomRecipeData(CraftableBigItemStack cbis) {
        return PackageResourceCrafting.has(cbis);
    }

    public static int getCustomOutputCount(CraftableBigItemStack cbis) {
        return PackageResourceCrafting.get(cbis).map(PackageResourceCraftingData::outputCount).orElse(0);
    }

    public static int getCustomTransferLimit(CraftableBigItemStack cbis) {
        return PackageResourceCrafting.get(cbis).map(PackageResourceCraftingData::transferLimit).orElse(0);
    }

    public static List<BigItemStack> getCustomRequirements(CraftableBigItemStack cbis) {
        return PackageResourceCrafting.get(cbis).map(PackageResourceCraftingData::requirements).orElse(List.of());
    }

    public static void setCustomRecipeData(CraftableBigItemStack cbis, int outputCount, int transferLimit,
                                            List<BigItemStack> requirements) {
        PackageResourceCrafting.set(cbis, new PackageResourceCraftingData(outputCount, transferLimit, requirements));
    }

    public static ItemStack keyAsItemStack(GenericStack stack) {
        return BigGenericStack.of(stack.withAmount(1)).asStack().stack;
    }

    public static boolean shouldDisplayAsFluidInPackage(ItemStack stack) {
        return FluidDisplayHelper.shouldDisplayAsFluidInPackage(stack)
                && getPackageFluidAmount(stack) > 0;
    }

    public static int getPackageFluidAmount(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null || handler.getTanks() <= 0) {
            return 0;
        }
        FluidStack fluid = handler.getFluidInTank(0);
        return fluid.isEmpty() ? 0 : fluid.getAmount();
    }
}
