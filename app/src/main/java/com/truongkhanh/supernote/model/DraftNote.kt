package com.truongkhanh.supernote.model

import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Draft_Note")
data class DraftNote(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "Title")
    var title: String?,
    @ColumnInfo(name = "Description")
    var description: String?,
    @ColumnInfo(name = "Priority")
    var priority: Int,
    @ColumnInfo(name = "Estimate_Total")
    var estimateTotal: Int,
    @ColumnInfo(name = "Estimate_Daily")
    var estimateDaily: Int,
    @ColumnInfo(name = "Start_Date")
    var startDate: Long,
    @ColumnInfo(name = "Deadline")
    var deadline: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeInt(priority)
        parcel.writeInt(estimateTotal)
        parcel.writeInt(estimateDaily)
        parcel.writeLong(startDate)
        parcel.writeLong(deadline)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DraftNote> {
        override fun createFromParcel(parcel: Parcel): DraftNote {
            return DraftNote(parcel)
        }

        override fun newArray(size: Int): Array<DraftNote?> {
            return arrayOfNulls(size)
        }

        val diffUtil = object : DiffUtil.ItemCallback<DraftNote>() {
            override fun areItemsTheSame(oldItem: DraftNote, newItem: DraftNote): Boolean =
                (oldItem.id == newItem.id)

            override fun areContentsTheSame(oldItem: DraftNote, newItem: DraftNote): Boolean =
                (oldItem == newItem)
        }
    }
}