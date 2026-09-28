# FluidLogistics 1.3.0 Compatibility Fix

## Problem

The original mod was throwing `NoSuchMethodError` when interacting with the Portable Stock Ticker interface:

```
java.lang.NoSuchMethodError: 'boolean com.yision.fluidlogistics.item.CompressedTankItem.isVirtual(net.minecraft.world.item.ItemStack)'
```

### Root Cause

- **Mod in conflict**: Create: Mobile Packages (v0.7.7) + FluidLogistics (v1.3.0)
- **When it failed**: Accessing PortableStockTickerScreen interface
- **Why it failed**: The bridge code (`CFLBridge.java`) was calling methods that don't exist in FluidLogistics 1.3.0

## Solution

### API Changes in FluidLogistics 1.3.0

The following methods were removed/renamed in FluidLogistics 1.3.0:

| Old API (< 1.3.0) | New API (1.3.0+) |
|---|---|
| `CompressedTankItem.isVirtual(stack)` | `CompressedTankItem.isFluidStack(stack)` |
| `CompressedTankItem.setFluidVirtual(stack, fluid)` | `CompressedTankItem.setFluid(stack, fluid)` |

### Changes Made

**File**: `src/main/java/de/theidler/create_mobile_packages/compat/fluidlogistics/CFLBridge.java`

```diff
  public static boolean isVirtualFluid(ItemStack stack) {
-     return stack.getItem() instanceof CompressedTankItem && CompressedTankItem.isVirtual(stack);
+     return stack.getItem() instanceof CompressedTankItem && CompressedTankItem.isFluidStack(stack);
  }

  public static boolean isVirtualFluid(GenericStack stack) {
      ItemStack itemStack = keyAsItemStack(stack);
      if (itemStack.getItem() instanceof CompressedTankItem) {
-         return CompressedTankItem.isVirtual(itemStack);
+         return CompressedTankItem.isFluidStack(itemStack);
      }
      return false;
  }

  public static GenericStack toVirtualFluidStack(FluidStack fluid, int amountMb) {
      ItemStack virtualTank = new ItemStack(AllItems.COMPRESSED_STORAGE_TANK.get());
-     CompressedTankItem.setFluidVirtual(virtualTank, fluid.copyWithAmount(1));
+     CompressedTankItem.setFluid(virtualTank, fluid.copyWithAmount(1));
      return GenericStack.wrap(virtualTank).withAmount(amountMb);
  }
```

## Testing

Build and verify:

```bash
./gradlew clean build
```

The JAR should compile without errors and work correctly with FluidLogistics 1.3.0.

## CI/CD

A new GitHub Actions workflow (`.github/workflows/build.yml`) has been added to:
- Automatically build the mod on every push and pull request
- Upload build artifacts for easy access
- Validate compilation before merging

## Status

✅ Ready for pull request to original repository
