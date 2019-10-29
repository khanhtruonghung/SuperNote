package com.truongkhanh.supernote.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class Todo(
    var id: String?,
    var title: String?,
    var description: String?,
    var priority: Int?,
    var startDate: Date?,
    var endDate: Date?,
    var lastDayCheckDone: Date?,
    var tagListID: String?,
    var createDate: Date?,
    var updateDate: Date?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Date::class.java.classLoader) as? Date,
        parcel.readValue(Date::class.java.classLoader) as? Date,
        parcel.readValue(Date::class.java.classLoader) as? Date,
        parcel.readString(),
        parcel.readValue(Date::class.java.classLoader) as? Date,
        parcel.readValue(Date::class.java.classLoader) as? Date
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeValue(priority)
        parcel.writeValue(startDate)
        parcel.writeValue(endDate)
        parcel.writeValue(lastDayCheckDone)
        parcel.writeString(tagListID)
        parcel.writeValue(createDate)
        parcel.writeValue(updateDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Todo> {
        override fun createFromParcel(parcel: Parcel): Todo {
            return Todo(parcel)
        }

        override fun newArray(size: Int): Array<Todo?> {
            return arrayOfNulls(size)
        }
    }
}