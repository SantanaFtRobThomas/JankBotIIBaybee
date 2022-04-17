package com.jagrosh.jmusicbot.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jmusicbot.Bot;



public abstract class TantamodCommand extends Command {
    protected final Bot bot;
    public boolean hidden = false;

    public TantamodCommand(Bot bot) {
        this.bot = bot;
        this.category = new Category("Tantamod", event -> {
            if(event.getAuthor().getId().equals(event.getClient().getOwnerId()))
                return true;
            if(event.getGuild()==null)
                return true;
            return event.getMember().getRoles().contains(event.getGuild().getRoleById(736622853797052519L));
        });
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    
    public boolean isHidden() {
        return hidden;
    }

    public long strToMs(String time){
        if (!time.matches("^\\d+[hmHM]")) {
            return -1;
        }
        long ms = 0;
        Pattern r = Pattern.compile("(\\d+)[hH]");
        Matcher m = r.matcher(time);
        if(m.find()) {
            long hours = Long.parseLong(m.group(1));
            ms += hours * 60 * 60 * 1000;
        }
        r = Pattern.compile("(\\d+)[mM]");
        m = r.matcher(time);
        if(m.find()) {
            long minutes = Long.parseLong(m.group(1));
            ms += minutes * 60 * 1000;
        }
        return ms;
    }

    public String msToStrings(long ms){
        int days, hours, mins = 0;
        days = (int) (ms / 86400000);
        ms -= days * 86400000;
        hours = (int) (ms / 3600000);
        ms -= hours * 3600000;
        mins = (int) (ms / 60000);
        return (days > 0 ? days + (days > 1 ? "days, " : "day, "): "") + hours + (hours != 1 ? " hours and " : " hour and ") + mins + (mins != 1 ? " minutes." : "minute.");
    }
}
