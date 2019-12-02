package com.truongkhanh.supernote.model

import android.os.Parcel
import android.os.Parcelable

data class MyCalendar(
    var day: Int,
    var month: Int,
    var year: Int,
    var minute: Int,
    var hour: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(day)
        parcel.writeInt(month)
        parcel.writeInt(year)
        parcel.writeInt(minute)
        parcel.writeInt(hour)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyCalendar> {
        override fun createFromParcel(parcel: Parcel): MyCalendar {
            return MyCalendar(parcel)
        }

        override fun newArray(size: Int): Array<MyCalendar?> {
            return arrayOfNulls(size)
        }
    }
}