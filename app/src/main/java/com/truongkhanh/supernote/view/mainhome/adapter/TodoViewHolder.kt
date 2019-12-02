package com.truongkhanh.supernote.view.mainhome.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truongkhanh.supernote.model.Todo
import kotlinx.android.synthetic.main.item_simple_todo.view.*

class TodoViewHolder(todoClickListener: (Todo) -> Unit, item: View) :
    RecyclerView.ViewHolder(item) {
    val title:TextView = item.tvTodoTitle
    val description: TextView = item.tvTodoDescription
    val priority: TextView = item.tvPriority
    val startTime: TextView = item.tvStartTime
    val endTime: TextView = item.tvEndTime
    val separateTextView: TextView = item.tvSeparate
    var data: Todo? = null

    init {
        item.setOnClickListener {
            data?.let { todoClickListener(it) }
        }
    }
}