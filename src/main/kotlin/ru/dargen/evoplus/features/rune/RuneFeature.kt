package ru.dargen.evoplus.features.rune

import net.minecraft.item.Items
import pro.diamondworld.protocol.packet.ability.AbilityTimers
import pro.diamondworld.protocol.packet.rune.ActiveRunes
import ru.dargen.evoplus.event.chat.ChatReceiveEvent
import ru.dargen.evoplus.event.interact.AttackEvent
import ru.dargen.evoplus.event.on
import ru.dargen.evoplus.feature.Feature
import ru.dargen.evoplus.features.misc.notify.NotifyWidget
import ru.dargen.evoplus.features.rune.widget.AbilityTimerWidget
import ru.dargen.evoplus.protocol.listen
import ru.dargen.evoplus.protocol.registry.AbilityType
import ru.dargen.evoplus.render.Colors
import ru.dargen.evoplus.render.Relative
import ru.dargen.evoplus.render.node.box.hbox
import ru.dargen.evoplus.render.node.item
import ru.dargen.evoplus.render.node.state.hbar
import ru.dargen.evoplus.render.node.text
import ru.dargen.evoplus.scheduler.scheduleEvery
import ru.dargen.evoplus.util.collection.concurrentHashMapOf
import ru.dargen.evoplus.util.currentMillis
import ru.dargen.evoplus.util.math.map
import ru.dargen.evoplus.util.math.v3
import ru.dargen.evoplus.util.minecraft.customItem
import ru.dargen.evoplus.util.minecraft.itemStack
import ru.dargen.evoplus.util.minecraft.printMessage
import ru.dargen.evoplus.util.minecraft.uncolored
import ru.dargen.evoplus.util.render.alpha

object RuneFeature : Feature("rune", "Руны", customItem(Items.PAPER, 445)) {

    val Abilities = concurrentHashMapOf<String, Long>()

    val ActiveRunesText = text(
        " §e??? ???", " §6??? ???",
        " §6??? ???", " §a??? ???", " §a??? ???"
    ) {
        isShadowed = true
    }
    val ActiveAbilitiesWidget by widgets.widget(
        "Задержка способностей",
        "active-abilities",
        enabled = false,
        widget = AbilityTimerWidget
    )
    val ActiveRunesWidget by widgets.widget("Надетые руны", "active-runes", enabled = false) {
        align = v3(0.25)
        origin = Relative.CenterTop

        +ActiveRunesText
    }
    var ReadyNotify by settings.boolean(
        "Уведомление при окончании задержки способностей",
        true
    )
    var ReadyMessage by settings.boolean(
        "Сообщение при окончании задержки способностей",
        true
    )

    val RunesBagProperties by settings.boolean(
        "Отображение статистики сета рун (в мешке)",
        true
    )
    val RunesBagSet by settings.boolean(
        "Отображать активный сет рун (в мешке)",
        true
    )
    val RunesSetSwitch by settings.boolean(
        "Смена сетов рун через A-D и 1-7 (в мешке)",
        true
    )

    init {
        scheduleEvery(period = 2) {
            updateAbilities()

            AbilityTimerWidget.update()
        }

        RunesBag

        listen<ActiveRunes> { activeRunes ->
            ActiveRunesText.text = activeRunes.data.joinToString("\n") { " $it" }
        }

        listen<AbilityTimers> {
            it.timers
                .filterValues { it > 1000 }
                .forEach { (id, timestamp) -> Abilities[id] = currentMillis + timestamp + 600 }
        }


        on<ChatReceiveEvent> {
            if(text.uncolored().startsWith("Способность \"Невидимость\" активировалась")) {
                NinjaStartTime = currentMillis + 7000
                activeNinja = 1
            }
        }

        on<AttackEvent> {
            NinjaStartTime = currentMillis
        }
    }

    private fun updateAbilities() {
        Abilities.forEach { (id, timestamp) ->
            val type = AbilityType.valueOf(id) ?: return@forEach
            val remainTime = timestamp - currentMillis

            if (remainTime in 0..1000) {
                if (ReadyNotify) NotifyWidget.showText("§aСпособность \"${type.name}\" готова")
                if (ReadyMessage) printMessage("§aСпособность \"${type.name}\" готова")
                Abilities.remove(id)
            }

            if (remainTime < 0) Abilities.remove(id)
        }

        if(activeNinja == 1) {
            NinjaTimeLeft = NinjaStartTime - currentMillis
            if(NinjaTimeLeft <= 0) {activeNinja = 0}
        }
        NinjaProgressBar.progress = map(NinjaTimeLeft.toDouble(), 0.0, 7000.0, 0.0, 1.0)
    }


    var activeNinja = 0
    var NinjaTimeLeft = 0L
    var NinjaStartTime = 0L

    val NinjaProgressBar = hbar {
        size = v3(500.0, 5.0)

        align = Relative.RightBottom
        origin = Relative.RightTop

        interpolationTime = .2

        backgroundColor = Colors.Transparent
        progressColor = Colors.Deepskyblue.alpha(.7)
    }

    val NinjaTimeWidget by widgets.widget("Время действия ниндзи", "ninja-time", false) {
        origin = Relative.Center
        align = Relative.Center
        +hbox {
            space = .0
            indent = v3()
            +NinjaProgressBar
        }
    }





}
