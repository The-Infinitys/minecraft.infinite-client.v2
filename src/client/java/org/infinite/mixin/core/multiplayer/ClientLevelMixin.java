package org.infinite.mixin.core.multiplayer;

import java.util.function.BooleanSupplier;
import net.minecraft.client.multiplayer.ClientLevel;
import org.infinite.InfiniteClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
  @Inject(at = @At("HEAD"), method = "tick")
  @SuppressWarnings("Unused")
  private void onStartTick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
    InfiniteClient.INSTANCE.getWorldTicks().onStartTick();
  }

  @Inject(at = @At("TAIL"), method = "tick")
  @SuppressWarnings("Unused")
  private void onEndTick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
    InfiniteClient.INSTANCE.getWorldTicks().onEndTick();
  }
}
