package com.truongkhanh.supernote.view.createtodo.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truongkhanh.supernote.model.ScheduleItem
import kotlinx.android.synthetic.main.item_schedule_item.view.*

class ScheduleListViewHolder(
    item: View
) : RecyclerView.ViewHolder(item) {
    val tvDate: TextView = item.tvDate
    val tvStart: TextView = item.tvStart
    val tvEnd: TextView = item.tvEnd
    val clTime: View = item.clTime
    var data: ScheduleItem? = null
}