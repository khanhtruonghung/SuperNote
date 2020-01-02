package com.truongkhanh.supernote.utils

import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.model.Todo
import java.text.SimpleDateFormat
import java.util.*

fun getDateTimeFormat(calendar: Calendar): String {
    val simpleDateFormat = SimpleDateFormat("dd, MMM yyyy HH:mm", Locale.getDefault())
    return simpleDateFormat.format(calendar.time)
}

fun getDateFormat(calendar: Calendar): String {
    val simpleDateFormat = SimpleDateFormat("dd, MMM yyyy", Locale.getDefault())
    return simpleDateFormat.format(calendar.time)
}

fun getDefaultDateFormat(calendar: Calendar): String {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return simpleDateFormat.format(calendar.time)
}

fun getMyCalendar(calendar: com.haibin.calendarview.Calendar): MyCalendar {
    return MyCalendar(
        calendar.day,
        calendar.month - 1,
        calendar.year,
        0, 0
    )
}

fun getMyCalendar(calendar: Calendar): MyCalendar {
    return MyCalendar(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.MINUTE),
        calendar.get(Calendar.HOUR_OF_DAY)
    )
}

fun getCalendarFromMyCalendar(myCalendar: MyCalendar): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, myCalendar.year)
    calendar.set(Calendar.MONTH, myCalendar.month)
    calendar.set(Calendar.DAY_OF_MONTH, myCalendar.day)
    calendar.set(Calendar.HOUR_OF_DAY, myCalendar.hour)
    calendar.set(Calendar.MINUTE, myCalendar.minute)
    return calendar
}

fun getTodoEndDay(todo: Todo): Int {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.timeInMillis = todo.endDate
    return calendar.get(Calendar.DAY_OF_MONTH)
}

fun getTodoStartDay(todo: Todo): Int {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.timeInMillis = todo.startDate
    return calendar.get(Calendar.DAY_OF_MONTH)
}

fun isDayBetween(startDay: Int, endDay: Int, dayCompare: Int): Boolean {
    return dayCompare in startDay..endDay
}

fun getTimeString(timeInMillis: Long): String {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.timeInMillis = timeInMillis
    val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return simpleDateFormat.format(calendar.time)
}

fun getDateTimeString(timeInMillis: Long): String {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.timeInMillis = timeInMillis
    val simpleDateFormat = SimpleDateFormat("dd, MMM yyyy HH:mm", Locale.getDefault())
    return  simpleDateFormat.format(calendar.time)
}

fun getTimeString2(calendar: Calendar): String {
    val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return simpleDateFormat.format(calendar.time)
}

fun getDateString(dateTime: Long): String {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.timeInMillis = dateTime
    val simpleDateFormat = SimpleDateFormat("dd/mm/yyyy", Locale.getDefault())
    return simpleDateFormat.format(calendar.time)
}