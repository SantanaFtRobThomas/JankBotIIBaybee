package com.jagrosh.jmusicbot.commands.tantamod;

import java.util.Timer;
import java.util.TimerTask;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.TantamodCommand;

public class SetGramophoneCmd extends TantamodCommand {
    private final long default_time = 130L * 60000L;
    public SetGramophoneCmd(Bot bot) {
        super(bot);
        this.name = "gramophone";
        this.help = "Enable/disable gramophone mode (mod only). Specify a time in the form of `<time><unit>` (e.g. `5m` or `2h10m`) to set the duration of gramophone mode."
                + " If no time is specified, the default duration is 2 hours and 10 minutes.";
    }

    @Override
    public void execute(CommandEvent event) {
        String[] args = event.getArgs().toLowerCase().trim().split(" ");
        switch (args[0]) {
            case "on":
                if (bot.getGramophoneMode(event.getGuild())) {
                    event.reply("Gramophone mode is already enabled.");
                    return;
                }
                if (bot.getDJMode(event.getGuild())) {
                    event.reply("DJ mode is already enabled. Disabling and changing to gramophone mode.");
                    bot.setDJMode(event.getGuild(), false, null);
                }
                long time;
                if (args.length > 1) {
                    time = strToMs(args[1]);
                    if (time == -1) {
                        event.replyError(
                                "Invalid time specified. Specify in the form `<time><unit>` (e.g. `5m` or `2h10m`)");
                        return;
                    }
                } else {
                    time = default_time;
                }
                event.reply("Gramophone Mode On. Auto-off in " + msToStrings(time));
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        bot.setGramophoneMode(event.getGuild(), false, null);
                        event.reply("Gramophone Mode has been automatically switched OFF.");
                    }
                }, (time));
                bot.setGramophoneMode(event.getGuild(), true, t);
                break;
            case "off":
                if (!bot.getGramophoneMode(event.getGuild())) {
                    event.reply("Gramophone mode is not enabled.");
                    return;
                }
                bot.setGramophoneMode(event.getGuild(), false, null);
                event.reply("Gramophone Mode Off.");
                break;
            case "":
                event.reply("Gramophone Mode is currently " + (this.bot.getGramophoneMode(event.getGuild()) ? "ON" : "OFF") + ".");
                break;
            default:
                event.reply("Didn't understand. j!gramophone <on|off> <time>");
        }
    }

}
