package com.truongkhanh.supernote.model

import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil

data class CheckItem(
    var id: Int,
    var title: String?,
    var isChecked: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeByte(if (isChecked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CheckItem> {

        const val IS_CHECK_ITEM_CHANGE = 1

        val diffUtil = object : DiffUtil.ItemCallback<CheckItem>() {
            override fun areItemsTheSame(oldItem: CheckItem, newItem: CheckItem): Boolean =
                (oldItem.title == newItem.title && oldItem.isChecked == newItem.isChecked)

            override fun areContentsTheSame(oldItem: CheckItem, newItem: CheckItem): Boolean =
                (oldItem.title == newItem.title && oldItem.isChecked == newItem.isChecked)

            override fun getChangePayload(oldItem: CheckItem, newItem: CheckItem) =
                if (oldItem.isChecked != newItem.isChecked)
                    IS_CHECK_ITEM_CHANGE
                else
                    null
        }

        override fun createFromParcel(parcel: Parcel): CheckItem {
            return CheckItem(parcel)
        }

        override fun newArray(size: Int): Array<CheckItem?> {
            return arrayOfNulls(size)
        }
    }
}