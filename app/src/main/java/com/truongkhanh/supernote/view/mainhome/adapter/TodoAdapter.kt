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
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.model.ScheduleItem
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.dialog.bottomsheet.HIGH_PRIORITY
import com.truongkhanh.supernote.view.dialog.bottomsheet.HIGH_TEXT
import com.truongkhanh.supernote.view.dialog.bottomsheet.LOW_TEXT

class TodoAdapter(
    private val context: Context,
    private val listener: NotifyListener,
    dayInit: Int,
    monthInit: Int,
    private val checkDoneListener: (Todo) -> Unit,
    private val todoClickListener: (Todo) -> Unit
) : RecyclerView.Adapter<TodoViewHolder>(), Filterable {

    private var todoListFiltered: MutableList<Todo> = mutableListOf()
    private var todoList: MutableList<Todo> = mutableListOf()
    private var currentDay: Int = dayInit
    private var currentMonth: Int = monthInit

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

        holder.title.setTextColor(getTitleTextColor(data.isDone))
        if (data.isDone) {
            holder.title.setText(data.title, TextView.BufferType.SPANNABLE)
            val spannable: Spannable = holder.title.text as Spannable
            spannable.setSpan(
                StrikethroughSpan(),
                0,
                data.title?.length ?: 0,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            holder.title.text = data.title
        }

        getScheduleItems(data.schedule)?.let { scheduleItems ->
            var scheduleItem: ScheduleItem? = null
            for (item in scheduleItems) {
                if (item.timeStart?.day == currentDay && item.timeStart?.month == currentMonth) {
                    scheduleItem = item
                    break
                }
            }
            scheduleItem?.let {
                holder.startTime.text = getTimeString2(it.timeStart)
                holder.endTime.text = getTimeString2(it.timeEnd)
            }
        }
        holder.priority.text = data.priority.toString()
        holder.checkbox.isChecked = data.isDone
    }

    private fun getPriorityString(priority: Int): String {
        return when(priority) {
            HIGH_PRIORITY -> {
                HIGH_TEXT
            }
            else -> {
                LOW_TEXT
            }
        }
    }

    private fun getScheduleItems(schedule: String?): MutableList<ScheduleItem>? {
        if (!schedule.isThisEmpty())
            return schedule?.scheduleItemsFromString()
        return null
    }

    private fun getTitleTextColor(isDone: Boolean): Int {
        return if (isDone)
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
        for ((index, value) in todoList.withIndex()) {
            if (value.id == newTodo.id) {
                todoList[index] = newTodo
                break
            }
        }
        var position = 0
        for ((index, value) in todoListFiltered.withIndex()) {
            if (value.id == newTodo.id) {
                todoListFiltered[index] = newTodo
                position = index
                break
            }
        }
        notifyItemChanged(position)
    }

    fun removeTodo(todo: Todo) {
        var position = -1
        todoList.forEachIndexed { index, todo ->
            if (todo.id == todo.id)
                position = index
        }
        if (position > -1)
            todoList.removeAt(position)

        var position2 = -1
        todoListFiltered.forEachIndexed { index, oldTodo ->
            if (todo.id == oldTodo.id)
                position2 = index
        }
        if (position2 > -1)
            todoListFiltered.removeAt(position2)
        notifyItemRemoved(position2)
    }

    private fun getTimeString2(myCalendar: MyCalendar?): String {
        return when (myCalendar?.day) {
            currentDay -> {
                var hour = myCalendar.hour.toString()
                var minute = myCalendar.minute.toString()
                if (myCalendar.minute < 10)
                    minute = "0".plus(minute)
                if (myCalendar.hour < 10)
                    hour = "0".plus(hour)
                hour.plus(":").plus(minute)
            }
            else -> {
                NULL_STRING
            }
        }
    }

    override fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            constraint.toString().myCalendarFromString().let {
                currentDay = it.day
                currentMonth = it.month
            }
            val filterList: MutableList<Todo> = mutableListOf()
            todoList.forEach { todo ->
                getScheduleItems(todo.schedule)?.let { scheduleItems ->
                    for (item in scheduleItems) {
                        if (item.timeStart?.day == currentDay && item.timeStart?.month == currentMonth) {
                            filterList.add(todo)
                            break
                        }
                    }
                }
            }
            filterList.sortBy { todo ->
                var date: Long = 0
                todo.schedule?.scheduleItemsFromString()?.forEach { scheduleItem ->
                    if (scheduleItem.timeStart?.day == currentDay && scheduleItem.timeStart?.month == currentMonth)
                        date = scheduleItem.date
                }
                date
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