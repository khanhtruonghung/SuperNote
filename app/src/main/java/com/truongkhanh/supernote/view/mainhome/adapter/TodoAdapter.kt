package com.truongkhanh.supernote.view.mainhome.adapter

import android.content.Context
import android.text.Spannable
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.utils.*
import java.util.*

class TodoAdapter(
    private val context: Context,
    private val listener: NotifyListener,
    dayInit: Int,
    private val checkDoneListener: (Todo) -> Unit,
    private val todoClickListener: (Todo) -> Unit
) : RecyclerView.Adapter<TodoViewHolder>(), Filterable {

    private var todoListFiltered: MutableList<Todo> = mutableListOf()
    private var todoList: MutableList<Todo> = mutableListOf()
    private var currentDay: Int = dayInit

    interface NotifyListener {
        fun notifyFiltered()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_simple_todo, parent, false)
        return TodoViewHolder(checkDoneListener, todoClickListener, view)
    }

    override fun getItemCount(): Int {
        return todoListFiltered.size
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val data = todoListFiltered[position]
        holder.data = data
        holder.description.text = data.description

        holder.description.visibility = getEnable(!data.isDone)
        holder.title.setTextColor(getTitleTextColor(data.isDone))
        if (data.isDone) {
            holder.title.setText(data.title, TextView.BufferType.SPANNABLE)
            val spannable: Spannable = holder.title.text as Spannable
            spannable.setSpan(StrikethroughSpan(), 0, data.title?.length ?: 0, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            holder.title.text = data.title
        }

        holder.startTime.text = getTimeString(data.startDate)
        holder.endTime.text = getTimeString(data.endDate)
        holder.separateTextView.text = getSeparateString(data)
        holder.priority.text = data.priority.toString()
        holder.checkbox.isChecked = data.isDone
    }

    private fun getTitleTextColor(isDone: Boolean): Int {
        return if(isDone)
            getColor(COLOR_GREY_500)
        else
            getColor(COLOR_WHITE_GREY)
    }

    fun setItems(items: MutableList<Todo>?) {
        val newItems = items ?: mutableListOf()
        todoList = newItems
        todoListFiltered = newItems
        notifyDataSetChanged()
    }

    fun notifyItem(newTodo: Todo) {
        var position = -1
        todoListFiltered.mapIndexed { index, todo ->
            if (todo.id == newTodo.id)
                position = index
        }
        if (position != -1) {
            todoList[position].isDone = newTodo.isDone
            todoListFiltered[position].isDone = newTodo.isDone
            notifyItemChanged(position)
        }
    }

    fun removeTodo(newTodo: Todo) {
        var position = -1
        todoList.forEachIndexed { index, todo ->
            if (todo.id == newTodo.id)
                position = index
        }
        if (position > -1)
            todoList.removeAt(position)

        var position2 = -1
        todoListFiltered.forEachIndexed { index, todo ->
            if (todo.id == newTodo.id)
                position2 = index
        }
        if (position2 > -1)
            todoListFiltered.removeAt(position2)
        notifyItemRemoved(position2)
    }

    private fun getTimeString(date: Long): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = date
        return when (calendar.get(Calendar.DAY_OF_MONTH)) {
            currentDay -> getTimeString2(calendar)
            else -> NULL_STRING
        }
    }

    private fun getSeparateString(data: Todo): String {
        val startCalendar = Calendar.getInstance(Locale.getDefault())
        startCalendar.timeInMillis = data.startDate
        val startDay: Int = startCalendar.get(Calendar.DAY_OF_MONTH)

        val endCalendar = Calendar.getInstance(Locale.getDefault())
        endCalendar.timeInMillis = data.endDate
        val endDay:Int = endCalendar.get(Calendar.DAY_OF_MONTH)

        return when {
            (currentDay != startDay && currentDay != endDay) -> {
                context.getString(R.string.lbl_all_day)
            }
            data.isAllDay -> {
                context.getString(R.string.lbl_all_day)
            }
            else -> {
                context.getString(R.string.lbl_separate_time)
            }
        }
    }

    override fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            currentDay = constraint.toString().toInt()
            val filterList: MutableList<Todo> = mutableListOf()
            todoList.forEach { todo ->
                val endDay = getTodoEndDay(todo)
                val startDay = getTodoStartDay(todo)
                if (isDayBetween(startDay, endDay, currentDay))
                    filterList.add(todo)
            }
            val filterResults = FilterResults()
            filterResults.values = filterList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            results?.values?.let {
                todoListFiltered = it as MutableList<Todo>
                notifyDataSetChanged()
                listener.notifyFiltered()
            }
        }
    }
}