package com.jagrosh.jmusicbot.commands.tantamod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.TantamodCommand;

import net.dv8tion.jda.api.entities.TextChannel;

public class TalkCmd extends TantamodCommand {
    public TalkCmd(Bot bot) {
        super(bot);
        this.name = "say";
        this.help = "Make the bot say something, or reply. `" + bot.getConfig().getPrefix() + "say #voice_channel [ID of message to reply] <message>`";
        this.hidden = true;
    }

    protected void execute(CommandEvent event) 
    { 
        String[] spl = event.getArgs().split(" ");
        TextChannel channel_to_send;
        if(spl[0].matches("^<#\\d+>")){
            channel_to_send = event.getGuild().getTextChannelById(spl[0].replace("<#", "").replace(">", ""));
        }
        else if (spl[0].matches("^\\d+")){
            try {
                channel_to_send = event.getGuild().getTextChannelById(spl[0]);
            }
            catch (Exception e){
                event.replyError("Invalid channel ID.");
                return;
            }
        }
        else {
            event.replyError("Invalid channel ID.");
            return;
        }

        List<String> spll = new ArrayList<String>(Arrays.asList(spl));
        spll.remove(0);

        if(spll.get(0).matches("^\\d+")){
            try {
                String id = spll.get(0);
                spll.remove(0);
                channel_to_send.retrieveMessageById(id).queue((message) -> {
                    message.reply(String.join(" ", spll)).queue();
                });
            } catch (Exception e){
                event.replyError("Could not find message.");
                return;
            }
        } else {
            channel_to_send.sendMessage(String.join(" ", spll)).queue();
        }
    }
}
