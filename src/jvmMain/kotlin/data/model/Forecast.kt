package data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Forecast(
    @SerializedName("forecastday")
    var forecastday: List<ForecastDay>
)
