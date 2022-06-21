package com.jagrosh.jmusicbot.commands.jankbot

import com.google.gson.JsonParser
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.Bot
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

class XkcdCmd(bot: Bot) : Command() {
    init {
        name = "xkcd"
        help =
            "XKCD comics. Use " + bot.config.prefix + "xkcd <number> to get that comic, or the word `random` to get a random comic. If you don't specify a comic number, the latest comic will be shown."
        guildOnly = true
    }

    public override fun execute(event: CommandEvent) {
        var args = if (event.args.contains(" ")) event.args.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[0] else event.args
        val random = args == "random"
        if (random) args = ""
        try {
            val request = HttpRequest.newBuilder()
                .GET()
                .uri(if (random) URI("https://xkcd.com/info.0.json") else URI("https://xkcd.com/$args/info.0.json"))
                .build()
            val response = HttpClient.newBuilder().build().send(request, BodyHandlers.ofString())
            if (response.statusCode() == 404) {
                event.replyError("404: Comic not found.")
                return
            }
            val json = response.body()
            var parser = JsonParser.parseString(json)
            if (random) {
                var num = (Math.random() * parser.asJsonObject["num"].asInt).toInt()
                if (num == 0) num = 1
                parser = JsonParser.parseString(
                    HttpClient.newBuilder().build().send(
                        HttpRequest.newBuilder()
                            .GET()
                            .uri(URI("https://xkcd.com/$num/info.0.json"))
                            .build(), BodyHandlers.ofString()
                    ).body()
                )
            }
            val title = parser.asJsonObject["safe_title"].asString
            val img = parser.asJsonObject["img"].asString
            val num = parser.asJsonObject["num"].asString
            val alt = parser.asJsonObject["alt"].asString
            event.reply("https://xkcd.com/$num")
        } catch (e: Exception) {
            event.reply("Error: " + e.message)
            return
        }
    }
}