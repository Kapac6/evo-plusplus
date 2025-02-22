package ru.dargen.evoplus.features.stats

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import net.fabricmc.loader.impl.util.log.Log
import net.fabricmc.loader.impl.util.log.LogCategory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import ru.dargen.evoplus.event.chat.ChatReceiveEvent
import ru.dargen.evoplus.event.evo.data.ComboUpdateEvent
import ru.dargen.evoplus.event.evo.data.LevelUpdateEvent
import ru.dargen.evoplus.event.interact.BlockBreakEvent
import ru.dargen.evoplus.event.on
import ru.dargen.evoplus.feature.Feature
import ru.dargen.evoplus.features.misc.notify.NotifyWidget
import ru.dargen.evoplus.features.stats.combo.ComboWidget
import ru.dargen.evoplus.features.stats.level.LevelWidget
import ru.dargen.evoplus.features.stats.pet.PetInfoWidget
import ru.dargen.evoplus.protocol.collector.PlayerDataCollector.combo
import ru.dargen.evoplus.protocol.collector.PlayerDataCollector.economic
import ru.dargen.evoplus.render.Relative
import ru.dargen.evoplus.render.node.box.hbox
import ru.dargen.evoplus.render.node.box.vbox
import ru.dargen.evoplus.render.node.input.button
import ru.dargen.evoplus.render.node.item
import ru.dargen.evoplus.render.node.postRender
import ru.dargen.evoplus.render.node.text
import ru.dargen.evoplus.scheduler.scheduleEvery
import ru.dargen.evoplus.util.currentMillis
import ru.dargen.evoplus.util.math.v3
import ru.dargen.evoplus.util.minecraft.itemStack
import ru.dargen.evoplus.util.minecraft.uncolored
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.floor
import kotlin.math.max
import kotlin.time.TimeSource
import kotlin.time.measureTime
import ru.dargen.evoplus.util.format.asShortTextTime

object StatisticFeature : Feature("statistic", "Статистика", Items.PAPER) {

    val ActivePetsWidget by widgets.widget("Активные питомцы", "active-pets", widget = PetInfoWidget)

    private val ComboTimerPattern =
        "Комбо закончится через (\\d+) секунд\\. Продолжите копать, чтобы не потерять его\\.".toRegex()

    val ComboCounterWidget by widgets.widget("Счетчик комбо", "combo-counter", widget = ComboWidget)
    val ComboProgressBarEnabled by settings.boolean("Шкала прогресса комбо") on {
        ComboWidget.ProgressBar.enabled = it
    }

    val LevelRequireWidget by widgets.widget("Требования на уровень", "level-require", widget = LevelWidget)
    val LevelProgressBarEnabled by settings.boolean("Шкала прогресса уровня") on {
        LevelWidget.ProgressBar.enabled = it
    }
    val NotifyCompleteLevelRequire by settings.boolean(
        "Уведомлять при выполнении требований",
        true
    )


    private val MoneyBoostExpression =
            "Вы получили локальный бустер денег x(\\d+\\.\\d+) на (\\d+) минут!".toRegex()

    private val ShardBoostExpression =
            "Вы получили локальный бустер шардов x(\\d+\\.\\d+) на (\\d+) минут!".toRegex()

    val BoostTimersText = text("") {isShadowed = true}
    val ShardBoostTimersText = text("") {isShadowed = true}

    val BoostTimersWidget by widgets.widget("Таймер бустов", "boost-timers", false) {
        origin = Relative.Center
        align = Relative.Center
        +vbox {
            space = .0
            indent = v3()

            +BoostTimersText
            +text(" ")
            +ShardBoostTimersText
        }
    }

    data class Boost(
            var booster: Float,
            var time: Int
    )

    val BoostArray: MutableList<Boost> = ArrayList()
    val BoostTextList: MutableList<String> = ArrayList()

    val ShardBoostArray: MutableList<Boost> = ArrayList()
    val ShardBoostTextList: MutableList<String> = ArrayList()




    var BlocksCount = 0
        set(value) {
            field = value
            BlocksCounterText.text = "${max(economic.blocks - field, 0)}"
        }

    val BlocksCounterText = text("0") { isShadowed = true }
    val BlocksCounterWidget by widgets.widget("Счетчик блоков", "block-counter") {
        origin = Relative.LeftCenter
        align = v3(.87, .54)
        +hbox {
            space = .0
            indent = v3()

            +BlocksCounterText
            +item(itemStack(Items.DIAMOND_PICKAXE)) {
                scale = v3(.7, .7, .7)
            }
        }
    }

    val ResetBlocksCounter =
            settings.baseElement("Сбросить счетчик блоков") {
                button("Сбросить") {
                    on {
                        BlocksCount = economic.blocks } } }


    var TotalBlocks = 0
    var Uptime = 0
    var startTime = -1L
    var lastMined = -1L
    var pause = 0

    val BPHText = text("0") {isShadowed = true}
    val UptimeText = text("0") {isShadowed = true}
    val BlocksPerHourText = text("7") { isShadowed = true }
    val BlocksPerHourWidget by widgets.widget("Счетчик блоков в час", "block-counter-per-hour", false) {
        origin = Relative.LeftCenter
        align = v3(.87, .54)
        +hbox {
            space = .0
            indent = v3()
            +item(itemStack(Items.NETHERITE_PICKAXE)) {
                scale = v3(1.6, 1.6, 1.6)
            }

            +BPHText

        }
    }

    val ResetBlocksPerHourCounter =
            settings.baseElement("Сбросить счетчик блоков в час") {
                button("Сбросить") {
                    on {
                        pause = 0
                        TotalBlocks = 0
                        Uptime = 0
                        startTime = -1
                        lastMined = -1
                    }
                }
            }





    
    var BlocksPerSecondCounter = mutableListOf<Long>()
    val BlocksPerSecondWidget by widgets.widget("Счетчик блоков за секунду", "blocks-per-second-counter") {
        origin = Relative.LeftCenter
        align = v3(.87, .60)
        +hbox {
            space = .0
            indent = v3()

            +text("0") {
                isShadowed = true
                postRender { _, _ ->
                    BlocksPerSecondCounter.removeIf { currentMillis - it > 1000 }
                    text = "${BlocksPerSecondCounter.size}"
                }
            }
            +item(itemStack(Items.WOODEN_PICKAXE)) {
                scale = v3(.7, .7, .7)
            }


        }
    }



    init {
        scheduleEvery(unit = TimeUnit.SECONDS) {
            PetInfoWidget.update()
            ComboWidget.update(combo)

            BoostTextList.clear()
            ShardBoostTextList.clear()
            BoostTextList.add("§e$")
            ShardBoostTextList.add("\uE365")
            BoostArray.sortBy { it.time }
            for(Boost in BoostArray) {
                Boost.time--
                if(Boost.time <= 0) BoostArray.remove(Boost)
                BoostTextList.add("§6x${Boost.booster}§7-§e${(Boost.time * 1000).toLong().asShortTextTime}")
            }

            ShardBoostArray.sortBy { it.time }
            for(Boost in ShardBoostArray) {
                Boost.time--
                if(Boost.time <= 0) ShardBoostArray.remove(Boost)
                ShardBoostTextList.add("§6x${Boost.booster}§7-§e${(Boost.time * 1000).toLong().asShortTextTime}")
            }

            if(BoostArray.size<1) {
                BoostTextList.add("§6Бустов нет")
            }

            if(ShardBoostArray.size<1) {
                ShardBoostTextList.add("§6Бустов нет")
            }



            BoostTimersText.lines = BoostTextList

            ShardBoostTimersText.lines = ShardBoostTextList

            if(((currentMillis - lastMined)/1000 > 10) && startTime != -1L) {pause = 1}
            if(pause == 1) { startTime += 1000 }



            if(startTime == -1L) {
                BPHText.text = "-"
                Uptime = 0
            } else
            {

                var hour = Uptime / 3600
                var min = Uptime / 60 % 60
                var sec = Uptime / 1 % 60
                BoostTimersText.lines

                if(pause == 0) {

                    BlocksPerHourText.text = "${(floor((TotalBlocks.toDouble() / ((currentMillis.toDouble() - startTime.toDouble()) / (1000 * 60 * 60))))).toInt()}"

                    if (hour > 0 && min > 0) {
                        UptimeText.text = "${hour}ч ${min}м ${sec}с"
                    } else if (hour == 0 && min > 0) {
                        UptimeText.text = "${min}м ${sec}с"
                    } else {
                        UptimeText.text = "${sec}с"
                    }

                    BPHText.lines = listOf(
                            "§aВремя: §f${UptimeText.text}",
                            "§aБлоки/час: §f${BlocksPerHourText.text}",
                            "§aНакопано: §f${TotalBlocks}"
                    )

                    Uptime = ((currentMillis - startTime) / 1000).toInt()
                } else if(pause == 1) {
                    BPHText.lines = listOf(
                            "§2Время: §7${UptimeText.text}",
                            "§2Блоки/час: §7${BlocksPerHourText.text}",
                            "§2Накопано: §7${TotalBlocks}"
                    )
                }
            }

        }

        on<BlockBreakEvent> {
            BlocksPerSecondCounter.add(currentMillis)
            TotalBlocks++




            if(startTime == -1L) {startTime = currentMillis}
            lastMined = currentMillis
            pause = 0

        }

        on<ChatReceiveEvent> {
            ComboTimerPattern.find(text.uncolored())?.let {
                val remain = it.groupValues[1].toIntOrNull() ?: return@on
                combo.remain = remain.toLong()
                ComboWidget.update(combo)

            }

            MoneyBoostExpression.find(text.uncolored())?.let {
                val boost = it.groupValues[1].toFloat()
                val time = (it.groupValues[2].toInt())*60

                if(text.uncolored().startsWith("Вы получили")) {
                    BoostArray.add(Boost(boost, time))
                }
            }

            ShardBoostExpression.find(text.uncolored())?.let {
                val boost = it.groupValues[1].toFloat()
                val time = (it.groupValues[2].toInt())*60

                if(text.uncolored().startsWith("Вы получили")) {
                    ShardBoostArray.add(Boost(boost, time))
                }
            }
        }

        on<ComboUpdateEvent> {
            ComboWidget.update(combo)
        }

        on<LevelUpdateEvent> {
            LevelWidget.update(economic)

            if (NotifyCompleteLevelRequire && level.isCompleted && !previousLevel.isCompleted) {
                NotifyWidget.showText("§aВы можете повысить уровень!")
            }

            if (BlocksCount == 0) BlocksCount = economic.blocks
            BlocksCounterText.text = "${max(economic.blocks - BlocksCount, 0)}"
        }
    }

}