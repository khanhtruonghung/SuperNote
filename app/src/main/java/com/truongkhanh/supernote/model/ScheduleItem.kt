package com.truongkhanh.supernote.model

import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil

data class ScheduleItem(
    var date: Long = 0L,
    var timeStart: MyCalendar?,
    var timeEnd: MyCalendar?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readParcelable(MyCalendar::class.java.classLoader),
        parcel.readParcelable(MyCalendar::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(date)
        parcel.writeParcelable(timeStart, flags)
        parcel.writeParcelable(timeEnd, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ScheduleItem> {
        override fun createFromParcel(parcel: Parcel): ScheduleItem {
            return ScheduleItem(parcel)
        }

        override fun newArray(size: Int): Array<ScheduleItem?> {
            return arrayOfNulls(size)
        }

        val diffUtil = object : DiffUtil.ItemCallback<ScheduleItem>() {
            override fun areItemsTheSame(oldItem: ScheduleItem, newItem: ScheduleItem): Boolean =
                (oldItem.date == newItem.date)

            override fun areContentsTheSame(oldItem: ScheduleItem, newItem: ScheduleItem): Boolean =
                (oldItem == newItem)
        }
    }
}