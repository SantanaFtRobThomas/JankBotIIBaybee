package com.jagrosh.jmusicbot.commands.listeners;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.List;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OtherCommandListener extends ListenerAdapter {

    Bot bot;
    CommandClient cc;

    public OtherCommandListener(CommandClient cc, Bot bot){
        super();
        this.bot = bot;
        this.cc = cc;
    }

    private String removeStringOf(String src_string, Character... chars){
        List<Character> char_list = Arrays.asList(chars);
        StringBuilder str = new StringBuilder();
        for(CharacterIterator it = new StringCharacterIterator(src_string); it.current() != CharacterIterator.DONE; it.next()){
            if(char_list.indexOf(it.current()) == -1){
                str.append(it.current());
            }
        }
        return str.toString();
    }
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.getMessage().getContentRaw().startsWith("j!") && !event.getMessage().getContentRaw().startsWith("]")){
            String msg = event.getMessage().getContentRaw();
            List<String> split_msg;
            List<String> lower_split_msg;
            for(String wake_word : new String[] {"<@" + bot.getJDA().getSelfUser().getId() + ">" , "<@!" + bot.getJDA().getSelfUser().getId() + ">", "jankbot", "janky jeff"}){
                if(msg.toLowerCase().indexOf(wake_word) != -1){
                    String rest = msg.substring(msg.toLowerCase().indexOf(wake_word) + wake_word.length());
                    split_msg = Arrays.asList(rest.split(" "));
                    lower_split_msg = Arrays.asList(rest.toLowerCase().split(" "));
                    for (int i = 0; i < lower_split_msg.size(); i++) {
                        String s = lower_split_msg.get(i);
                        lower_split_msg.set(i, removeStringOf(s, '?', ',', '.', '!'));
                    }
                    for(Command command : cc.getCommands()) {
                        if(lower_split_msg.indexOf(command.getName()) != -1 || this.isInAliases(lower_split_msg, command.getAliases())) {
                            split_msg = split_msg.subList(lower_split_msg.indexOf(command.getName())+1, split_msg.size());
                            String argz = String.join(" ", split_msg);
                            MessageReceivedEvent mre = new MessageReceivedEvent(bot.getJDA(), event.getResponseNumber(), event.getMessage());
                            CommandEvent ev = new CommandEvent(mre, wake_word, argz, cc);
                            command.run(ev);
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean isInAliases(List<String> msg, String[] aliases){
        for(String s : aliases) if(msg.indexOf(s) != -1) return true;
        return false;
    }
    
}
