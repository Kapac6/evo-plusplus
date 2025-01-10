package ru.dargen.evoplus.update

import ru.dargen.evoplus.EvoPlus
import ru.dargen.evoplus.Logger
import ru.dargen.evoplus.api.render.Colors
import ru.dargen.evoplus.api.render.Relative
import ru.dargen.evoplus.api.render.context.screen
import ru.dargen.evoplus.api.render.node.box.hbox
import ru.dargen.evoplus.api.render.node.box.vbox
import ru.dargen.evoplus.api.render.node.input.button
import ru.dargen.evoplus.api.render.node.text
import ru.dargen.evoplus.resource.Social
import ru.dargen.evoplus.util.catch
import ru.dargen.evoplus.util.math.v3
import ru.dargen.evoplus.util.minecraft.Client
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.URLClassLoader
import java.nio.channels.Channels
import java.nio.file.Path
import kotlin.concurrent.thread
import kotlin.io.path.deleteIfExists

object Updater {

    fun openUpdateScreenIfNeed() = catch("Error while attempting update") {
        if (UpdateResolver.isOutdated) openUpdateScreen()
    }

    fun openUpdateScreen() = screen {
        val version = UpdateResolver.latestVersion!!

        color = Colors.TransparentBlack

        +vbox {
            indent = v3()
            space = 20.0

            childrenRelative = .5

            origin = Relative.Center
            align = Relative.Center

            +text(
                "Обнаружена новая версия EvoPlus - ${version.friendlyName}",
                "",
                *version.changelog.lines().map("§7"::plus).toTypedArray(),
                "",
                if (version.isHotFix) "Для выхода с обновлением нажмите кнопку \"Обновить\"."
                else "Для перехода к обновлению нажмите кнопку \"Обновиться\"."
            ) {
                scale = v3(1.1, 1.1)
                isCentered = true
            }

            +vbox {
                childrenRelative = .5
                indent = v3()
                space = 3.0

                +hbox {
                    indent = v3()
                    space = 3.0

                    +button("Discord") {
                        buttonColor = Social.DISCORD.color
                        on { Social.DISCORD.open() }
                    }
                    +button("Modrinth") {
                        buttonColor = Social.MODRINTH.color
                        on { Social.MODRINTH.open() }
                    }
                }

                +button(if (version.isHotFix) "Обновить" else "Обновиться") {
                    buttonColor = Colors.Red
                    on {
                        if (version.isHotFix) update(version.file)
                        else {
                            Social.MODRINTH.open()
                            Desktop.getDesktop().open(File("mods"))
                        }
                    }
                }
            }
        }
    }.open()

    private fun update(file: Modrinth.VersionInfo.FileInfo) {
        catch("Error while downloading latest EvoPlus version") {
            val input = Channels.newChannel(URI(file.url).toURL().openStream())
            val output = FileOutputStream(File("mods", file.filename)).channel
            output.transferFrom(input, 0, Long.MAX_VALUE)
        }

        Logger.info("Downloaded latest EvoPlus version ${file.filename}")

        thread(true, true) {
            Thread.sleep(1000)
            EvoPlus.Container.origin.paths.forEach(Path::deleteIfExists)

            Logger.info("Shutting down client")

            Client.scheduleStop()
        }

        catch("Error while closing classloader") {
            val loader = Updater::class.java.classLoader.parent as URLClassLoader
            loader.close()
        }
    }

}