package ru.dargen.evoplus.Sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import ru.dargen.evoplus.EvoPlus;
import ru.dargen.evoplus.EvoPlusKt;

import static net.fabricmc.loader.impl.FabricLoaderImpl.MOD_ID;

public class SoundEventsz {
    private SoundEventsz() {
    }

    public static SoundEvent NINJA1 = registerSound("ninja1");
    public static SoundEvent VOID5 = registerSound("void5");
    public static SoundEvent FENIX1 = registerSound("fenix1");
    public static SoundEvent BOSSRESPAWN = registerSound("bossresp");


    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.of(EvoPlus.INSTANCE.getId(), id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }

    public static void initialize() {
        EvoPlusKt.getLogger().debug("JOPA SHAVALA TRUSI");
    }
}
