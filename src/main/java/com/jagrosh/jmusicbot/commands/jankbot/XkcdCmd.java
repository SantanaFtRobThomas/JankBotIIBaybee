package com.jagrosh.jmusicbot.commands.jankbot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;

public class XkcdCmd extends Command {

    public XkcdCmd(Bot bot) {
        this.name = "xkcd";
        this.help = "XKCD comics.";
        this.guildOnly = true;
    }

    public void execute(CommandEvent event) {
        String args = event.getArgs().contains(" ") ? event.getArgs().split(" ")[0] : event.getArgs();
        boolean random = args.equals("random");
        if (random) args = "";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(random ? new URI("https://xkcd.com/info.0.json") : new URI("https://xkcd.com/" + args + "/info.0.json"))
                    .build();
            HttpResponse<String> response = HttpClient.newBuilder().build().send(request, BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                event.replyError("404: Comic not found.");
                return;
            }
            String json = response.body();
            JsonElement parser = JsonParser.parseString(json);
            if (random) {
                int num = (int) (Math.random() * parser.getAsJsonObject().get("num").getAsInt());
                if(num == 0) num = 1;
                parser = JsonParser.parseString(HttpClient.newBuilder().build().send(HttpRequest.newBuilder()
                        .GET()
                        .uri(new URI("https://xkcd.com/" + Integer.toString(num) + "/info.0.json"))
                        .build(), BodyHandlers.ofString()).body());
            }
            String title = parser.getAsJsonObject().get("safe_title").getAsString();
            String img = parser.getAsJsonObject().get("img").getAsString();
            String num = parser.getAsJsonObject().get("num").getAsString();
            event.reply("XKCD " + num + ": " + title + "\n" + img);

        } catch (Exception e) {
            event.reply("Error: " + e.getMessage());
            return;
        }

    }

}
