package com.truongkhanh.supernote.utils

import android.content.Context
import android.graphics.Color
import android.view.View
import com.google.gson.GsonBuilder
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.*
import com.truongkhanh.supernote.view.dialog.bottomsheet.AlertPickerDialogFragment
import java.util.*

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

fun MutableList<CheckItem>.checkItemsToString(): String {
    val gson = GsonBuilder().create()
    return gson.toJson(this)
}

fun String.checkItemsFromString(): MutableList<CheckItem> {
    if (this.isEmpty())
        return  mutableListOf()
    val gson = GsonBuilder().create()
    return gson.fromJson(this, Array<CheckItem>::class.java).toMutableList()
}

fun MutableList<ScheduleItem>.scheduleItemsToString(): String {
    if (this.isNullOrEmpty())
        return NULL_STRING
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

fun String.notificationItemsFromString(): MutableList<NotificationItem> {
    val gson = GsonBuilder().create()
    return gson.fromJson(this, Array<NotificationItem>::class.java).toMutableList()
}

fun MutableList<NotificationItem>.notificationItemsToString(): String {
    val gson = GsonBuilder().create()
    return gson.toJson(this)
}

fun getEnable(enable: Boolean): Int {
    return if(enable)
        View.VISIBLE
    else
        View.GONE
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

fun Long.toCalendar(): Calendar {
    val c = Calendar.getInstance(Locale.getDefault())
    c.timeInMillis = this
    return c
}