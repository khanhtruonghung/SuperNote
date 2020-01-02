package com.truongkhanh.supernote.customcalendarview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.WeekView
import com.truongkhanh.supernote.utils.CALENDAR_TEXT_SIZE
import com.truongkhanh.supernote.utils.COLOR_WHITE_GREY
import com.truongkhanh.supernote.utils.dpToPx
import kotlin.math.min

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
        this.color = com.truongkhanh.supernote.utils.getColor(COLOR_WHITE_GREY)
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
        this.color = com.truongkhanh.supernote.utils.getColor("#33B5E5")
        this.isAntiAlias = true
        this.style = Paint.Style.STROKE
        this.strokeWidth = 2f
    }

    private var radius: Int = 0

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
        canvas?.drawRect(
            x.toFloat(),
            0F,
            x + mItemWidth.toFloat(),
            mItemHeight.toFloat(),
            selectedPaint
        )
        return true
    }

    override fun onPreviewHook() {
        radius = min(mItemWidth, mItemHeight) / 5 * 2
        mSchemePaint.style = Paint.Style.FILL_AND_STROKE
    }

    private var mRadio = dipToPx(getContext(), 3f).toFloat()

    override fun onDrawScheme(canvas: Canvas?, calendar: Calendar?, x: Int) {
        val cx = x + mItemWidth / 2F
        val cy = mItemHeight / 2F
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