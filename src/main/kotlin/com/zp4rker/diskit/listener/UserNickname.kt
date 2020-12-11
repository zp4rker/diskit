package com.zp4rker.diskit.listener

import com.zp4rker.discore.API
import com.zp4rker.discore.extenstions.event.on
import com.zp4rker.diskit.AccountLinker
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent

/**
 * @author zp4rker
 */
class UserNickname {

    init {
        API.on<GuildMemberUpdateNicknameEvent> {
            if (AccountLinker.searchPlayer(it.user) != null) {
                it.member.modifyNickname(it.oldNickname).queue()
            }
        }
    }

}