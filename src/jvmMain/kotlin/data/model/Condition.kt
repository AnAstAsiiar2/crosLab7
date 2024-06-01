package data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Condition(
    @SerializedName("icon")
    var icon : String
)
