package ru.dargen.evoplus.feature.type.clicker

import ru.dargen.evoplus.util.minecraft.ClientExtension

enum class ClickerState(val display: String) {

    LEFT("���") {
        override fun invoke() {
            ClientExtension.leftClick()
        }
    },
    RIGHT("���") {
        override fun invoke() {
            ClientExtension.rightClick()
        }
    };

    abstract operator fun invoke()

    override fun toString() = display
}