package com.truongkhanh.supernote.view.createtodo.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.truongkhanh.supernote.model.CheckItem
import kotlinx.android.synthetic.main.item_check_list_item.view.*

class CheckListViewHolder(
    item: View
) : RecyclerView.ViewHolder(item) {
    var title: EditText = item.etContent
    var checkBox: CheckBox = item.cbCheckList
    var delete: ImageView = item.btnDeleteItem
    var data: CheckItem? = null

}