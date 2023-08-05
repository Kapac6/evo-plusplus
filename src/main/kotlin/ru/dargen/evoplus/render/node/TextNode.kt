package ru.dargen.evoplus.render.node

import net.minecraft.client.util.math.MatrixStack
import ru.dargen.evoplus.render.Colors
import ru.dargen.evoplus.util.kotlin.KotlinOpens
import ru.dargen.evoplus.util.math.Vector3
import ru.dargen.evoplus.util.render.Render
import ru.dargen.evoplus.util.render.drawText

@KotlinOpens
class TextNode(lines: List<String>) : Node() {
    constructor(vararg lines: String) : this(lines.toList())
    constructor(line: String) : this(line.split('\n'))

    val linesCount get() = lines.size
    var lines: List<String> = lines
        set(value) {
            field = value
            prepare()
        }
    var text: String
        get() = lines.joinToString("\n")
        set(value) {
            lines = value.split('\n')
        }
    var linesWithWidths: Map<String, Int> = emptyMap()

    var linesSpace = 1.0
    var isShadowed = true
    var isCentered = false

    init {
        color = Colors.White
        prepare()
    }

    fun prepare() {
        val renderer = Render.TextRenderer

        linesWithWidths = lines.associateWith { renderer.getWidth(it) }
        size.set(
            (linesWithWidths.values.maxOrNull() ?: 0) + 1.5,
            linesCount * renderer.fontHeight + (linesCount - 1) * linesSpace, .0
        )
    }

    override fun renderElement(matrices: MatrixStack, tickDelta: Float) {
        val renderer = Render.TextRenderer

        val height = renderer.fontHeight
        var index = 0
        linesWithWidths.forEach { (line, width) ->
            val x = if (isCentered) size.x / 2.0 - width / 2.0 else .0
            val y = index * height + (++index - 1) * linesSpace

            matrices.drawText(text, Vector3(x, y), isShadowed, color.rgb)
        }
    }

}

fun text(vararg lines: String, block: TextNode.() -> Unit = {}) = TextNode(*lines).apply(block)

fun text(lines: List<String>, block: TextNode.() -> Unit = {}) = TextNode(lines).apply(block)