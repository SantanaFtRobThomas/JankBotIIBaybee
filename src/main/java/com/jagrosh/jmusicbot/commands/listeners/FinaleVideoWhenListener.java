package com.jagrosh.jmusicbot.commands.listeners;

import java.io.File;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class FinaleVideoWhenListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if((event.getMessage().getContentRaw().toLowerCase().contains("when")
                && event.getMessage().getContentRaw().toLowerCase().contains("finale video"))
                && event.getMessage().getContentRaw().split(" ").length <= 16
                && !event.getMessage().getAuthor().isBot()) {
            event.getMessage().reply("https://youtu.be/BcSNmPjS7A0").queue();
        }
    }
}
