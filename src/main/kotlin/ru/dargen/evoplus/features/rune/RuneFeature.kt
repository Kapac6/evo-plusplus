package ru.dargen.evoplus.features.rune

import net.minecraft.client.sound.Sound
import net.minecraft.item.Items
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import pro.diamondworld.protocol.packet.ability.AbilityTimers
import pro.diamondworld.protocol.packet.rune.ActiveRunes
import ru.dargen.evoplus.EvoPlus
import ru.dargen.evoplus.Sound.SoundEventsz
import ru.dargen.evoplus.event.chat.ChatReceiveEvent
import ru.dargen.evoplus.event.interact.AttackEvent
import ru.dargen.evoplus.event.on
import ru.dargen.evoplus.feature.Feature
import ru.dargen.evoplus.features.misc.notify.NotifyWidget
import ru.dargen.evoplus.features.rune.widget.AbilityTimerWidget
import ru.dargen.evoplus.features.stats.combo.ComboWidget
import ru.dargen.evoplus.protocol.collector.PlayerDataCollector
import ru.dargen.evoplus.protocol.listen
import ru.dargen.evoplus.protocol.registry.AbilityType
import ru.dargen.evoplus.render.Colors
import ru.dargen.evoplus.render.Relative
import ru.dargen.evoplus.render.node.box.hbox
import ru.dargen.evoplus.render.node.state.hbar
import ru.dargen.evoplus.render.node.text
import ru.dargen.evoplus.scheduler.scheduleEvery
import ru.dargen.evoplus.util.collection.concurrentHashMapOf
import ru.dargen.evoplus.util.currentMillis
import ru.dargen.evoplus.util.format.asShortTextTime
import ru.dargen.evoplus.util.math.map
import ru.dargen.evoplus.util.math.v3
import ru.dargen.evoplus.util.minecraft.*
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
    val RuneSetCooldownText = text("Кд смены рун: §a✔") {isShadowed = true}
    var RuneSetCD = -1L
    val RuneSetCooldownWidget by widgets.widget("Задержка сетов рун", "rune-set-cooldown", enabled = false) {
        align = v3(0.25)
        origin = Relative.CenterTop

        +RuneSetCooldownText
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

    val Commentator by settings.boolean(
            "Профессиональный комментатор",
            true
    )

    val RuneCdRegex = "Вы не можете менять сет рун ещё (\\d+) секунд.".toRegex()

    init {
        scheduleEvery(period = 2) {
            updateAbilities()

            AbilityTimerWidget.update()
            if(currentMillis < RuneSetCD) {
                RuneSetCooldownText.text = "Кд смены рун: §c${(RuneSetCD-currentMillis).asShortTextTime}"
            } else {
                RuneSetCooldownText.text = "Кд смены рун: §a✔"
            }
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
                //World?.playSound(null, Player?.blockPos ?: BlockPos(2, 2, 2), SoundEventsz.ninja1_event, SoundCategory.MASTER, 1000f, 1f)

                //printMessage("${SoundEventsz.NINJA1} | ${Player} | ${SoundEventsz.NINJA1.id}")
                NinjaStartTime = currentMillis + 7000
                activeNinja = 1
                if(Commentator) {
                    Player?.playSound(SoundEventsz.NINJA1, 100f, 1f)
                }
            }
            if(text.uncolored().startsWith("Способность ")) {
                RuneSetCD = currentMillis + 10000
            }
            if(Commentator) {
                if (text.uncolored().startsWith("Способность \"Черная дыра\" активировалась")) {
                    Player?.playSound(SoundEventsz.VOID5, 100f, 1f)
                }
                if (text.uncolored().startsWith("Способность \"Рассвет\" активировалась")) {
                    Player?.playSound(SoundEventsz.FENIX1, 100f, 1f)
                }
            }

            RuneCdRegex.find(text.uncolored())?.let {
                val cd = it.groupValues[1].toLong()
                if(text.uncolored().startsWith("Вы не можете менять")) {
                    RuneSetCD = currentMillis + (cd*1000)
                }
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
