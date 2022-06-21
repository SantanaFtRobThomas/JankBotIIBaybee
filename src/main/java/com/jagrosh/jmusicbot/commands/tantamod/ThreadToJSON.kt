package com.jagrosh.jmusicbot.commands.tantamod

import com.google.api.services.youtube.model.*
import com.google.gson.Gson
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.Bot
import com.jagrosh.jmusicbot.commands.TantamodCommand
import com.jagrosh.jmusicbot.commands.jankbot.GetYoutubeAuth
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.PrivateChannel
import java.io.File
import java.util.regex.Pattern

data class ThreadPost(
    val member: Long,
    val name: String,
    val content: String
)

class ThreadToJSON(bot: Bot?) : TantamodCommand(bot) {
    init {
        name = "jsonthread"
        help = "DMs a thread as a JSON object `j!jsonthread <thread_id>`"
    }

    public override fun execute(event: CommandEvent) {
        if (event.args.isNotEmpty()) {
            try {
                val ch = event.guild.getThreadChannelById(event.args)
                if (ch != null) {
                    val posts: MutableList<ThreadPost> = ArrayList()
                    if (ch.messageCount > 100) {
                        event.reply(
                            "The thread has more than 100 messages. For now, I'll only look at the first 100 but this will change if it becomes a problem."
                        )
                    }
                    ch.getHistoryFromBeginning(100).queue { msgs: MessageHistory ->
                        var all_msgs = msgs.retrievedHistory
                        val rev_msg: MutableList<Message> = ArrayList()
                        for (i in all_msgs.indices.reversed()) rev_msg.add(all_msgs[i]) //Collections.reverse() throws a weird error
                        all_msgs = rev_msg
                        for (m in all_msgs) {
                            posts.add(ThreadPost((m.author.idLong), m.author.name, m.contentRaw))
                        }
                        val gson = Gson()
                        val jsonstring = gson.toJson(posts).toByteArray()
                        event.member.user.openPrivateChannel().queue { channel ->
                            channel.sendFile(jsonstring, "${event.args}.json").queue()
                        }
                    }
                } else {
                    event.reply("Could not find thread with id " + event.args)
                }
            } catch (e: NumberFormatException) {
                event.reply("This doesn't look like a valid thread ID.")
            }
        }
    }
}