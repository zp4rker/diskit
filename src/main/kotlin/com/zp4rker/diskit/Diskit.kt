package com.zp4rker.diskit

import com.zp4rker.discore.API
import com.zp4rker.discore.command.CommandHandler
import com.zp4rker.discore.extenstions.event.on
import com.zp4rker.diskit.command.LinkCommand
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.InterfacedEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author zp4rker
 */
class Diskit : JavaPlugin() {

    override fun onEnable() {
        // MC side of things
        initMetrics().let {
            logger.info("Metrics is ${if (it.isEnabled) "enabled" else "disabled"}.")
        }
        saveDefaultConfig()
        registerCommands()

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

        with(CommandHandler(config.getString("bot-settings.prefix", "!")!!)) {
            /* register commands here */
        }

        API.on<ReadyEvent> {
            logger.info("Discord bot now ready!")
        }
    }

    private fun initMetrics() = Metrics(this, 9607)

    private fun registerCommands() {
        getCommand("link")?.setExecutor(LinkCommand())
    }

}