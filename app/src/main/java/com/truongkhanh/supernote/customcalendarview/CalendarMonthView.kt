package com.truongkhanh.supernote.customcalendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView
import com.truongkhanh.supernote.utils.CALENDAR_TEXT_SIZE
import com.truongkhanh.supernote.utils.dpToPx
import com.truongkhanh.supernote.utils.getColor

class CalendarMonthView(context: Context) : MonthView(context) {

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

    private val selectedPaint = Paint().apply {
        this.color = getColor("#33B5E5")
        this.isAntiAlias = true
        this.style = Paint.Style.STROKE
        this.strokeWidth = 2f
    }

    private val mPadding = dipToPx(getContext(), 3f)
    private val mPointRadius = dipToPx(context, 2f).toFloat()

    override fun onDrawText(
        canvas: Canvas?,
        calendar: Calendar?,
        x: Int,
        y: Int,
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
        y: Int,
        hasScheme: Boolean
    ): Boolean {
        canvas?.drawRect(
            x.toFloat(),
            y.toFloat(),
            x + mItemWidth.toFloat(),
            y + mItemHeight.toFloat(),
            selectedPaint
        )
        return true
    }

    override fun onDrawScheme(canvas: Canvas?, calendar: Calendar?, x: Int, y: Int) {
        val paint = Paint().apply {
            this.isAntiAlias = true
            this.color = Color.BLUE
            this.style = Paint.Style.FILL
            this.strokeWidth = 2F
        }
        canvas?.drawCircle(
            (x + mItemWidth / 2).toFloat(),
            (y + mItemHeight / 2).toFloat(),
            mItemWidth / 2.5F,
            paint
        )
    }

    private fun dipToPx(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}