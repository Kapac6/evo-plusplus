package ru.dargen.evoplus.render.node.state

import ru.dargen.evoplus.render.animation.Easings
import ru.dargen.evoplus.render.animation.animate
import ru.dargen.evoplus.render.animation.cancelAnimation
import ru.dargen.evoplus.render.node.RectangleNode
import ru.dargen.evoplus.render.node.rectangle
import ru.dargen.evoplus.util.kotlin.KotlinOpens
import ru.dargen.evoplus.util.math.Vector3
import java.awt.Color

@KotlinOpens
class HBarNode : RectangleNode() {

    val progressRectangle = +rectangle()
    override var size: Vector3
        get() = super.size
        set(value) {
            super.size = value
            cancelAnimation("progress")
            progressRectangle.size.y = size.y
            progress = progress
        }

    var interpolationEasing = Easings.Linear
    var interpolationTime = .05

    var progress = .0
        set(value) {
            field = value.coerceIn(.0, 1.0)
            animate("progress", interpolationTime, interpolationEasing) {
                progressRectangle.size = size.clone().apply { x *= field }
            }
        }

    var backgroundColor: Color
        get() = color
        set(value) {
            color = value
        }
    var progressColor: Color
        get() = progressRectangle.color
        set(value) {
            progressRectangle.color = value
        }

    init {
        backgroundColor = Color.WHITE
        progressColor = Color.GREEN
    }

}

fun hbar(block: HBarNode.() -> Unit = {}) = HBarNode().apply(block)