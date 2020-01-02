package com.truongkhanh.supernote.customcalendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView
import com.truongkhanh.supernote.utils.CALENDAR_TEXT_SIZE
import com.truongkhanh.supernote.utils.COLOR_WHITE_GREY
import com.truongkhanh.supernote.utils.dpToPx
import com.truongkhanh.supernote.utils.getColor
import kotlin.math.min

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
        this.color = getColor(COLOR_WHITE_GREY)
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
    private var radius: Int = 0
    private var mRadio = dipToPx(getContext(), 3f).toFloat()

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

    override fun onPreviewHook() {
        radius = min(mItemWidth, mItemHeight) / 5 * 2
        mSchemePaint.style = Paint.Style.FILL_AND_STROKE
    }

    override fun onDrawScheme(canvas: Canvas?, calendar: Calendar?, x: Int, y: Int) {
        val cx = x + mItemWidth / 2F
        val cy = y + mItemHeight / 2F
        canvas?.drawCircle(
            cx,
            cy + (mRadio * 4),
            mRadio,
            mSchemePaint
        )
    }

    private fun dipToPx(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}