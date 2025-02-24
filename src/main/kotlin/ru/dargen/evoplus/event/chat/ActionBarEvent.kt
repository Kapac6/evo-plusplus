package ru.dargen.evoplus.event.chat

import net.minecraft.text.Text
import ru.dargen.evoplus.event.CancellableEvent
import ru.dargen.evoplus.event.Event

class ActionBarEvent(var actionbar: Text) : CancellableEvent() {
    val text get() = actionbar.string
}
