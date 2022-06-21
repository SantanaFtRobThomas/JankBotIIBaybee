package com.jagrosh.jmusicbot.commands.tantamod

import com.google.api.services.youtube.model.*
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.Bot
import com.jagrosh.jmusicbot.commands.TantamodCommand
import com.jagrosh.jmusicbot.commands.jankbot.GetYoutubeAuth
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageHistory
import java.util.List
import java.util.regex.Pattern

class GenerateGramophonePlaylistCmd(bot: Bot?) : TantamodCommand(bot) {
    private val YOUTUBE_PATTERN = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?][A-Za-z0-9_-]{10,}"

    init {
        name = "gramophoneplaylist"
        help = "Generate a playlist from a gramophone thread. `j!gramophoneplaylist <thread_id>`"
    }

    public override fun execute(event: CommandEvent) {
        if (event.args.isNotEmpty()) {
            try {
                val ch = event.guild.getThreadChannelById(event.args)
                if (ch != null) {
                    if (ch.messageCount > 100) {
                        event.reply(
                            "The thread has more than 100 messages. For now, I'll only look at the first 100 but this will change if it becomes a problem."
                        )
                    }
                    ch.getHistoryFromBeginning(100).queue { msgs: MessageHistory ->
                        val pattern = Pattern.compile(YOUTUBE_PATTERN)
                        var all_msgs = msgs.retrievedHistory
                        val rev_msg: MutableList<Message> = ArrayList()
                        val urls: MutableList<String> = ArrayList()
                        for (i in all_msgs.indices.reversed()) rev_msg.add(all_msgs[i]) //Collections.reverse() throws a weird error 
                        all_msgs = rev_msg
                        for (m in all_msgs) {
                            val matcher = pattern.matcher(m.contentRaw)
                            while (matcher.find()) {
                                urls.add(matcher.group(0))
                            }
                        }
                        if (urls.size == 0) {
                            event.replyError("No youtube links found in thread.")
                            return@queue
                        }
                        try {
                            val svc = GetYoutubeAuth.getService()
                            val playlist = svc.playlists().insert(
                                listOf("snippet", "status"),
                                Playlist()
                                    .setSnippet(
                                        PlaylistSnippet()
                                            .setTitle(ch.name)
                                            .setDescription(
                                                "Playlist generated from gramophone thread "
                                                        + ch.name + "."
                                            )
                                    )
                                    .setStatus(
                                        PlaylistStatus()
                                            .setPrivacyStatus("public")
                                    )
                            )
                            val p = playlist.execute()
                            val playlist_id = p.id
                            println("Created playlist with id $playlist_id")
                            event.reply("Creating playlist. Please wait...")
                            var ctr = 0L
                            urls.removeAt(0) // pop the first entry off because it's probably the meme example
                            for (url in urls) {
                                try {
                                    val playlistItem = PlaylistItem()
                                    val snippet = PlaylistItemSnippet()
                                    snippet.playlistId = playlist_id
                                    snippet.position = ctr++
                                    snippet.resourceId = ResourceId()
                                        .setKind("youtube#video").setVideoId(url)
                                    playlistItem.snippet = snippet
                                    val rq = svc.playlistItems().insert(
                                        listOf("snippet"),
                                        playlistItem
                                    )
                                    println(rq.toString())
                                    println(rq.httpContent)
                                    println(rq.jsonContent)
                                    val resp = rq.execute()
                                    println(
                                        "Added video " + resp.snippet.resourceId.videoId
                                                + " to playlist " + playlist_id
                                    )
                                } catch (e: Exception) {
                                    println("Error adding $url: $e")
                                    continue
                                }
                            }
                            event.reply("Playlist created. https://www.youtube.com/playlist?list=$playlist_id")
                        } catch (e: Exception) {
                            event.replyError(
                                """
                                Something went wrong while trying to generate the playlist. <@204969832326627330> ```
                                $e
                                ```
                                """.trimIndent()
                            )
                            e.printStackTrace()
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