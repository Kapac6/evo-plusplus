package ru.dargen.evoplus.features.rune

import ru.dargen.evoplus.util.format.fix

data class RuneProperty(val name: String, val type: Type, var value: Double = .0) {

    val valueColor get() = if (value < 0) "§c" else "§a"
    val formattedValue get() = "$valueColor${type.formatter(this)}".replace(",", ".")

    override fun toString() = "$name: $formattedValue"

    enum class Type(
        val pattern: Regex,
        val appender: RuneProperty.(matcher: MatchResult) -> Unit,
        val formatter: RuneProperty.() -> String
    ) {

        PRESENCE("^\\+$".toRegex(), {}, { "+" }),
        INCREASE(
            "^([-+][.\\d]+)(?: \\(([-+][.\\d]+)\\))?(?: \\| ([-+][.\\d]+))?$".toRegex(),
            { value += it.groupValues[1].toDouble() + (it.groupValues.getOrNull(2)?.toDoubleOrNull() ?: .0) + (it.groupValues.getOrNull(3)?.toDoubleOrNull() ?: .0) },
            { "${if (value >= 0) "+" else ""}${value.fix()}" }),
        PERCENTAGE(
            "^([-+][.\\d]+)%(?: \\(([-+][.\\d]+)%\\))?(?: \\| ([-+][.\\d]+)%)?$".toRegex(),
            { value += it.groupValues[1].toDouble() + (it.groupValues.getOrNull(2)?.toDoubleOrNull() ?: .0) + (it.groupValues.getOrNull(3)?.toDoubleOrNull() ?: .0) },
            { "${if (value >= 0) "+" else ""}${value.fix()}%" }),
        MULTIPLY(
            "^x([.\\d]+)(?: \\| x([.\\d]+))?$".toRegex(),
            { value += it.groupValues[1].toDouble() + (it.groupValues.getOrNull(2)?.toDoubleOrNull() ?: .0) + (it.groupValues.getOrNull(3)?.toDoubleOrNull() ?: .0) - 1 },
            { "x${value.fix()}" }),
        MINER(
            "^1 к (\\d+)$".toRegex(),
            { value += 1.0 / it.groupValues[1].toDouble() },
            { "1 к ${(1.0 / value).toInt()}" });

    }

}