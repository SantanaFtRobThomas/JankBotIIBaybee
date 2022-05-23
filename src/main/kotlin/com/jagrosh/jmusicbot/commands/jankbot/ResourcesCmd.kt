package com.jagrosh.jmusicbot.commands.jankbot

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import com.jagrosh.jmusicbot.Bot;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emote
import java.io.File;

class ResourcesCmd(bot: Bot) : Command() {
    val bot: Bot
    init {
        this.bot = bot
        this.name = "resources"
        this.help = "Get some resources for making Jankman content."
        this.guildOnly = true
    }

    override fun execute(event: CommandEvent) {
        val menu: SelectMenu = SelectMenu.create("MENU:RESOURCES")
                                .setPlaceholder("Select a resource")
                                .addOption("Jankman's Face", "MENU:RESOURCES:FACE")
                                .addOption("Jankman's Arm", "MENU:RESOURCES:ARM")
                                .addOption("Jankman's Left Eye", "MENU:RESOURCES:LEFTEYE")
                                .addOption("Jankman's Right Eye", "MENU:RESOURCES:RIGHTEYE")
                                .addOption("Jankman SVG", "MENU:RESOURCES:SVG")
                                .setMinValues(1)
                                .setMaxValues(5)
                                .build()
        val r = event.event.message.reply("Select some resources.").setActionRow(menu)
        event.event.message.addReaction("ok:978252580234866688")
        r.queue { message -> bot.jda.addEventListener(ResourceMenuListener(event.member.id, message, event.event.message))}
    }
}

class ResourceMenuListener(user_id: String, orig_msg: Message, orig_usr_msg: Message) : ListenerAdapter() {
    val user_id: String
    val orig_msg: Message
    val orig_usr_msg: Message
    init {
        this.user_id = user_id
        this.orig_msg = orig_msg
        this.orig_usr_msg = orig_usr_msg
    }
    
    override fun onSelectMenuInteraction(event: SelectMenuInteractionEvent) {
        val imagesMap = mapOf(
            "MENU:RESOURCES:FACE" to "/home/calluml/MusicBot/images/components/jankman_face.png",
            "MENU:RESOURCES:ARM" to "/home/calluml/MusicBot/images/components/jarm.png",
            "MENU:RESOURCES:LEFTEYE" to "/home/calluml/MusicBot/images/components/eyeleft.png",
            "MENU:RESOURCES:RIGHTEYE" to "/home/calluml/MusicBot/images/components/eyeright.png",
            "MENU:RESOURCES:SVG" to "/home/calluml/MusicBot/images/jankman.svg"
        )
        val imgs: MutableList<File> = mutableListOf<File>()
        if(event.user.id == user_id) {
            for (opt in event.selectedOptions) {
                imgs.add(File(imagesMap[opt.value]))
            }
            val r = event.replyFile(imgs[0]).setEphemeral(true)
            if(imgs.size > 1) {
                for(i in 1 until imgs.size) {
                    r.addFile(imgs[i]).queue()
                }
            } else {
                r.queue()
            }
            orig_msg.delete().queue()
            orig_usr_msg.addReaction("white_check_mark:978254300126019624")
        } else {
            event.reply("Sorry, this dropdown is only for ${orig_usr_msg.author.name}. Please request your own resources.").setEphemeral(true).queue()
        }
    }
} 