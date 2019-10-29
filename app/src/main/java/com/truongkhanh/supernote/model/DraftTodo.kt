package com.truongkhanh.supernote.model

import android.os.Parcel
import android.os.Parcelable

data class DraftTodo(
    var id: String?,
    var listTodoID: MutableList<String> = mutableListOf(),
    var isOptimized: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.createStringArrayList() as MutableList<String>,
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeStringList(listTodoID)
        parcel.writeByte(if (isOptimized) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DraftTodo> {
        override fun createFromParcel(parcel: Parcel): DraftTodo {
            return DraftTodo(parcel)
        }

        override fun newArray(size: Int): Array<DraftTodo?> {
            return arrayOfNulls(size)
        }
    }
}
