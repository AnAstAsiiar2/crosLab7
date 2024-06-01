package data

import data.model.Hour
import data.model.Weather
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import utils.Constants
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.HashMap

object WeatherApiClient {
    val format = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(CIO){
        install(ContentNegotiation){
            json(format)
        }
    }
    private val pattern = "yyyy-MM-dd HH:mm"
    private val dateFormat = SimpleDateFormat(pattern)
    private val locationsTwoWeeksAgoInfo: HashMap<String, List<Weather>> = HashMap<String, List<Weather>>()
    private val cur = LocalDate.now()
    private val curString = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(cur)

    suspend fun getWeather(location: String, dt: String = curString, flagAddToHistory: Boolean = true): Weather {
        val loc = location.replace(" ", "%20")
        //println(loc)
        val url = "http://api.weatherapi.com/v1/history.json?key=${Constants.API_KEY}&q=${loc}&dt=${dt}"
        println(url)
        val content = client.get(url)
        if (content.status == HttpStatusCode.BadRequest)
            return Weather(null, null)
        val weather: Weather = content.body()
        val hours = weather.forecast?.forecastday?.get(0)?.hour
        val hoursToRemove = mutableListOf<Hour>()
        if (hours != null) {
            for (hour in hours){
                val h = dateFormat.parse(hour.time).hours
                if (h % 4 != 0)
                    hoursToRemove.add(hour)
            }
            hours.removeAll(hoursToRemove)
        }
        if (flagAddToHistory)
            HistoryTracker.addLocation(weather)
        return weather
    }
    suspend fun getWeatherTwoWeeksAgo(locationName: String):List<Weather>{
        if (locationsTwoWeeksAgoInfo.containsKey(locationName))
            return locationsTwoWeeksAgoInfo.get(locationName)!!
        val res: MutableList<Weather> = mutableListOf()
        var dateTwoWeeksAgo = cur.minusWeeks(2).plusDays(1)
        while (dateTwoWeeksAgo.isBefore(cur)){
            val dString = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dateTwoWeeksAgo)
            val w = getWeather(locationName, dString, false)
            res.add(w)
            dateTwoWeeksAgo = dateTwoWeeksAgo.plusDays(1)
        }
        for (w in HistoryTracker.getActualLocationsInfo()){
            if (w.location?.name!!  == locationName){
                res.add(w)
                break
            }
        }
        locationsTwoWeeksAgoInfo.put(locationName, res)
        return res
    }
}