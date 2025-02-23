package ru.dargen.evoplus.util.math

import java.awt.Color

fun square(value: Double) = value * value

fun Double.progressTo(destination: Double, progress: Double) = this + (destination - this) * progress

fun Int.progressTo(destination: Int, progress: Double) = (this + (destination - this) * progress).toInt()

fun <N> N.fix(min: N, max: N) where N : Number, N : Comparable<N> = when {
    this < min -> min
    this > max -> max
    else -> this
}

fun map(x: Double, inputMin: Double, inputMax: Double, outputMin: Double, outputMax: Double) : Double {
    return((x - inputMin) * (outputMax - outputMin) / (inputMax - inputMin) + outputMin)
}

//fun HEXtoRGB(hex: Int) : Color {
//    val r = (hex and 0xFF0000) shr 16
//    val g = (hex and 0xFF00) shr 8
//    val b = (hex and 0xFF)
//    val Color = Color.decode()
//}