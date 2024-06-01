package data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import data.model.Weather
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar

object HistoryTracker {
    private val f = "locations.json"
    private var locations: MutableList<Weather> = mutableListOf()
    private var actualLocationInfo: MutableList<Weather> = mutableListOf()
    private val gson: Gson = Gson()
    private val pattern = "yyyy-MM-dd HH:mm"
    private val dateFormat = SimpleDateFormat(pattern)
    val cities = arrayOf("Skole", "Chervonograd", "Sambir", "Lviv", "Truskavets", "Zolochiv", "Zhovkva", "Sokal", "Boryslav", "Brody")
    fun addLocation(location: Weather){
        var flag = false
        for (item in locations){
            if (item.location?.name?.let { location.location?.name?.compareTo(it) } == 0){
                flag = true
                val d1 = dateFormat.parse(item.location?.localtime).time / 86400000L
                val d2 = dateFormat.parse(location.location?.localtime).time / 86400000L
                if (d1 != d2) {
                    val index = locations.indexOf(item)
                    locations.set(index, location)
                }
                break
            }
        }
        if (!flag)
            locations.add(location)
        saveLocations()
    }
    private fun saveLocations() {
        try {
            FileWriter(f).use { writer -> gson.toJson(locations, writer) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadLocations() {
        try {
            FileReader(f).use { reader ->
                val listType = object : TypeToken<List<Weather>>() {}.type
                locations = gson.fromJson(reader, listType)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    suspend fun getCities(){
        for (city in cities){
            WeatherApiClient.getWeather(city)
        }
    }
    fun getLocations(): MutableList<Weather>{
        if (locations.size <= 10)
            return locations
        val res = ArrayList<Weather>()
        for (i in locations.size - 10 until locations.size)
            res.add(locations[i])
        return res
    }
    suspend fun getActualLocationsInfo(): List<Weather>{
        loadLocations()
        actualLocationInfo = getLocations()
        val cur = Calendar.getInstance().time
        val curTime = cur.time / 86400000L
        for (i in 0 until actualLocationInfo.size){
            val d = dateFormat.parse(actualLocationInfo[i].location?.localtime).time / 86400000L
            if (curTime != d){
//                println(Calendar.getInstance().time.time)
//                println(dateFormat.parse(actualLocationInfo[i].location?.localtime).time)
//                println(actualLocationInfo[i].location?.name!!)
                val curW = WeatherApiClient.getWeather(actualLocationInfo[i].location?.name!!, flagAddToHistory = false)
                actualLocationInfo.set(i, curW)
            }
        }
        locations = actualLocationInfo
        saveLocations()
        return actualLocationInfo
    }
}