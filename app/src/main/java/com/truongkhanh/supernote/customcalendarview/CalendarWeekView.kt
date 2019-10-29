package com.truongkhanh.supernote.customcalendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.WeekView
import com.truongkhanh.supernote.utils.CALENDAR_TEXT_SIZE
import com.truongkhanh.supernote.utils.dpToPx

class CalendarWeekView(context: Context) : WeekView(context) {

    private val curDayTextPaint = Paint().apply {
        this.isAntiAlias = true
        this.textAlign = Paint.Align.CENTER
        this.color = Color.RED
        this.isFakeBoldText = true
        this.textSize = dpToPx(
            context,
            CALENDAR_TEXT_SIZE
        )
    }

    private val curMonthTextPaint = Paint().apply {
        this.isAntiAlias = true
        this.textAlign = Paint.Align.CENTER
        this.color = Color.WHITE
        this.isFakeBoldText = true
        this.textSize = dpToPx(
            context,
            CALENDAR_TEXT_SIZE
        )
    }

    private val notCurMonthTextPaint = Paint().apply {
        this.isAntiAlias = true
        this.textAlign = Paint.Align.CENTER
        this.color = Color.GRAY
        this.isFakeBoldText = true
        this.textSize = dpToPx(
            context,
            CALENDAR_TEXT_SIZE
        )
    }

    override fun onDrawText(
        canvas: Canvas?,
        calendar: Calendar?,
        x: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
        val baselineY = mTextBaseLine + y
        val cx = x + mItemWidth / 2F
        canvas?.drawText(
            calendar?.day.toString(),
            cx,
            baselineY,
            when {
                calendar?.isCurrentDay!! -> curDayTextPaint
                calendar.isCurrentMonth -> curMonthTextPaint
                else -> notCurMonthTextPaint
            }
        )
    }

    override fun onDrawSelected(
        canvas: Canvas?,
        calendar: Calendar?,
        x: Int,
        hasScheme: Boolean
    ): Boolean {
        canvas?.drawCircle(
            (x + mItemWidth / 2).toFloat(),
            mItemHeight / 2F,
            mItemWidth / 2.5F,
            mSelectedPaint
        )
        return true
    }

    override fun onDrawScheme(canvas: Canvas?, calendar: Calendar?, x: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.BLUE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10F
        canvas?.drawCircle(
            x + mItemWidth / 2F,
            (mItemHeight - 3 * paddingTop).toFloat(),
            mItemHeight / 2F,
            paint
        )
    }

}