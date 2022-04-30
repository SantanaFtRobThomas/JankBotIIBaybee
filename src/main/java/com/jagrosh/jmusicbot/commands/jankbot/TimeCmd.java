package com.jagrosh.jmusicbot.commands.jankbot;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.GeoApiContext;
import com.google.maps.TimeZoneApi;
import com.google.maps.model.LatLng;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


public class TimeCmd extends Command {
    private GeoApiContext apiContext;
    public TimeCmd(Bot bot, GeoApiContext apiContext) {
        // this.bot = bot;
        this.name = "time";
        this.help = "Jankclock.";
        this.guildOnly = true;
        this.apiContext = apiContext;
    }

    public void execute(CommandEvent event) {
        String args = event.getArgs();
        double min, hr;
        String reply_txt = "";
        if (args.isEmpty()) {
            LocalTime time;
            time = LocalTime.now();
            min = time.getMinute();
            hr = time.getHour();
            reply_txt += "Time in Jankland:";
        } else {
            if(args.startsWith("in ")) args = args.substring(3);
            boolean where = false;
            if(args.contains("where")) {
                where = true;
                args = args.replace("where", "");
            }
            String search = args;
            
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI("https://open.mapquestapi.com/geocoding/v1/address?key=" + URLEncoder.encode(_TimeKey.mapquest_key, StandardCharsets.UTF_8.toString()) 
                                    + "&location=" + URLEncoder.encode(search, StandardCharsets.UTF_8.toString())))
                    .build();
                HttpResponse<String> response = HttpClient.newBuilder().build().send(request, BodyHandlers.ofString());
                String json = response.body();
                JsonElement parser = JsonParser.parseString(json);
                JsonObject obj = parser.getAsJsonObject();
                JsonArray results = obj.getAsJsonArray("results");
                JsonObject res1 = results.get(0).getAsJsonObject();
                JsonArray location = res1.getAsJsonArray("locations");
                JsonObject latLng = location.get(0).getAsJsonObject().getAsJsonObject("latLng");
                String lat = latLng.get("lat").getAsString();
                String lng = latLng.get("lng").getAsString();

                LatLng latlng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                if(lat.equals("39.78373") && lng.equals("-100.445882")) {
                    event.replyError("Couldn't find anywhere for \"" + search + "\".");
                    return;
                }

                JsonObject placeName = location.get(0).getAsJsonObject();
                String place = placeName.get("adminArea5").getAsString();

                TimeZone tz = TimeZoneApi.getTimeZone(apiContext, latlng).await();
                ZonedDateTime time_to_disp = ZonedDateTime.now(tz.toZoneId());
                hr = time_to_disp.getHour();
                min = time_to_disp.getMinute();
                reply_txt += "Time in \"" + place + "\":";
                if(where) reply_txt += "\n  https://maps.google.com/?q=" + lat + "," + lng;

            } catch (Exception e) {
                e.printStackTrace();
                event.replyError("There was an error getting the time.");
                return;
            }
        }
        
        double mins_rotation = 360.0 - (min * 6.0);
        double hrs_rotation = 360.0 - ((hr * 30) + (min * 0.5));

        Mat face = Imgcodecs.imread("/home/calluml/MusicBot/images/feis.png", Imgcodecs.IMREAD_UNCHANGED);
        Mat hour = Imgcodecs.imread("/home/calluml/MusicBot/images/ja2.png", Imgcodecs.IMREAD_UNCHANGED);
        Mat mins = Imgcodecs.imread("/home/calluml/MusicBot/images/ja2.png", Imgcodecs.IMREAD_UNCHANGED);
        Mat bg = Imgcodecs.imread("/home/calluml/MusicBot/images/face.png", Imgcodecs.IMREAD_UNCHANGED);

        Mat min_rot_mat = Imgproc.getRotationMatrix2D(new Point(hour.cols() / 2.0, hour.cols() / 2.0), mins_rotation,1);
        Mat hr_rot_mat = Imgproc.getRotationMatrix2D(new Point(hour.cols() / 2.0, hour.cols() / 2.0), hrs_rotation,1);

        Imgproc.warpAffine(mins, mins, min_rot_mat, mins.size());
        Imgproc.warpAffine(hour, hour, hr_rot_mat, hour.size());

        //Mat out = new Mat();

        overlayImage(bg, hour, face, mins);

        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", bg, mob);
        event.getChannel().sendMessage(reply_txt).addFile(mob.toArray(), "time.png").queue();
        
    }

    private static void overlayImage(Mat... imagesMats) {
        List<Mat> images = new ArrayList<Mat>(Arrays.asList(imagesMats));
        Mat background = images.get(0);
        images.remove(0);
        for (int y = 0; y < background.rows(); ++y) {
            for (int x = 0; x < background.cols(); ++x) {
                double opacity;
                double[] finalPixelValue = background.get(y, x);
                for (Mat image : images) {
                    double[] pixelValue = image.get(y, x);
                    opacity = pixelValue[3] / 255.0;
                    for(int i = 0; i < 3; i++) {
                        finalPixelValue[i] = (pixelValue[i] * opacity) + (finalPixelValue[i] * (1.0 - opacity));
                    }
                }
                background.put(y, x, finalPixelValue);
            }
        }
    }
}
