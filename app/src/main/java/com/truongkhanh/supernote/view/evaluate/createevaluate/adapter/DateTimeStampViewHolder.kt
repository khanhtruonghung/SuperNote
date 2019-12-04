package com.truongkhanh.supernote.view.evaluate.createevaluate.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_date_time_stamp.view.*

class DateTimeStampViewHolder(item: View) : RecyclerView.ViewHolder(item) {
    val dateTimeStamp: TextView = item.tvDateTimeStamp
}