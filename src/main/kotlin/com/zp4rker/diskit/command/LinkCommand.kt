package com.zp4rker.diskit.command

import com.zp4rker.discore.API
import com.zp4rker.discore.extenstions.embed
import com.zp4rker.discore.extenstions.event.Predicate
import com.zp4rker.discore.extenstions.event.expect
import net.dv8tion.jda.api.entities.SelfUser
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

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.DARK_RED}Only players can run that command!")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("${ChatColor.DARK_RED}You must either provide a username (eg. zp4rker#3333) or a user ID (eg. 145064570237485056)")
            // send usage
            return true
        }

        val user = if (args.size == 1 && args[0].toCharArray().all(Char::isDigit)) {
            API.getUserById(args[0])
        } else {
            API.getUserByTag(args.joinToString(" "))
        } ?: run {
            sender.sendMessage("${ChatColor.RED}Unable to find that user! Please try again.")
            return true
        }

        user.openPrivateChannel().submit().handle { pc, t ->
            if (t != null) {
                sender.sendMessage("${ChatColor.RED}Unable to open DM with user, please make sure you have DMs enabled.")
            } else {
                val embed = embed {
                    title {
                        text = "Minecraft Account Link"
                    }

                    description = "React with \u2705 to link your Discord account to `${sender.name}`"

                    thumbnail = "https://crafatar.com/renders/head/${sender.uniqueId}.png?overlay"

                    footer {
                        text = "This message expires in 5 minutes."
                    }
                }
                sender.sendMessage("sending message...")

                pc.sendMessage(embed).queue {
                    it.addReaction("\u2705").queue()

                    val reactionPredicate: Predicate<MessageReactionAddEvent> = { e ->
                        e.user == user && e.reactionEmote.emoji == "\u2705"
                    }
                    val timeoutAction: () -> Unit = { it.delete().queue() }

                    it.expect(reactionPredicate, timeout = 5, timeoutUnit = TimeUnit.MINUTES, timeoutAction = timeoutAction) { _ ->
                        sender.sendMessage("${ChatColor.GREEN}Successfully linked to your Discord account!")

                        pc.sendMessage(embed {
                            title {
                                text = "Minecraft Account Link"

                                description = "Successfully linked to your Minecraft account!"

                                thumbnail = "https://crafatar.com/renders/head/${sender.uniqueId}.png?overlay"
                            }
                        }).queue()
                        it.delete().queue()

                        // save link
                    }
                }

                sender.sendMessage("should have sent message")
            }
        }

        return true
    }

}