package com.truongkhanh.supernote.view.evaluate.createevaluate.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.EvaluateTodo
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.NULL_STRING
import com.truongkhanh.supernote.utils.convertFromString
import com.truongkhanh.supernote.utils.getTimeString

class EvaluateTodoDetailAdapter(private val context: Context, private val itemClickListener: (EvaluateTodo) -> Unit) : ListAdapter<EvaluateTodo, EvaluateTodoDetailViewHolder>(EvaluateTodo.diffUtil) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EvaluateTodoDetailViewHolder {
        val context = parent.context
        val bag = DisposeBag(context as LifecycleOwner)
        val view = LayoutInflater.from(context).inflate(R.layout.item_evaluate_detail, parent, false)
        return EvaluateTodoDetailViewHolder(itemClickListener, bag, view)
    }

    override fun onBindViewHolder(holder: EvaluateTodoDetailViewHolder, position: Int) {
        val data = getItem(position)

        holder.data = data
        holder.title.text = data.title
        holder.startDate.text = getTimeString(data.isAllDay, data.startDate)
        holder.endDate.text = getTimeString(data.isAllDay, data.endDate)
        holder.separate.text = getSeparateString(data.isAllDay)
        holder.checkSummary.text = getCheckSummaryString(data)
    }

    private fun getCheckSummaryString(data: EvaluateTodo): CharSequence {
        return data.checkList?.convertFromString()?.let{checkList ->
            val isChecked = checkList.count { checkItem -> checkItem.isChecked }
            isChecked.toString().plus("/").plus(checkList.size)
        } ?: "0/0"

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
}