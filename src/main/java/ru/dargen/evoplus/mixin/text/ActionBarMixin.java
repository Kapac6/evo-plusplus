package ru.dargen.evoplus.mixin.text;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import ru.dargen.evoplus.event.EventBus;
import ru.dargen.evoplus.event.chat.ActionBarEvent;
import ru.dargen.evoplus.util.minecraft.MinecraftKt;

@Mixin(InGameHud.class)
public class ActionBarMixin {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/gui/hud/InGameHud;setOverlayMessage(Lnet/minecraft/text/Text;Z)V")
    //private void sendMessage(Text message, boolean tinted, CallbackInfo info) {}
    private void OnActionBar(Text message, boolean tinted, CallbackInfo ci) {
        EventBus.INSTANCE.fire(new ActionBarEvent(message));
    }
}