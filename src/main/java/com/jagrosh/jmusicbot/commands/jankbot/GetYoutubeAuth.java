package com.jagrosh.jmusicbot.commands.jankbot;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;

public class GetYoutubeAuth {
    
    private static final String CLIENT_SECRETS= "/home/calluml/MusicBot/sekrit.json";
    private static final Collection<String> SCOPES =
        Arrays.asList("https://www.googleapis.com/auth/youtube.readonly",
                        "https://www.googleapis.com/auth/youtube.force-ssl",
                        "https://www.googleapis.com/auth/youtube.upload",
                        "https://www.googleapis.com/auth/youtubepartner",
                        "https://www.googleapis.com/auth/youtubepartner-channel-audit",
                        "https://www.googleapis.com/auth/youtube.channel-memberships.creator",
                        "https://www.googleapis.com/auth/youtube.third-party-link.creator");

    private static final String APPLICATION_NAME = "JankBot playlist creator";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        FileDataStoreFactory fds = new FileDataStoreFactory(new File("/home/calluml/MusicBot/youtube-java-client-secrets"));
        InputStream in  = new FileInputStream(CLIENT_SECRETS);
        GoogleClientSecrets clientSecrets =
          GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.

        GoogleAuthorizationCodeFlow flow =
            new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(fds)
            .build();
        Credential credential =
            new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }
}