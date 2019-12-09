package com.truongkhanh.supernote.view.evaluate.evaluatelist.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.supernote.model.Evaluate
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.disposedBy
import kotlinx.android.synthetic.main.item_list_evaluate.view.*

class EvaluateListViewHolder(itemClickListener: (Evaluate) -> Unit, bag: DisposeBag, item: View) :
    RecyclerView.ViewHolder(item) {
    val title: TextView = item.tvEvaluateTitle
    val time: TextView = item.tvEvaluateTime
    val evaluateIcon: View = item.evaluateIcon
    val evaluateText: TextView = item.tvEvaluateIcon
    var data: Evaluate? = null

    init {
        RxView.clicks(item)
            .subscribe {
                data?.let { itemClickListener(it) }
            }.disposedBy(bag)
    }
}