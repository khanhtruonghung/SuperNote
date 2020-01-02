package com.truongkhanh.supernote.view.draftnote.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.utils.DisposeBag

class DraftListAdapter(private val longClickListener: (Pair<View, DraftNote>) -> Unit, private val itemClickListener: (DraftNote) -> Unit) : ListAdapter<DraftNote, DraftListViewHolder>(DraftNote.diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_note, parent, false)
        val bag = DisposeBag(parent.context as LifecycleOwner)
        return DraftListViewHolder(bag, longClickListener, itemClickListener , view)
    }

    override fun onBindViewHolder(holder: DraftListViewHolder, position: Int) {
        val data = getItem(position)
        holder.data = data
        holder.title.text = data.title
        holder.content.text = data.description
    }
}