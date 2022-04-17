package com.jagrosh.jmusicbot.commands.tantamod;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.TantamodCommand;

public class GenerateGramophonePlaylistCmd extends TantamodCommand {
    public GenerateGramophonePlaylistCmd(Bot bot) {
        super(bot);
        this.name = "gramophone";
        this.help = "Generate a playlist from a gramophone thread.";
    }

    public void execute(CommandEvent event) {
        //
    }
}
