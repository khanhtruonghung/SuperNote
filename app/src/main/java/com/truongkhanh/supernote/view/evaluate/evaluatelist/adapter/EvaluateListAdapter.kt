package com.truongkhanh.supernote.view.evaluate.evaluatelist.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.Evaluate
import com.truongkhanh.supernote.model.enumclass.DAY
import com.truongkhanh.supernote.model.enumclass.MONTH
import com.truongkhanh.supernote.model.enumclass.WEEK
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.NULL_STRING
import com.truongkhanh.supernote.utils.getDateTimeString

class EvaluateListAdapter(
    private val context: Context,
    private val itemClickListener: (Evaluate) -> Unit
) : ListAdapter<Evaluate, EvaluateListViewHolder>(Evaluate.diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvaluateListViewHolder {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.item_list_evaluate, parent, false)
        val bag = DisposeBag(context as LifecycleOwner)
        return EvaluateListViewHolder(itemClickListener, bag, view)
    }

    override fun onBindViewHolder(holder: EvaluateListViewHolder, position: Int) {
        val data = getItem(position)

        holder.data = data
        holder.title.text = data.title
        holder.time.text = getDateTimeString(data.date)

        holder.evaluateText.text = getIconText(context, data.evaluateType)
        holder.evaluateIcon.background = getIconBackground(context, data.evaluateType)
    }

    private fun getIconText(context: Context, enum: Int): String {
        return when (enum) {
            DAY -> context.getString(R.string.lbl_day)
            WEEK -> context.getString(R.string.lbl_week)
            MONTH -> context.getString(R.string.lbl_month)
            else -> NULL_STRING
        }
    }

    private fun getIconBackground(context: Context, enum: Int): Drawable? {
        return when (enum) {
            DAY -> context.getDrawable(R.drawable.bg_circle_fb_red)
            WEEK -> context.getDrawable(R.drawable.bg_circle_fb_blue)
            MONTH -> context.getDrawable(R.drawable.bg_circle_fb_green)
            else -> null
        }
    }
}