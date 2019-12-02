package com.truongkhanh.supernote.customcalendarview

import android.content.Context
import android.graphics.Canvas
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView

class CalendarMultiSelectMonthView (context: Context): MonthView(context) {
    override fun onDrawText(
        canvas: Canvas?,
        calendar: Calendar?,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDrawSelected(
        canvas: Canvas?,
        calendar: Calendar?,
        x: Int,
        y: Int,
        hasScheme: Boolean
    ): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDrawScheme(canvas: Canvas?, calendar: Calendar?, x: Int, y: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}