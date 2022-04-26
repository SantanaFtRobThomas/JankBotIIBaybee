/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.tantamod;

import java.util.Timer;
import java.util.TimerTask;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.TantamodCommand;


/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetDJCmd extends TantamodCommand
{
    private final long default_time = 130L * 60000L;
    public SetDJCmd(Bot bot)
    {
        super(bot);
        this.name = "dj";
        this.help = "Enable/disable DJ mode (mod only). Specify a time in the form of `[#]m[#]s` to set the DJ mode duration."
                    + "If no time is specified, the DJ mode will time out after 2 hours and 10 minutes.";
    }
    

    @Override
    public void execute(CommandEvent event) 
    {
        String[] args = event.getArgs().toLowerCase().trim().split(" ");
        switch (args[0]) {
            case "on":
                if (bot.getDJMode(event.getGuild())) {
                    event.reply("DJ mode is already enabled.");
                    return;
                }
                long time;
                if (args.length > 1) {
                    time = strToMs(args[1]);
                    if (time == -1) {
                        event.replyError(
                                "Invalid time specified. Specify in the form `<time><unit>` (e.g. `5m` or `2h10m`)");
                        return;
                    } else if (time == -2) {
                        event.replyError("Don't be silly.");
                        return;
                    }
                } else {
                    time = default_time;
                }
                event.reply("DJ Mode On. Auto-off in " + msToStrings(time));
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        bot.setDJMode(event.getGuild(), false, null);
                        event.reply("DJ Mode has been automatically switched OFF.");
                    }
                }, (time));
                bot.setDJMode(event.getGuild(), true, t);
                break;
            case "off":
                if (!bot.getDJMode(event.getGuild())) {
                    event.reply("DJ mode is not enabled.");
                    return;
                }
                bot.setDJMode(event.getGuild(), false, null);
                event.reply("DJ Mode Off.");
                break;
            case "":
                event.reply("DJ Mode is currently " + (this.bot.getDJMode(event.getGuild()) ? "ON" : "OFF") + ".");
                break;
            default:
                event.reply("Didn't understand. j!dj <on|off> <time>");
        }
    }

    

    
}
