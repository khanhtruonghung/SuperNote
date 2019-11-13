package com.truongkhanh.supernote.model

import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Todo")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "Title")
    var title: String?,
    @ColumnInfo(name = "Description")
    var description: String?,
    @ColumnInfo(name = "Check_List")
    var checkList: String?,
    @ColumnInfo(name = "Priority")
    var priority: Int?,
    @ColumnInfo(name = "Start_Date")
    var startDate: Long,
    @ColumnInfo(name = "String_Start_Date")
    var stringStartDate: String?,
    @ColumnInfo(name = "End_Date")
    var endDate: Long,
    @ColumnInfo(name = "String_End_Date")
    var stringEndDate: String?,
    @ColumnInfo(name = "Is_All_Day")
    val isAllDay: Boolean? = false,
    @ColumnInfo(name = "Check_Done_Date")
    var checkDoneDate: Long = 0L
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readLong(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(checkList)
        parcel.writeValue(priority)
        parcel.writeLong(startDate)
        parcel.writeString(stringStartDate)
        parcel.writeLong(endDate)
        parcel.writeString(stringEndDate)
        parcel.writeByte(if (isAllDay!!) 1 else 0)
        parcel.writeLong(checkDoneDate)
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

        val diffUtil = object : DiffUtil.ItemCallback<Todo>() {
            override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean =
                (oldItem.id == newItem.id)

            override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean =
                (oldItem == newItem)
        }
    }
}