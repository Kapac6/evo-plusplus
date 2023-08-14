package ru.dargen.evoplus.feature.type.boss

import ru.dargen.evoplus.util.minecraft.uncolored


enum class BossType(val entityName: String, val level: Int) {

    KRIEGER("Кригер", 15),
    SLIME("Слизень", 20),
    STEEL_GUARD("Стальной Страж", 25),
    NIGHTMARE("Кошмар", 30),
    TWINS("Близнецы", 35),
    FIRE_LORD("Повелитель Огня", 40),
    SPIDER("Паучиха", 45),
    DROWNED("Утопленник", 50),
    MAGICIAN("Колдун", 55),
    DIE("Смерть", 60),
    RIDER("Наездник", 65),
    OUTLAW("Разбойник", 70),
    MAGMA_SLIME("Лавовый куб", 75),
    WARDEN("Варден", 80),
    GHOST_HUNTER("Призрачный охотник", 90),
    BLACK_DRAGON("Чёрный дракон", 95),
    GIANT("Гигант", 100),
    CURSED_LEGION("Проклятый легион", 105),
    MONSTER("Монстр", 110),
    NECROMANCER("Некромант", 115),
    DARKNESS_DEVOURER("Пожиратель тьмы", 120),
    MONSTER_2("Чудовище", 125),
    OCTOPUS("Октопус", 130),
    SMITH("Кузнец", 140),
    SHULKER("Могущественный шалкер", 150),
    CASTER("Заклинатель", 160),
    DIE_RIDER("Всадник", 170),
    KOBOLD("Кобольд", 180),
    SAMURAI("Самурай", 190),
    DIE_LORD("Повелитель мёртвых", 200),
    SHADOW_LORD("Теневой лорд", 210),
    BIG_TURTLE("Гигантская черепаха", 220),
    GOLIATH("Голиаф", 230),
    DESTROYER("Разрушитель", 240),
    SNOW_MONSTER("Снежный монстр", 250),
    SCREAM("Крик", 260),
    SPECTRAL_CUBE("Спектральный куб", 270),
    SHADOW("Тень", 280),
    CYNTHIA("Синтия", 290),
    MAGNUS("Магнус", 310),
    HELL_HERALD("Вестник ада", 330),
    HELL_HOUND("Цербер", 350),
    IFRIT("Ифрит", 360),
    BAPHOMET("Бафомет", 370),
    PIGLIN("Пиглин", 380),
    QUEEN_PIGLIN("Королева пиглинов", 390),
    HOGLIN("Хоглин", 400),
    ZOMBIE_PIGLIN("Зомби пиглин", 410),
    BRUTAL_PIGLIN("Брутальный пиглин", 420),
    MAGMA("Магма", 430),
    ZOGLIN("Зоглин", 440),
    HELL_KNIGHT("Адский рыцарь", 440);

    val displayName = "§6$entityName §8[§6$level§8]"

    companion object {

        private val Name2Type = entries.associateBy(BossType::entityName).mapKeys { (name, _) -> name.lowercase() }
        private val MedalPattern = "\\s([\uE124\uE125\uE126])(\\sx\\d+|)".toRegex()

        operator fun get(displayName: String) =
            Name2Type[displayName.lowercase().uncolored().replace(MedalPattern, "")]

    }
}

