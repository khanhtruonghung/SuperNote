package com.truongkhanh.supernote.view.createtodo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.ScheduleItem
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.THROTTLE_TIME
import com.truongkhanh.supernote.utils.disposedBy
import com.truongkhanh.supernote.utils.getDayMonthFormat
import java.util.concurrent.TimeUnit

class ScheduleListAdapter(context: Context, private val itemClickListener: (Pair<Int, ScheduleItem>) -> Unit) :
    ListAdapter<ScheduleItem, ScheduleListViewHolder>(ScheduleItem.diffUtil) {
    private val bag: DisposeBag = DisposeBag(context as LifecycleOwner)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleListViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_schedule_item, parent, false)
        return ScheduleListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleListViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(
        holder: ScheduleListViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val data = getItem(position)
        holder.data = data
        holder.tvDate.text = getDayMonthFormat(data.date)
        holder.tvStart.text = getTimeString(data.timeStart?.hour ?: 0, data.timeStart?.minute ?: 0)
        holder.tvEnd.text = getTimeString(data.timeEnd?.hour ?: 0, data.timeEnd?.minute ?: 0)

        RxView.clicks(holder.clTime)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                itemClickListener(Pair(position, data))
            }.disposedBy(bag)
    }

    private fun getTimeString(hour: Int, minute: Int): String {
        var hourString = hour.toString()
        var minuteString = minute.toString()
        if (hour < 10)
            hourString = "0".plus(hourString)
        if (minute < 10)
            minuteString = "0".plus(minuteString)
        return hourString.plus(":").plus(minuteString)
    }

}