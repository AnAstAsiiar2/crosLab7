package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import data.HistoryTracker
import data.WeatherApiClient
import data.model.Hour
import data.model.Weather
import kotlinx.coroutines.launch
import java.net.URL
import java.text.SimpleDateFormat
import javax.imageio.ImageIO


@Composable
fun mainView() {
    HistoryTracker.loadLocations()
    var flag by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var historyFlag by remember { mutableStateOf(true) }
    var errorMSG by remember { mutableStateOf("") }
    fun String.availableSymbols(): Boolean = all { it.isLetterOrDigit() || it == ' ' || it == ',' || it == '.'}
    Row(
        modifier = Modifier.fillMaxSize(1f)
    ) {
//        Column(
//            modifier = Modifier.padding(20.dp)
//                .fillMaxHeight(1f)
//                .width(200.dp)
//        ) {
//            OutlinedTextField(
//                value = textValue,
//                onValueChange = {
//                    textValue = it
//                    historyFlag = false
//                                },
//                label = { Text("Enter location") },
//                modifier = Modifier.fillMaxWidth(1f)
//            )
//            Button(
//                shape = RoundedCornerShape(5.dp),
//                onClick =
//                {
//                    location = textValue
//                    if(!location.availableSymbols() || location.isBlank()){
//                        errorMSG = "You have to enter location."
//                        historyFlag = true
//                    }
//                    else {
//                        errorMSG = ""
//                        flag = true
//                        historyFlag = true
//                    }
//                },
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            ) {
//                Text(
//                    textAlign = TextAlign.Center,
//                    text = "Show weather"
//                )
//            }
//            Text(errorMSG, color = Color.Red)
//        }
//        Divider(
//            color = Color.Black,
//            modifier = Modifier.fillMaxHeight().width(1.dp)
//        )
        Column(modifier = Modifier.padding(20.dp)
            .fillMaxHeight(1f).fillMaxWidth(1f)
        ) {

//            if (flag)
//                displayCurrentWeather(location)
//            Spacer(Modifier.height(20.dp).fillMaxWidth(1f))
            if (historyFlag)
                displayHistory()
            else{
                Text("European Union")
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(if (HistoryTracker.getLocations().size > 0) "Loading..." else "No data...")
                }
            }
        }
    }
}

@Composable
fun displayCurrentWeather(location: String){
    val scope = rememberCoroutineScope()
    var weatherResponse by remember { mutableStateOf(Weather(null, null)) }
    var flag by remember { mutableStateOf(false) }
    LaunchedEffect(location) {
        scope.launch {
            try {
                val w = WeatherApiClient.getWeather(location)
                weatherResponse = w
                flag = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    Text("Current weather in ${if (weatherResponse.location == null) location else weatherResponse.location!!.name}")
    Spacer(Modifier.height(10.dp).fillMaxWidth(1f))

    if (weatherResponse.location == null && flag)
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("Not found.", color = Color.Red)
        }
    else{
        if (flag) {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(1),
                contentPadding = PaddingValues(10.dp),
                modifier = Modifier.height(130.dp)
            ){
                items(weatherResponse.forecast!!.forecastday[0].hour.toList()){
                    weatherView(it)
                }
            }
        }
        else
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Loading...")
            }
    }
}

@Composable
fun displayHistory(){
    Text("European Union")
    Spacer(Modifier.height(10.dp).fillMaxWidth(1f))
    var historyWeather by remember { mutableStateOf(emptyList<Weather>()) }
    var twoWeeksWeather by remember { mutableStateOf(emptyList<Weather>()) }
    var dates by remember { mutableStateOf(emptyList<String>()) }
    var loading by remember { mutableStateOf(true) }
    var getTwoWeeksLoading by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf(0) }
    var flag by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    LaunchedEffect("history"){
        scope.launch {
            historyWeather = HistoryTracker.getActualLocationsInfo()
            loading = false
        }
    }

    if (HistoryTracker.getLocations().size == 0)
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("No data...")
        }
    else{
        if (loading){
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Loading...")
            }
        }else{
            val locations = mutableListOf<String>()
            for (w in historyWeather)
                locations.add(w.location!!.name)
            var buttonNames by remember { mutableStateOf(locations.toList()) }
            var dataWeather by remember { mutableStateOf(historyWeather) }
            var bottomButtonText by remember { mutableStateOf("Show 2 week weather") }
            var locationName by remember { mutableStateOf(locations[0]) }
            var oldSelected = 0
            if (getTwoWeeksLoading) {
                loading = true
                LaunchedEffect("two week weather") {
                    scope.launch {
                        twoWeeksWeather = WeatherApiClient.getWeatherTwoWeeksAgo(locationName)
                        var datesString = mutableListOf<String>()
                        for (w in twoWeeksWeather) {
                            val pattern = "yyyy-MM-dd HH:mm"
                            val dateFormat = SimpleDateFormat(pattern)
                            val d = dateFormat.parse(w.forecast!!.forecastday[0].hour[0].time)
                            dateFormat.applyPattern("dd MMMM")
                            datesString.add(dateFormat.format(d))
                        }
                        dates = datesString
                        buttonNames = dates
                        flag = false
                        getTwoWeeksLoading = false
                        selectedLocation = 0
                        loading = false
                    }
                }
            }
            if (flag){
                buttonNames = locations
                dataWeather = historyWeather
                bottomButtonText = "Show 2 week weather"
                locationName = locations[selectedLocation]
            }
            if (!flag){
                buttonNames = dates
                dataWeather = twoWeeksWeather
                locationName = locations[oldSelected]
                bottomButtonText = "Back"
            }
            LazyHorizontalGrid(
                rows = GridCells.Fixed(1),
                contentPadding = PaddingValues(20.dp),
                modifier = Modifier.height(75.dp)
            ) {
                items(buttonNames) {
                    Button(
                        onClick = {
                            selectedLocation = buttonNames.indexOf(it)
                            flag = flag
                                  },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedLocation == buttonNames.indexOf(it))
                                Color.LightGray else Color.Transparent
                        ), modifier = Modifier.wrapContentHeight()
                    ) {
                        Text(it)
                    }
                }
            }
            Spacer(Modifier.height(7.dp).fillMaxWidth(1f))
            //Text("${locationName}:")
            Spacer(Modifier.height(5.dp).fillMaxWidth(1f))
            LazyHorizontalGrid(
                rows = GridCells.Fixed(1),
                contentPadding = PaddingValues(10.dp),
                modifier = Modifier.height(130.dp)
            ) {
                items(dataWeather[selectedLocation].forecast!!.forecastday[0].hour) {
                    weatherView(it)
                }
            }
            Spacer(Modifier.height(5.dp).fillMaxWidth(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(1f)) {
                Button(onClick = {
                    oldSelected = selectedLocation
                    if(flag)
                        getTwoWeeksLoading = true
                    else {
                        flag = true
                        selectedLocation = 0
                    }
                    }, modifier = Modifier.wrapContentHeight()
                ) {
                    Text(bottomButtonText)
                }
            }
        }
    }
}


@OptIn(ExperimentalUnitApi::class)
@Composable
fun weatherView(weather: Hour) {
    Card(
        modifier = Modifier.padding(10.dp).width(120.dp).wrapContentHeight(),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(5.dp),
        backgroundColor = Color(0xFF6b9ac4)
    ) {
        val hour = weather.time.split(" ")[1]
        Column(modifier = Modifier.padding(3.dp)) {
            Text(hour, Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(5.dp))
            Row(){
                Image(
                    bitmap = loadImage(weather.condition.icon),
                    contentDescription = "Condition",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(40.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("${weather.temp_c}°C",
                    fontSize = TextUnit(10f, TextUnitType.Sp)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(){
                Text("${weather.pressure_mb} mb",
                    fontSize = TextUnit(10f, TextUnitType.Sp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("${weather.humidity}%",
                    fontSize = TextUnit(10f, TextUnitType.Sp)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text("${weather.precip_mm} mm", Modifier.align(Alignment.CenterHorizontally))

        }
    }
}

fun loadImage(url: String): ImageBitmap {
    return ImageIO.read(URL("https:$url")).toComposeImageBitmap()
}