package com.truongkhanh.supernote.utils

import android.content.Context

fun dpToPx(context: Context, dpValue: Float): Float {
    val scale = context.resources.displayMetrics.density
    return (dpValue * scale + 0.5f)
}