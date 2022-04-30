/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands;

import java.util.List;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.settings.Settings;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public abstract class MusicCommand extends Command {
    protected final Bot bot;
    protected boolean bePlaying;
    protected boolean beListening;
    protected boolean DJModeAllowed;
    protected boolean GramophoneModeAllowed;

    public MusicCommand(Bot bot) {
        this.bot = bot;
        this.guildOnly = true;
        this.category = new Category("Music");
        this.DJModeAllowed = false;
        this.GramophoneModeAllowed = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!checkPermission(event)) {
            if (this.bot.getDJMode(event.getGuild()) && !this.DJModeAllowed) {
                event.reply("Sorry, DJ Mode is on!");
                return;
            }

            if (this.bot.getGramophoneMode(event.getGuild()) && !this.GramophoneModeAllowed) {
                event.reply("Sorry, Gramophone Mode is on!");
                return;
            }
        }

        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        TextChannel tchannel = settings.getTextChannel(event.getGuild());
        if (tchannel != null && !event.getTextChannel().equals(tchannel)) {
            try {
                event.getMessage().delete().queue();
            } catch (PermissionException ignore) {
            }
            event.replyInDm(event.getClient().getError() + " You can only use that command in "
                    + tchannel.getAsMention() + "!");
            return;
        }
        bot.getPlayerManager().setUpHandler(event.getGuild()); // no point constantly checking for this later
        if (bePlaying && !((AudioHandler) event.getGuild().getAudioManager().getSendingHandler())
                .isMusicPlaying(event.getJDA())) {
            event.reply(event.getClient().getError() + " There must be music playing to use that!");
            return;
        }
        if (beListening) {
            AudioChannel current = event.getGuild().getSelfMember().getVoiceState().getChannel();
            if (current == null)
                current = settings.getVoiceChannel(event.getGuild());
            GuildVoiceState userState = event.getMember().getVoiceState();
            if (!userState.inAudioChannel() || userState.isDeafened()
                    || (current != null && !userState.getChannel().equals(current))) {
                event.replyError("You must be listening in "
                        + (current == null ? "a voice channel" : current.getAsMention()) + " to use that!");
                return;
            }

            AudioChannel afkChannel = userState.getGuild().getAfkChannel();
            if (afkChannel != null && afkChannel.equals(userState.getChannel())) {
                event.replyError("You cannot use that command in an AFK channel!");
                return;
            }

            if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
                try {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                } catch (PermissionException ex) {
                    event.reply(event.getClient().getError() + " I am unable to connect to "
                            + userState.getChannel().getAsMention() + "!");
                    return;
                }
            }
        }
        doCommand(event);

    }

    public abstract void doCommand(CommandEvent event);

    public static boolean checkPermission(CommandEvent event) {
        if (event.getAuthor().getId().equals(event.getClient().getOwnerId()))
            return true;
        if (event.getMember().hasPermission(Permission.MANAGE_SERVER))
            return true;

        List<Role> user_roles = event.getMember().getRoles();

        for (Role r : user_roles) {
            if (r.getIdLong() == 736622853797052519L)
                return true;
        }

        return false;
    }
}
