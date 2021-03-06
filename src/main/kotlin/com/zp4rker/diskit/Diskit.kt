package com.zp4rker.diskit

import com.zp4rker.discore.API
import com.zp4rker.discore.command.CommandHandler
import com.zp4rker.diskit.command.dsc.AdminLinkCommand
import com.zp4rker.diskit.command.mc.LinkCommand
import com.zp4rker.diskit.listener.mc.PlayerJoinListener
import com.zp4rker.diskit.listener.dsc.UserNickname
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.InterfacedEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author zp4rker
 */

lateinit var PLUGIN: Diskit
lateinit var DCMDHANDLER: CommandHandler

class Diskit : JavaPlugin() {

    override fun onEnable() {
        PLUGIN = this

        AccountLinker.cacheRunnable.runTaskTimer(this, 0, 20 * 60 * 5)

        // MC side of things
        saveDefaultConfig()
        bukkitCommands()
        bukkitListeners()

        if (config.getBoolean("metrics-enabled", true)) initMetrics().let {
            logger.info("Metrics is ${if (it.isEnabled) "enabled" else "disabled"}.")
        }

        // Discord side of things
        if (config.getString("bot-settings.token", "token.here") == "token.here") {
            logger.info("Token has not been set in the config! Disabling plugin...")
            server.pluginManager.disablePlugin(this)
            return
        }

        API = with(JDABuilder.createDefault(config.getString("bot-settings.token", "empty")!!)) {
            enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
            enableCache(CacheFlag.values().asList())
            setEventManager(InterfacedEventManager())

            build()
        }
        DCMDHANDLER = CommandHandler(config.getString("bot-settings.prefix", "!")!!)

        discordListeners()
        discordCommands()
    }

    override fun onDisable() {
        AccountLinker.flushCache(true)
    }

    private fun initMetrics() = Metrics(this, 9607)

    private fun bukkitCommands() {
        LinkCommand()
    }

    private fun bukkitListeners() {
        PlayerJoinListener()
    }

    private fun discordCommands() {
        AdminLinkCommand()
    }

    private fun discordListeners() {
        UserNickname()
    }

}