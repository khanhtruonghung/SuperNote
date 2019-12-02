package com.truongkhanh.supernote.view.createtodo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.CheckItem
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.disposedBy

class CheckListAdapter(private val bag: DisposeBag, private val itemCheckedListener: ((CheckItem) -> Unit)?, private val itemDeleteClicked: ((CheckItem) -> Unit)?) : ListAdapter<CheckItem, CheckListViewHolder>(CheckItem.diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_check_list_item, parent, false)
        return CheckListViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckListViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(
        holder: CheckListViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val data = getItem(position)

        holder.data = data
        holder.title.setText(data.title)
        holder.checkBox.isChecked = data.isChecked

        if (itemCheckedListener != null) {
            RxView.clicks(holder.checkBox)
                .subscribe {
                    data.isChecked = holder.checkBox.isChecked
                    itemCheckedListener.also { it(data) }
                }.disposedBy(bag)
        } else {
            holder.checkBox.isEnabled = false
            holder.title.isEnabled = false
        }

        if (itemDeleteClicked != null) {
            RxTextView.textChanges(holder.title)
                .subscribe {
                    data.title = it.toString()
                }.disposedBy(bag)
            RxView.clicks(holder.delete)
                .subscribe {
                    itemDeleteClicked.also { it(data) }
                }.disposedBy(bag)
        } else {
            holder.title.isEnabled = false
            holder.delete.visibility = View.GONE
        }
    }
}