package com.jagrosh.jmusicbot.commands.jankbot

import com.google.gson.JsonParser
import com.google.maps.GeoApiContext
import com.google.maps.TimeZoneApi
import com.google.maps.model.LatLng
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jmusicbot.Bot
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.Point
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.nio.charset.StandardCharsets
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

class TimeCmd(bot: Bot, apiContext: GeoApiContext) : Command() {
    private val apiContext: GeoApiContext

    init {
        // this.bot = bot;
        name = "time"
        help =
            "Jankclock. Use " + bot.config.prefix + "time <location> to get the time in that location. You can also ask where I think that is by including the word `where` in the message."
        guildOnly = true
        this.apiContext = apiContext
    }

    public override fun execute(event: CommandEvent) {
        var args = event.args
        val min: Double
        var hr: Double
        var reply_txt = ""
        if (args.isEmpty()) {
            val time: LocalTime = LocalTime.now()
            min = time.minute.toDouble()
            hr = time.hour.toDouble()
            reply_txt += "Time in Jankland:"
        } else {
            if (args.startsWith("in ")) args = args.substring(3)
            var where = false
            if (args.contains("where")) {
                where = true
                args = args.replace("where", "")
            }
            val search = args
            try {
                val request = HttpRequest.newBuilder()
                    .GET()
                    .uri(
                        URI(
                            "https://www.mapquestapi.com/geocoding/v1/address?key=" + URLEncoder.encode(
                                _TimeKey.mapquest_key,
                                StandardCharsets.UTF_8.toString()
                            )
                                    + "&location=" + URLEncoder.encode(search, StandardCharsets.UTF_8.toString())
                        )
                    )
                    .build()
                val response = HttpClient.newBuilder().build().send(request, BodyHandlers.ofString())
                val json = response.body()
                val parser = JsonParser.parseString(json)
                val obj = parser.asJsonObject
                val results = obj.getAsJsonArray("results")
                val res1 = results[0].asJsonObject
                val location = res1.getAsJsonArray("locations")
                val latLng = location[0].asJsonObject.getAsJsonObject("latLng")
                val lat = latLng["lat"].asString
                val lng = latLng["lng"].asString
                val latlng = LatLng(lat.toDouble(), lng.toDouble())
                if (lat == "38.89037" && lng == "-77.03196") {
                    event.replyError("Couldn't find anywhere for \"$search\".")
                    return
                }
                val placeName = location[0].asJsonObject
                var placeLevel = 5
                var place = ""
                while(placeLevel >= 3 && place.isEmpty()) {
                    place = placeName["adminArea${placeLevel--}"].asString
                }
                if(place == "") place = search
                val tz = TimeZoneApi.getTimeZone(apiContext, latlng).await()
                val time_to_disp = ZonedDateTime.now(tz.toZoneId())
                hr = time_to_disp.hour.toDouble()
                min = time_to_disp.minute.toDouble()
                reply_txt += "Time in \"$place\":"
                if (where) reply_txt += "\n  https://maps.google.com/?q=$lat,$lng"
            } catch (e: Exception) {
                e.printStackTrace()
                event.replyError("There was an error getting the time.")
                return
            }
        }
        val mins_rotation = 360.0 - (min * 6.0)
        if (hr >= 12) hr -= 12
        val hrs_rotation = 360.0 - ((hr * 30) + (min * 0.5))
        val face = Imgcodecs.imread("/home/calluml/MusicBot/images/feis.png", Imgcodecs.IMREAD_UNCHANGED)
        val hour = Imgcodecs.imread("/home/calluml/MusicBot/images/ja2.png", Imgcodecs.IMREAD_UNCHANGED)
        val mins = Imgcodecs.imread("/home/calluml/MusicBot/images/ja2.png", Imgcodecs.IMREAD_UNCHANGED)
        val bg = Imgcodecs.imread("/home/calluml/MusicBot/images/face.png", Imgcodecs.IMREAD_UNCHANGED)
        val min_rot_mat = Imgproc.getRotationMatrix2D(Point(hour.cols() / 2.0, hour.cols() / 2.0), mins_rotation, 1.0)
        val hr_rot_mat = Imgproc.getRotationMatrix2D(Point(hour.cols() / 2.0, hour.cols() / 2.0), hrs_rotation, 1.0)
        Imgproc.warpAffine(mins, mins, min_rot_mat, mins.size())
        Imgproc.warpAffine(hour, hour, hr_rot_mat, hour.size())

        //Mat out = new Mat();
        overlayImage(bg, hour, face, mins)
        val mob = MatOfByte()
        Imgcodecs.imencode(".png", bg, mob)
        event.channel.sendMessage(reply_txt).addFile(mob.toArray(), "time.png").queue()
    }

    companion object {
        private fun overlayImage(vararg imagesMats: Mat) {
            val images: MutableList<Mat> = ArrayList(listOf(*imagesMats))
            val background = images[0]
            images.removeAt(0)
            for (y in 0 until background.rows()) {
                for (x in 0 until background.cols()) {
                    var opacity: Double
                    val finalPixelValue = background[y, x]
                    for (image in images) {
                        val pixelValue = image[y, x]
                        opacity = pixelValue[3] / 255.0
                        for (i in 0..2) {
                            finalPixelValue[i] = pixelValue[i] * opacity + finalPixelValue[i] * (1.0 - opacity)
                        }
                    }
                    background.put(y, x, *finalPixelValue)
                }
            }
        }
    }
}