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
    @ColumnInfo(name = "End_Date")
    var endDate: Long,
    @ColumnInfo(name = "Alert_Type")
    var alertType: Int,
    @ColumnInfo(name = "Check_Done_Date")
    var checkDoneDate: Long = 0L,
    @ColumnInfo(name = "Is_Done")
    var isDone: Boolean = false,
    @ColumnInfo(name = "Notification_Request_ID")
    var notificationRequestID: String?,
    @ColumnInfo(name = "Schedule")
    var schedule: String?,
    var dateTimeStamp: Long = 0L,
    var isDateTimeStamp: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
                parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(checkList)
        parcel.writeValue(priority)
        parcel.writeLong(startDate)
        parcel.writeLong(endDate)
        parcel.writeInt(alertType)
        parcel.writeLong(checkDoneDate)
        parcel.writeByte(if (isDone) 1 else 0)
        parcel.writeString(notificationRequestID)
        parcel.writeString(schedule)
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