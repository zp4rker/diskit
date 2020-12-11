package com.zp4rker.diskit

import com.zp4rker.discore.API
import com.zp4rker.discore.extenstions.addRole
import net.dv8tion.jda.api.entities.User
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

/**
 * @author zp4rker
 */
object AccountLinker {

    private val cache = mutableMapOf<UUID, String>()

    fun link(player: Player, user: User) {
        cache[player.uniqueId] = user.id
        sync(player)
    }

    fun sync(player: Player) {
        val user = searchUser(player) ?: return
        val guild = API.getGuildById(PLUGIN.config.getString("bot-settings.server-id", "")!!) ?: return
        val member = guild.getMember(user) ?: return

        val displayName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', player.displayName)) ?: return
        if (member.effectiveName != displayName && !member.isOwner) member.modifyNickname(displayName).queue()

        val rsp = PLUGIN.server.servicesManager.getRegistration(Permission::class.java) ?: return
        val perms = rsp.provider
        for (group in perms.getPlayerGroups(player)) {
            val role = guild.getRolesByName(group, false).getOrNull(0)
            if (role == null) {
                PLUGIN.logger.warning("Unable to find role: $group")
                continue
            }
            if (!member.roles.contains(role)) member.addRole(role).queue()
        }
    }

    fun searchUser(player: Player): User? {
        return API.getUserById(cache.getOrElse(player.uniqueId) {
            PLUGIN.reloadConfig()
            val search = PLUGIN.config.getString("linked-users.${player.uniqueId}") ?: return null

            if (cache.size >= 100) flushCache()
            cache[player.uniqueId] = search
            search
        })
    }

    fun searchPlayer(user: User): Player? {
        return cache.keys.find { cache[it] == user.id }?.let { Bukkit.getPlayer(it) } ?: run {
            PLUGIN.reloadConfig()
            val linkedUsers = PLUGIN.config.getConfigurationSection("linked-users") ?: return null

            var search = ""
            for (key in linkedUsers.getKeys(false)) if (linkedUsers.getString(key) == user.id) search = key
            if (search == "") return null

            val uuid = UUID.fromString(search)

            if (cache.size >= 100) flushCache()
            cache[uuid] = user.id
            Bukkit.getPlayer(uuid)
        }
    }

    fun flushCache() {
        if (cache.size < 10) return

        for (entry in cache) {
            PLUGIN.config.set("linked-users.${entry.key}", entry.value)
        }
        PLUGIN.saveConfig()
        cache.clear()
    }

    val cacheRunnable = object : BukkitRunnable() {
        override fun run() {
            flushCache()
        }
    }

}