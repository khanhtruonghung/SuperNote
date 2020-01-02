package com.truongkhanh.supernote.model

data class ScheduleItem(
    var date: Long = 0L,
    var timeStart: MyCalendar?,
    var timeEnd: MyCalendar?
)