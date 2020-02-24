package com.truongkhanh.supernote.view.evaluate.createevaluate.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.getDateTimeString
import java.text.SimpleDateFormat
import java.util.*

class EvaluateTodoDetailAdapter(
    private val context: Context,
    private val checkDoneListener: (Todo) -> Unit,
    private val itemClickListener: (Todo) -> Unit
) : ListAdapter<Todo, RecyclerView.ViewHolder>(Todo.diffUtil) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val context = parent.context
        val bag = DisposeBag(context as LifecycleOwner)
        val todoView = LayoutInflater.from(context).inflate(R.layout.item_evaluate_detail, parent, false)
        val dateTimeStampView = LayoutInflater.from(context).inflate(R.layout.item_date_time_stamp, parent, false)
        return when(viewType) {
            NORMAL_TODO_TYPE -> EvaluateTodoDetailViewHolder(checkDoneListener, itemClickListener, bag, todoView)
            DATE_TIME_STAMP_TYPE -> DateTimeStampViewHolder(dateTimeStampView)
            else -> EvaluateTodoDetailViewHolder(checkDoneListener, itemClickListener, bag, todoView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            NORMAL_TODO_TYPE -> {
                val todoHolder = holder as EvaluateTodoDetailViewHolder
                val data = getItem(position)
                todoHolder.data = data
                todoHolder.title.text = data.title
                todoHolder.startDate.text = getDateTimeString(data.startDate)
                todoHolder.endDate.text = getDateTimeString(data.endDate)
            }
            DATE_TIME_STAMP_TYPE -> {
                val dateTimeStampHolder = holder as DateTimeStampViewHolder
                val data = getItem(position)
                dateTimeStampHolder.dateTimeStamp.text = getDateTimeStampString(data)
            }
        }
    }

    fun notifyItem(newTodo: Todo) {
        var position = -1
        currentList.mapIndexed { index, todo ->
            if (todo.id == newTodo.id)
                position = index
        }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    private fun getDateTimeStampString(data: Todo): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = data.dateTimeStamp
        val simpleDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isDateTimeStamp)
            DATE_TIME_STAMP_TYPE
        else NORMAL_TODO_TYPE
    }

//    private fun getTimeString(isAllDay: Boolean, time: Long): String {
//        return if (isAllDay)
//            NULL_STRING
//        else
//            com.truongkhanh.supernote.utils.getTimeString(time)
//    }
//
//    private fun getSeparateString(isAllDay: Boolean): String {
//        return if (isAllDay)
//            context.getString(R.string.lbl_all_day)
//        else
//            context.getString(R.string.lbl_separate_time)
//    }
}

const val NORMAL_TODO_TYPE = 0
const val DATE_TIME_STAMP_TYPE = 1