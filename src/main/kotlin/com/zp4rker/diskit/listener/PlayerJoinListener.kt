package com.zp4rker.diskit.listener

import com.zp4rker.diskit.AccountLinker
import com.zp4rker.diskit.PLUGIN
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * @author zp4rker
 */
class PlayerJoinListener : Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, PLUGIN)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (AccountLinker.searchUser(event.player) != null) AccountLinker.sync(event.player)
    }

}