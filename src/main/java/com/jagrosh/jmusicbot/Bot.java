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
package com.jagrosh.jmusicbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.google.maps.GeoApiContext;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jmusicbot.audio.AloneInVoiceHandler;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.NowplayingHandler;
import com.jagrosh.jmusicbot.audio.PlayerManager;
import com.jagrosh.jmusicbot.gui.GUI;
import com.jagrosh.jmusicbot.playlist.PlaylistLoader;
import com.jagrosh.jmusicbot.settings.SettingsManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class Bot {
    private final EventWaiter waiter;
    private final ScheduledExecutorService threadpool;
    private final BotConfig config;
    private final SettingsManager settings;
    private final PlayerManager players;
    private final PlaylistLoader playlists;
    private final NowplayingHandler nowplaying;
    private final AloneInVoiceHandler aloneInVoiceHandler;

    private GeoApiContext geoApiContext;

    private boolean shuttingDown = false;
    private JDA jda;
    private GUI gui;
    private CommandClient cc;
    private HashMap<Guild, Timer> gram_dj_timers = new HashMap<Guild, Timer>();
    private List<Long> gramophone_guilds = new ArrayList<Long>();
    private List<Long> dj_guilds = new ArrayList<Long>();

    public Bot(EventWaiter waiter, BotConfig config, SettingsManager settings, GeoApiContext geoApiContext) {
        this.waiter = waiter;
        this.config = config;
        this.settings = settings;
        this.playlists = new PlaylistLoader(config);
        this.threadpool = Executors.newSingleThreadScheduledExecutor();
        this.players = new PlayerManager(this);
        this.players.init();
        this.nowplaying = new NowplayingHandler(this);
        this.nowplaying.init();
        this.aloneInVoiceHandler = new AloneInVoiceHandler(this);
        this.aloneInVoiceHandler.init();
        this.geoApiContext = geoApiContext;
    }

    public GeoApiContext getGeoApiContext() {
        return geoApiContext;
    }

    public BotConfig getConfig() {
        return config;
    }

    public SettingsManager getSettingsManager() {
        return settings;
    }

    public EventWaiter getWaiter() {
        return waiter;
    }

    public ScheduledExecutorService getThreadpool() {
        return threadpool;
    }

    public PlayerManager getPlayerManager() {
        return players;
    }

    public PlaylistLoader getPlaylistLoader() {
        return playlists;
    }

    public NowplayingHandler getNowplayingHandler() {
        return nowplaying;
    }

    public AloneInVoiceHandler getAloneInVoiceHandler() {
        return aloneInVoiceHandler;
    }

    public JDA getJDA() {
        return jda;
    }

    public void closeAudioConnection(long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null)
            threadpool.submit(() -> guild.getAudioManager().closeAudioConnection());
    }

    public void resetGame() {
        Activity game = config.getGame() == null || config.getGame().getName().equalsIgnoreCase("none") ? null
                : config.getGame();
        if (!Objects.equals(jda.getPresence().getActivity(), game))
            jda.getPresence().setActivity(game);
    }

    public void shutdown() {
        if (shuttingDown)
            return;
        shuttingDown = true;
        threadpool.shutdownNow();
        if (jda.getStatus() != JDA.Status.SHUTTING_DOWN) {
            jda.getGuilds().stream().forEach(g -> {
                g.getAudioManager().closeAudioConnection();
                AudioHandler ah = (AudioHandler) g.getAudioManager().getSendingHandler();
                if (ah != null) {
                    ah.stopAndClear();
                    ah.getPlayer().destroy();
                    nowplaying.updateTopic(g.getIdLong(), ah, true);
                }
            });
            jda.shutdown();
        }
        this.geoApiContext.shutdown();
        if (gui != null)
            gui.dispose();
        System.exit(0);
    }

    public void setJDA(JDA jda) {
        this.jda = jda;
    }

    public void setGUI(GUI gui) {
        this.gui = gui;
    }

    public void setDJMode(Guild guild, boolean val, Timer timer) {
        if(val){
            if(!dj_guilds.contains(guild.getIdLong()))
                dj_guilds.add(guild.getIdLong());
            if(timer != null)
                gram_dj_timers.put(guild, timer);
        } else {
            if(dj_guilds.contains(guild.getIdLong()))
                dj_guilds.remove(guild.getIdLong());
            if(gram_dj_timers.containsKey(guild)){
                gram_dj_timers.get(guild).cancel();
                gram_dj_timers.remove(guild);
            }
                
        }
    }

    public boolean getDJMode(Guild guild) {
        return this.dj_guilds.contains(guild.getIdLong());
    }

    public void setCommandClient(CommandClient cc) {
        this.cc = cc;
    }

    public CommandClient getCommandClient() {
        return this.cc;
    }

    public void setGramophoneMode(Guild guild, boolean mode, Timer timer) {
        if(mode){
            if (!gramophone_guilds.contains(guild.getIdLong()))
                gramophone_guilds.add(guild.getIdLong());
            if (timer != null){
                gram_dj_timers.put(guild, timer);
            }
        }
        else{
            if(gramophone_guilds.contains(guild.getIdLong()))
                gramophone_guilds.remove(guild.getIdLong());
            if (gram_dj_timers.containsKey(guild)){
                gram_dj_timers.get(guild).cancel();
                gram_dj_timers.remove(guild);
            }
        }
    }

    public boolean getGramophoneMode(Guild guild){
        return this.gramophone_guilds.contains(guild.getIdLong());
    }

    public void addGDJTimer(Guild guild, Timer timer) {
        gram_dj_timers.put(guild, timer);
    }

    public void removeGDJTimer(Guild guild) {
        gram_dj_timers.remove(guild);
    }

    public void cancelGDJTimer(Guild guild) {
        Timer timer = gram_dj_timers.get(guild);
        if (timer != null)
            timer.cancel();
    }

    public boolean GDJTimerExists(Guild guild){
        return gram_dj_timers.containsKey(guild);
    }

}
