package com.jagrosh.jmusicbot.commands.tantamod;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.TantamodCommand;
import com.jagrosh.jmusicbot.commands.jankbot.GetYoutubeAuth;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.ThreadChannel;

public class GenerateGramophonePlaylistCmd extends TantamodCommand {
    final String YOUTUBE_PATTERN = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?][A-Za-z0-9_-]{10,}";

    public GenerateGramophonePlaylistCmd(Bot bot) {
        super(bot);
        this.name = "gramophoneplaylist";
        this.help = "Generate a playlist from a gramophone thread. `j!gramophoneplaylist <thread_id>`";
    }

    public void execute(CommandEvent event) {
        if (event.getArgs().length() != 0) {
            try {
                ThreadChannel ch = event.getGuild().getThreadChannelById(event.getArgs());
                if (ch != null) {
                    if (ch.getMessageCount() > 100) {
                        event.reply(
                                "The thread has more than 100 messages. For now, I'll only look at the first 100 but this will change if it becomes a problem.");
                    }
                    ch.getHistoryFromBeginning(100).queue(msgs -> {
                        Pattern pattern = Pattern.compile(YOUTUBE_PATTERN);
                        List<Message> all_msgs = msgs.getRetrievedHistory();
                        List<Message> rev_msg = new ArrayList<Message>();
                        List<String> urls = new ArrayList<String>();
                        for (int i = all_msgs.size() - 1; i >= 0; i--) rev_msg.add(all_msgs.get(i)); //Collections.reverse() throws a weird error 
                        all_msgs = rev_msg;
                        String reply = "```\n";
                        for (Message m : all_msgs) {
                            Matcher matcher = pattern.matcher(m.getContentRaw());
                            while (matcher.find()) {
                                urls.add(matcher.group(0));
                                if (reply.length() > 1950) {
                                    reply += "```";
                                    // event.reply(reply);
                                    reply = "```\nCONTINUED:\n";
                                }
                                reply += m.getAuthor().getName() + ": https://www.youtube.com/watch?v="
                                        + matcher.group(0) + "\n";
                            }
                        }
                        if (urls.size() == 0) {
                            event.replyError("No youtube links found in thread.");
                            return;
                        } else {
                            reply += "```";
                        }
                        // event.reply(reply);
                        try {
                            YouTube svc = GetYoutubeAuth.getService();
                            YouTube.Playlists.Insert playlist = svc.playlists().insert(List.of("snippet", "status"),
                                    new com.google.api.services.youtube.model.Playlist()
                                            .setSnippet(new com.google.api.services.youtube.model.PlaylistSnippet()
                                                    .setTitle(ch.getName())
                                                    .setDescription("Playlist generated from gramophone thread "
                                                            + ch.getName() + "."))
                                            .setStatus(new com.google.api.services.youtube.model.PlaylistStatus()
                                                    .setPrivacyStatus("public")));
                            Playlist p = playlist.execute();
                            String playlist_id = p.getId();
                            System.out.println("Created playlist with id " + playlist_id);
                            event.reply("Creating playlist. Please wait...");
                            long ctr = 0L;
                            urls.remove(0); // pop the first entry off because it's probably the meme example
                            for (String url : urls) {
                                PlaylistItem playlistItem = new PlaylistItem();
                                PlaylistItemSnippet snippet = new PlaylistItemSnippet();
                                snippet.setPlaylistId(playlist_id);
                                snippet.setPosition(ctr++);
                                snippet.setResourceId(new com.google.api.services.youtube.model.ResourceId()
                                        .setKind("youtube#video").setVideoId(url));
                                playlistItem.setSnippet(snippet);
                                YouTube.PlaylistItems.Insert rq = svc.playlistItems().insert(List.of("snippet"),
                                        playlistItem);
                                PlaylistItem resp = rq.execute();
                                System.out.println("Added video " + resp.getSnippet().getResourceId().getVideoId()
                                        + " to playlist " + playlist_id);
                            }
                            event.reply("Playlist created. https://www.youtube.com/playlist?list=" + playlist_id);
                        } catch (Exception e) {
                            event.replyError(
                                    "Something went wrong while trying to generate the playlist. <@204969832326627330> ```\n"
                                            + e.toString() + "\n```");
                            e.printStackTrace();
                        }

                    });
                } else {
                    event.reply("Could not find thread with id " + event.getArgs());
                }
            } catch (NumberFormatException e) {
                event.reply("This doesn't look like a valid thread ID.");
            }
        }
    }
}
