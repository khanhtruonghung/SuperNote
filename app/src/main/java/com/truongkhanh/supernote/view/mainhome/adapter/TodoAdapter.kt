package com.truongkhanh.supernote.view.mainhome.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.utils.*

class TodoAdapter(private val context: Context, private val listener: NotifyListener, private val todoClickListener: (Todo) -> Unit) : RecyclerView.Adapter<TodoViewHolder>(), Filterable {

    private var todoListFiltered: MutableList<Todo> = mutableListOf()
    private var todoList: MutableList<Todo> = mutableListOf()

    interface NotifyListener {
        fun notifyFiltered()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simple_todo, parent, false)
        return TodoViewHolder(todoClickListener, view)
    }

    override fun getItemCount(): Int {
        return todoListFiltered.size
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val data = todoListFiltered[position]
        holder.data = data
        holder.title.text = data.title
        holder.description.text = data.description

        data.isAllDay?.let{isAllDay ->
            holder.startTime.text = getTimeString(isAllDay, data.startDate)
            holder.endTime.text = getTimeString(isAllDay, data.endDate)
            holder.separateTextView.text = getSeparateString(isAllDay)
        }
        holder.priority.text = data.priority.toString()
    }

    fun setItems(items: MutableList<Todo>?) {
        val newItems = items ?: mutableListOf()
        todoList = newItems
        todoListFiltered = newItems
        notifyDataSetChanged()
    }

    fun addItem(item: Todo) {
        todoList.add(item)
        todoListFiltered = todoList
        notifyItemInserted(todoList.size)
    }

    fun deleteItem(item: Todo) {
        val position = todoList.indexOf(item)
        todoList.removeAt(position)
        todoListFiltered = todoList
        notifyItemRemoved(position)
    }

    private fun getTimeString(isAllDay: Boolean, time: Long): String {
        return if (isAllDay)
            NULL_STRING
        else
            getTimeString(time)
    }

    private fun getSeparateString(isAllDay: Boolean): String {
        return if (isAllDay)
            context.getString(R.string.lbl_all_day)
        else
            context.getString(R.string.lbl_separate_time)
    }

    override fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val dayFilter = constraint.toString().toInt()
            val filterList : MutableList<Todo> = mutableListOf()
            todoList.forEach { todo ->
                val endDay = getTodoEndDay(todo)
                val startDay = getTodoStartDay(todo)
                if (isDayBetween(startDay, endDay, dayFilter))
                    filterList.add(todo)
            }
            val filterResults = FilterResults()
            filterResults.values = filterList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            results?.values?.let{
                todoListFiltered = it as MutableList<Todo>
                notifyDataSetChanged()
                listener.notifyFiltered()
            }
        }
    }
}