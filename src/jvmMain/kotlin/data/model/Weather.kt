package data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Weather(
    @SerializedName("location")
    var location: Location?,
    @SerializedName("forecast")
    var forecast: Forecast?
)
