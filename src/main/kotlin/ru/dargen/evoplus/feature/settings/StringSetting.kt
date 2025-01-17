package ru.dargen.evoplus.feature.settings

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import ru.dargen.evoplus.render.node.input.button
import ru.dargen.evoplus.feature.screen.FeatureBaseElement

class StringSetting(id: String, name: String, value: String) : Setting<String>(id,name) {

    override var value: String = value
        set(value) {
            field = value
            handler(value)
        }

    override val settingElement = FeatureBaseElement(name) {
        button(value)
    }

    override fun load(element: JsonElement) {
        value = element.asString
    }

    override fun store() = JsonPrimitive(value)
}