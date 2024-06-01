package data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastDay(
    @SerializedName("hour")
    var hour: MutableList<Hour>
)
