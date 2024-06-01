package data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Hour(
    @SerializedName("time")
    var time: String,
    @SerializedName("temp_c")
    var temp_c: Float,
    @SerializedName("condition")
    var condition: Condition,
    @SerializedName("pressure_mb")
    var pressure_mb: Float,
    @SerializedName("precip_mm")
    var precip_mm: Float,
    @SerializedName("humidity")
    var humidity: Int
)
