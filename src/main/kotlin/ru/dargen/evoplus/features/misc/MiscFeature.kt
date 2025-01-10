package ru.dargen.evoplus.features.misc

import net.minecraft.client.MinecraftClient
import net.minecraft.item.Items
import ru.dargen.evoplus.api.event.chat.ChatReceiveEvent
import ru.dargen.evoplus.api.event.evo.EvoJoinEvent
import ru.dargen.evoplus.api.event.game.PostTickEvent
import ru.dargen.evoplus.api.event.on
import ru.dargen.evoplus.api.event.resourcepack.ResourcePackRequestEvent
import ru.dargen.evoplus.api.keybind.Keybinds
import ru.dargen.evoplus.api.keybind.on
import ru.dargen.evoplus.api.scheduler.schedule
import ru.dargen.evoplus.extension.ResourcePackManagerExtension
import ru.dargen.evoplus.feature.Feature
import ru.dargen.evoplus.features.misc.selector.FastSelectorScreen
import ru.dargen.evoplus.features.misc.selector.FastSelectorSetting
import ru.dargen.evoplus.resource.builtin.EvoPlusPackProvider
import ru.dargen.evoplus.resource.diamondworld.DiamondWorldPackProvider
import ru.dargen.evoplus.util.kotlin.cast
import ru.dargen.evoplus.util.minecraft.Client
import ru.dargen.evoplus.util.minecraft.CurrentScreen
import ru.dargen.evoplus.util.minecraft.Player
import ru.dargen.evoplus.util.minecraft.sendCommand
import ru.dargen.evoplus.util.minecraft.uncolored
import java.util.concurrent.TimeUnit


object MiscFeature : Feature("misc", "Прочее", Items.REPEATER) {

    private val BoosterMessagePattern = "^[\\w\\s]+ активировал глобальный бустер".toRegex()

    val FastSelector by settings.boolean("Fast-селектор", true)
    val FastSelectorItems by settings.setting(FastSelectorSetting)

    val AutoSprint by settings.boolean("Авто-спринт", true) on { if (!it) Player?.isSprinting = false }
    val AutoThanks by settings.boolean("Авто /thx", true)

    val ResourcePackPreload by settings.boolean("Пред-загрузка РП DiamondWorld", true) on {
        MinecraftClient.getInstance().reloadResources()
    }
    val ShowServerInTab by settings.boolean("Показывать текущий сервер в табе", true)

    val NotifiesWidget by widgets.widget("Уведомления", "notifies-widget", widget = Notifies)
    val CaseNotify by settings.boolean("Уведомления о кейсах", true)
    val LuckyBlockNotify by settings.boolean("Уведомления о лаки-блоках", true)
    val CollectionNotify by settings.boolean("Уведомления о коллекционках", true)


    init {
        Keybinds.FastSelector.on { if (CurrentScreen == null && FastSelector) FastSelectorScreen.open() }

        on<PostTickEvent> { Player?.apply { if (AutoSprint && forwardSpeed > 0) isSprinting = true } }
        on<ChatReceiveEvent> {
            val text = text.uncolored()

            if (BoosterMessagePattern.containsMatchIn(text)) thx()
            if (text.startsWith("Вы нашли")) {
                if (CaseNotify && text.contains("кейс")) Notifies.showText("§6$text")
                if (CollectionNotify && text.contains("коллекционный предмет")) Notifies.showText("§a$text")
                if (LuckyBlockNotify && text.contains("лаки-блок")) Notifies.showText("§e$text")
            }
        }
        on<EvoJoinEvent> { schedule(5, TimeUnit.SECONDS) { thx() } }

        Client.resourcePackManager
            .cast<ResourcePackManagerExtension>()
            .appendProviders(setOf(EvoPlusPackProvider(), DiamondWorldPackProvider()))

        on<ResourcePackRequestEvent> {
            if (ResourcePackPreload) {
                responseAccepted = true
                cancel()
            }
        }
        FastSelectorScreen
    }

    fun thx() {
        if (AutoThanks) {
            sendCommand("thx")
        }
    }

}
