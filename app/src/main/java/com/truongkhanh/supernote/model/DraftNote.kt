package com.truongkhanh.supernote.model

import android.os.Parcel
import android.os.Parcelable
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
    var description: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
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
    }
}