package ru.dargen.evoplus.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Queue;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {

    @Invoker("doAttack")
    boolean leftClick();

    @Accessor("renderTaskQueue")
    Queue<Runnable> getRenderTaskQueue();
}
