package com.truongkhanh.supernote.view.draftnote.list.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.THROTTLE_TIME
import com.truongkhanh.supernote.utils.disposedBy
import kotlinx.android.synthetic.main.item_list_note.view.*
import java.util.concurrent.TimeUnit

class DraftListViewHolder(bag: DisposeBag, longClickListener: (Pair<View, DraftNote>) -> Unit, itemClickListener: (DraftNote) -> Unit, item: View) : RecyclerView.ViewHolder(item) {
    val title: TextView = item.tvNoteTitle
    val content: TextView = item.tvNoteContent
    var data: DraftNote? = null

    init {
        RxView.clicks(item)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                data?.let{ itemClickListener(it) }
            }.disposedBy(bag)
        RxView.longClicks(item)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                data?.let{ longClickListener(Pair(item, it)) }
            }.disposedBy(bag)
    }
}