package com.truongkhanh.supernote.view.createtodo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.utils.DEFAULT_COLOR
import com.truongkhanh.supernote.utils.getColor

class TagListAdapter(
    private val itemClickListener: ((TagType) -> Unit)?,
    private val deleteClickListener: ((TagType) -> Unit)?
) : ListAdapter<TagType, TagListViewHolder>(TagType.diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false)
        return TagListViewHolder(itemClickListener, deleteClickListener, view)
    }

    override fun onBindViewHolder(holder: TagListViewHolder, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(
        holder: TagListViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val data = getItem(position)
        holder.tagType = data
        holder.background.setTint(getColor(data.color ?: DEFAULT_COLOR))
        holder.name.text = data.name
        if(deleteClickListener==null)
            holder.deleteButton.visibility = View.GONE
    }
}