package com.truongkhanh.supernote.model

import android.os.Parcel
import android.os.Parcelable

data class DraftNote(
    var id: String?,
    var title: String?,
    var description: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
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