package com.zp4rker.diskit.command

import com.zp4rker.discore.API
import com.zp4rker.discore.extenstions.embed
import com.zp4rker.discore.extenstions.event.Predicate
import com.zp4rker.discore.extenstions.event.expect
import com.zp4rker.diskit.AccountLinker
import com.zp4rker.diskit.PLUGIN
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

/**
 * @author zp4rker
 */
class LinkCommand : CommandExecutor {

    init {
        PLUGIN.getCommand("link")?.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.DARK_RED}Only players can run that command!")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}You must either provide a username (eg. zp4rker#3333) or a user ID (eg. 145064570237485056)")
            return false
        }

        if (AccountLinker.searchUser(sender) != null) {
            sender.sendMessage("${ChatColor.RED}You have already linked your account!")
            return true
        }

        val user = if (args.size == 1 && args[0].toCharArray().all(Char::isDigit)) {
            API.getUserById(args[0])
        } else {
            val input = args.joinToString(" ")
            if (!input.matches(Regex("^.*#\\d{4}$"))) {
                sender.sendMessage("${ChatColor.RED}Invalid username! Make sure not to forget the discriminator.")
                return false
            }
            API.getUserByTag(input)
        } ?: run {
            sender.sendMessage("${ChatColor.RED}Unable to find that user! Please try again.")
            return true
        }

        user.openPrivateChannel().submit().handle { pc, t ->
            if (t != null) {
                sender.sendMessage("${ChatColor.RED}Unable to open DM with user, please make sure you have DMs enabled.")
            } else {
                pc.sendMessage(embed {
                    title {
                        text = "Minecraft Account Link"
                    }

                    description = "React with \u2705 to link your Discord account to **${sender.name}**."

                    thumbnail = "https://crafatar.com/renders/head/${sender.uniqueId}.png?overlay"

                    footer {
                        text = "This message expires in 2 minutes."
                    }
                }).queue {
                    it.addReaction("\u2705").queue()
                    sender.sendMessage("${ChatColor.YELLOW}Check your DMs on Discord.")

                    val reactionPredicate: Predicate<MessageReactionAddEvent> = { e ->
                        e.user == user && e.reactionEmote.emoji == "\u2705"
                    }
                    val timeoutAction: () -> Unit = { it.delete().queue() }

                    it.expect(reactionPredicate, timeout = 2, timeoutUnit = TimeUnit.MINUTES, timeoutAction = timeoutAction) { _ ->
                        sender.sendMessage("${ChatColor.GREEN}Successfully linked to your Discord account!")

                        pc.sendMessage(embed {
                            title {
                                text = "Minecraft Account Link"

                                description = "Successfully linked to your Minecraft account!"

                                thumbnail = "https://crafatar.com/renders/head/${sender.uniqueId}.png?overlay"

                                footer {
                                    text = "UUID: ${sender.uniqueId}"
                                }
                            }
                        }).queue()
                        it.delete().queue()

                        AccountLinker.link(sender, user)
                    }
                }
            }
        }

        return true
    }

}