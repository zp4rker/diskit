package com.zp4rker.diskit.command.dsc

import com.zp4rker.discore.command.Command
import com.zp4rker.discore.extenstions.embed
import com.zp4rker.diskit.AccountLinker
import com.zp4rker.diskit.DCMDHANDLER
import com.zp4rker.diskit.PLUGIN
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author zp4rker
 */
class AdminLinkCommand : Command(
    aliases = arrayOf("link"),
    description = "Admin command to manually link accounts.",
    usage = "${DCMDHANDLER.prefix}link <MC UUID> @user",
    permission = Permission.ADMINISTRATOR
) {

    init {
        DCMDHANDLER.registerCommands(this)
    }

    override fun handle(args: Array<String>, message: Message, channel: TextChannel) {
        val uuid = kotlin.runCatching { UUID.fromString(args[0]) }.getOrNull()
        if (uuid == null) {
            channel.sendMessage(embed {
                title {
                    text = "Failed to link accounts!"
                }

                description = "That UUID seems to be invalid."

                color = "#ec644b"
            }).queue { it.delete().queueAfter(5, TimeUnit.SECONDS) }
            return
        }

        val user = message.mentionedUsers[0] ?: return

        if (AccountLinker.searchPlayer(user) != null) {
            channel.sendMessage(embed {
                title {
                    text = "Failed to link accounts!"
                }

                description = "That user/account has already been linked."

                color = "#ec644b"
            }).queue { it.delete().queueAfter(5, TimeUnit.SECONDS) }
            return
        }

        val player = kotlin.runCatching { Bukkit.getOfflinePlayer(uuid) }.run {
            exceptionOrNull()?.let { PLUGIN.logger.warning(it.message) }
            getOrNull()
        }
        if (player == null) {
            channel.sendMessage(embed {
                title {
                    text = "Failed to link accounts!"
                }

                description = "Unable to find player with that UUID."

                color = "#ec644b"
            }).queue { it.delete().queueAfter(5, TimeUnit.SECONDS) }
            return
        }

        AccountLinker.link(player, user)

        channel.sendMessage(embed {
            title {
                text = "Minecraft Account Link"
            }

            description = "Successfully linked **${user.asTag}** to **${player.name}**."

            thumbnail = "https://crafatar.com/renders/head/${player.uniqueId}.png?overlay"

            footer {
                text = "UUID: $uuid"
            }
        }).queue()

        user.openPrivateChannel().submit().thenAccept {
            it.sendMessage(embed {
                title {
                    text = "Minecraft Account Link"

                    description = "Successfully linked to your Minecraft account!"

                    thumbnail = "https://crafatar.com/renders/head/${player.uniqueId}.png?overlay"

                    footer {
                        text = "UUID: ${player.uniqueId}"
                    }
                }
            }).queue()
        }

        if (player.isOnline) player.player?.sendMessage("${ChatColor.GREEN}Your account was linked to ${user.asTag}!")
    }

}