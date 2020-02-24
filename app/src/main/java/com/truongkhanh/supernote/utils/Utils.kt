package com.truongkhanh.supernote.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.CheckItem
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.model.ScheduleItem
import com.truongkhanh.supernote.view.dialog.bottomsheet.AlertPickerDialogFragment

fun dpToPx(context: Context, dpValue: Float): Float {
    val scale = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f)
}

fun getAlertFormat(alertID: Int, context: Context): String {
    return when (alertID) {
        AlertPickerDialogFragment.NO_ALERT -> context.getString(R.string.lbl_no_alert)
        AlertPickerDialogFragment.ALERT_10_MINUTE -> context.getString(R.string.lbl_10_minutes)
        AlertPickerDialogFragment.ALERT_30_MINUTE -> context.getString(R.string.lbl_30_minutes)
        AlertPickerDialogFragment.ALERT_1_HOUR -> context.getString(R.string.lbl_1_hour)
        AlertPickerDialogFragment.ALERT_1_DAY -> context.getString(R.string.lbl_1_day)
        AlertPickerDialogFragment.ALERT_2_DAY -> context.getString(R.string.lbl_2_day)
        else -> context.getString(R.string.lbl_some_thing_went_wrong)
    }
}

fun getColor(colorString: String): Int {
    return Color.parseColor(colorString)
}

fun <T, K, R> MutableLiveData<T>.combineWith(
    mutableLiveData: MutableLiveData<K>,
    combineLogic: (T?, K?) -> R
): MutableLiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) {
        result.value = combineLogic.invoke(this.value, mutableLiveData.value)
    }
    result.addSource(mutableLiveData) {
        result.value = combineLogic.invoke(this.value, mutableLiveData.value)
    }
    return result
}

fun MutableList<CheckItem>.checkItemsToString(): String {
    val gson = GsonBuilder().create()
    return gson.toJson(this)
}

fun String.checkItemsFromString(): MutableList<CheckItem> {
    val gson = GsonBuilder().create()
    return gson.fromJson(this, Array<CheckItem>::class.java).toMutableList()
}

fun MutableList<ScheduleItem>.scheduleItemsToString(): String {
    val gson = GsonBuilder().create()
    return gson.toJson(this)
}

fun String.myCalendarFromString(): MyCalendar {
    val gson = GsonBuilder().create()
    return gson.fromJson(this, MyCalendar::class.java)
}

fun MyCalendar.myCalendarToString(): String {
    val gson = GsonBuilder().create()
    return gson.toJson(this)
}

fun String.scheduleItemsFromString(): MutableList<ScheduleItem> {
    val gson = GsonBuilder().create()
    return gson.fromJson(this, Array<ScheduleItem>::class.java).toMutableList()
}

fun getEnable(enable: Boolean): Int {
    return if(enable)
        View.VISIBLE
    else
        View.GONE
}

fun getEvaluateIconBackground(context: Context, enum: Int): Drawable? {
    return when (enum) {
        0 -> context.getDrawable(R.drawable.bg_circle_fb_blue)
        1 -> context.getDrawable(R.drawable.bg_circle_fb_red)
        2 -> context.getDrawable(R.drawable.bg_circle_fb_green)
        else -> null
    }
}

fun getEvaluateIconText(context: Context, enum: Int): String {
    return when (enum) {
        0 -> context.getString(R.string.lbl_day)
        1 -> context.getString(R.string.lbl_week)
        2 -> context.getString(R.string.lbl_month)
        else -> context.getString(R.string.lbl_day)
    }
}

fun String?.isThisEmpty(): Boolean {
    return  this == null || this.isEmpty() || this == NULL_STRING
}

fun DraftNote.clone(): DraftNote {
    return DraftNote(
        this.id,
        this.title,
        this.description,
        this.priority,
        this.estimateTotal,
        this.estimateDaily,
        this.startDate,
        this.deadline
    )
}