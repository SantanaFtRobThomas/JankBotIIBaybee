package com.jagrosh.jmusicbot.commands.jankbot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;

public class KittyCmd extends Command {
    public KittyCmd(Bot bot) {
        this.name = "kitty";
        this.help = "The Skcrungly Kitty.";
        this.guildOnly = true;
    }

    public void execute(CommandEvent event) {
        event.reply("https://imgs.xkcd.com/comics/cat_proximity.png");
    }
}
