package com.truongkhanh.supernote.model

import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Tag_Type")
data class TagType(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "name")
    var name: String?,
    @ColumnInfo(name = "color")
    var color: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(color)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TagType> {
        override fun createFromParcel(parcel: Parcel): TagType {
            return TagType(parcel)
        }

        override fun newArray(size: Int): Array<TagType?> {
            return arrayOfNulls(size)
        }

        val diffUtil = object : DiffUtil.ItemCallback<TagType>() {
            override fun areItemsTheSame(oldItem: TagType, newItem: TagType): Boolean =
                (oldItem.id == newItem.id)

            override fun areContentsTheSame(oldItem: TagType, newItem: TagType): Boolean =
                (oldItem.name == newItem.name && oldItem.color == newItem.color)
        }
    }
}