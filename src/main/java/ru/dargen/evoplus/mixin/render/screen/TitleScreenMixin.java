package ru.dargen.evoplus.mixin.render.screen;

import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Unique
    private static boolean tried = false;

    @Inject(method = "init", at = @At("HEAD"))
    public void init(CallbackInfo ci) {
        if (!tried) {
            tried = true;
        }
    }

}