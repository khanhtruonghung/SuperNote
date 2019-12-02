package com.truongkhanh.supernote.view.createtodo.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.truongkhanh.supernote.model.TagType
import kotlinx.android.synthetic.main.item_tag.view.*

class TagListViewHolder(
    itemClickListener: ((TagType) -> Unit)?,
    deleteClickListener: ((TagType) -> Unit)?,
    item: View
) : RecyclerView.ViewHolder(item) {
    val background: Drawable = item.rlTagBackground.background
    val name: TextView = item.tvTagName
    val deleteButton: ImageView = item.btnDeleteTag
    var tagType: TagType? = null

    init {
        deleteClickListener?.also { listener ->
            item.btnDeleteTag.setOnClickListener {
                tagType?.let {
                    listener(it)
                }
            }
        }
        itemClickListener?.also {listener ->
            item.setOnClickListener {
                tagType?.let {
                    listener(it)
                }
            }
        }
    }
}