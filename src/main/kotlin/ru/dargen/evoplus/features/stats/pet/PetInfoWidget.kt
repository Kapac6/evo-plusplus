package ru.dargen.evoplus.features.stats.pet

import ru.dargen.evoplus.feature.widget.WidgetBase
import ru.dargen.evoplus.feature.widget.isWidgetEditor
import ru.dargen.evoplus.protocol.collector.PlayerDataCollector
import ru.dargen.evoplus.protocol.collector.data.PetData
import ru.dargen.evoplus.render.Relative
import ru.dargen.evoplus.render.node.Node
import ru.dargen.evoplus.render.node.box.hbox
import ru.dargen.evoplus.render.node.box.vbox
import ru.dargen.evoplus.render.node.item
import ru.dargen.evoplus.render.node.text
import ru.dargen.evoplus.util.format.format
import ru.dargen.evoplus.util.math.scale
import ru.dargen.evoplus.util.math.v3

object PetInfoWidget : WidgetBase {

    override val node = vbox {
        space = .0
        indent = v3()
    }

    fun update() {
        node._childrens = PlayerDataCollector.pets
            .ifEmpty { if (isWidgetEditor) listOf(PetData.random() ?: return, PetData.random() ?: return) else emptyList() }
            .map {
                val type = it.type

                hbox {
                    space = 1.0
                    indent = v3()

                    +item(type.displayItem) { scale = scale(.7, .7) }
                    +text(
                        "${type.displayName} §8[§e${it.level}§8] §8(§e${it.energy.format("###")}⚡§8)"
                    ) { isShadowed = true }
                    recompose()
                }
            }.toMutableList()
    }

    override fun Node.prepare() {
        origin = Relative.CenterBottom
        align = v3(.75, .99)
    }
}