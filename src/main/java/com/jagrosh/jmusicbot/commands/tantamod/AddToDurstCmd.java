package com.jagrosh.jmusicbot.commands.tantamod;

import java.io.File;
import java.util.UUID;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.TantamodCommand;

import net.dv8tion.jda.api.entities.Message.Attachment;

public class AddToDurstCmd extends TantamodCommand {
    public AddToDurstCmd (Bot bot) {
        super(bot);
        this.name = "adddurst";
        this.help = "adds a picture to the durstdex";
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getMessage().getAttachments().size() == 0) {
            event.reply("You need to attach a picture to this command!");
            return;
        } 
        Attachment attachment = event.getMessage().getAttachments().get(0);
        File f = new File("/home/calluml/MusicBot/durst/" + attachment.getFileName());
        if(f.exists()) {
            attachment.downloadToFile("/home/calluml/MusicBot/durst/" + UUID.randomUUID().toString() + "." + attachment.getFileExtension());
            return;
        }
        attachment.downloadToFile("/home/calluml/MusicBot/durst/" + attachment.getFileName());
        event.reply("Added " + attachment.getFileName() + " to the durstdex!");
    }
}
