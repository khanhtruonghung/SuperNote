package com.truongkhanh.supernote.model

import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Evaluate",
    indices = [Index("Evaluate_Type")])
data class Evaluate(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int,
    @ColumnInfo(name = "Evaluate_Type")
    var evaluateType: Int,
    @ColumnInfo(name = "Date")
    var date: Long,
    @ColumnInfo(name = "Evaluate_Day")
    var day: Int,
    @ColumnInfo(name = "Evaluate_Week")
    var week: Int,
    @ColumnInfo(name = "Evaluate_Month")
    var month: Int,
    @ColumnInfo(name = "Evaluate_Year")
    var year: Int,
    @ColumnInfo(name = "Title")
    var title: String?,
    @ColumnInfo(name = "Description")
    var description: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(evaluateType)
        parcel.writeLong(date)
        parcel.writeInt(day)
        parcel.writeInt(week)
        parcel.writeInt(month)
        parcel.writeInt(year)
        parcel.writeString(title)
        parcel.writeString(description)
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

        val diffUtil = object : DiffUtil.ItemCallback<Evaluate>() {
            override fun areItemsTheSame(oldItem: Evaluate, newItem: Evaluate): Boolean =
                (oldItem.id == newItem.id)

            override fun areContentsTheSame(oldItem: Evaluate, newItem: Evaluate): Boolean =
                (oldItem == newItem)
        }
    }
}