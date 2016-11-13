package io.givenow.app.models

import android.content.Context
import com.parse.ParseClassName
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import java.util.*

@ParseClassName("DonationCategory")
class DonationCategory : ParseObject {

    var isClickable = true

    var isSelected = false

    constructor() : super() {
    }

    constructor(priority: Int, image: ParseFile,
                name_en: String, description_en: String,
                name_de: String, description_de: String) : super() {
        this.priority = priority
        this.image = image
        this.nameEN = name_en
        this.nameDE = name_de
        this.descriptionEN = description_en
        this.descriptionDE = description_de
    }

    fun getName(context: Context): String {
        if (context.resources.configuration.locale.language == Locale.GERMAN.language) {
            return nameDE
        } else {
            return nameEN
        }
    }

    fun getDescription(context: Context): String {
        if (context.resources.configuration.locale.language == Locale.GERMAN.language) {
            return descriptionDE
        } else {
            return descriptionEN
        }
    }

    var nameEN: String
        get() = getString("name_en")
        set(name_en) = put("name_en", name_en)

    var nameDE: String
        get() = getString("name_de")
        set(name_de) = put("name_de", name_de)

    var descriptionEN: String
        get() = getString("description_en")
        set(description_en) = put("description_en", description_en)

    var descriptionDE: String
        get() = getString("description_de")
        set(description_de) = put("description_de", description_de)

    var priority: Int
        get() = getInt("priority")
        set(priority) = put("priority", priority)

    var image: ParseFile
        get() = getParseFile("image")
        set(imageFile) = put("image", imageFile)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as DonationCategory

        return objectId == that.objectId
    }

    override fun hashCode(): Int {
        return objectId.hashCode()
    }

    companion object {

        fun fetchTop9(): ParseQuery<DonationCategory> {
            return ParseQuery.getQuery(DonationCategory::class.java)
                    .orderByAscending("priority")
                    .setLimit(9)
        }
    }
}
