package data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Location(
    @SerializedName("name")
    var name: String,
    @SerializedName("localtime")
    var localtime: String
)
