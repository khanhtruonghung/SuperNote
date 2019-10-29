package com.truongkhanh.supernote.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class Evaluate(
    var id: String?,
    var evaluateTypeID: String?,
    var date: Date?,
    var listTodo: MutableList<Todo>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Date::class.java.classLoader) as? Date,
        parcel.createTypedArrayList(Todo) as MutableList<Todo>
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(evaluateTypeID)
        parcel.writeValue(date)
        parcel.writeTypedList(listTodo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Evaluate> {
        override fun createFromParcel(parcel: Parcel): Evaluate {
            return Evaluate(parcel)
        }

        override fun newArray(size: Int): Array<Evaluate?> {
            return arrayOfNulls(size)
        }
    }
}