package ru.dargen.evoplus.util.evo

import ru.dargen.evoplus.util.minecraft.printMessage
import kotlin.math.abs
import kotlin.math.log10

fun convertFrom(msg: String) : Long {
    val type = msg.last()
    val startmoney = msg.dropLast(1).toDouble()
    var money : Double = startmoney


    when(type) {
        'K' -> money = (startmoney*1000)
        'M' -> money = (startmoney*1000000)
        'B' -> money = (startmoney*1000000000)
        'T' -> money = (startmoney*1000000000000)
    }

    //printMessage("${msg} | ${type} | ${startmoney} | ${money}")

    return money.toLong()
}

fun convertTo(msg: Long) : String {
    var length = msg.length()
    var money : String = "${msg}"
    var msgg = msg.toDouble()
    when(length) {
        in 1..3 -> money = "${String.format("%.2f", msgg)}$"
        in 4..6 -> money = "${String.format("%.2f", (msgg/1000))}K$"
        in 7..9 -> money = "${String.format("%.2f", (msgg/1000000))}M$"
        in 10..12 -> money = "${String.format("%.2f", (msgg/1000000000))}B$"
        in 13..15 -> money = "${String.format("%.2f", (msgg/1000000000000))}T$"
        in 16..18 -> money = "${String.format("%.2f", (msgg/1000000000000000))}Q$"
    }
    return money
}

fun Long.length() = when(this) {
    0L -> 1
    else -> log10(abs(toDouble())).toLong() + 1
}