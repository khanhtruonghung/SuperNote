package com.truongkhanh.supernote.utils

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation

open class CustomAnimation {
    fun expand(baseView: View, childView: View) {
        val matchParentMeasureSpec: Int = View.MeasureSpec.makeMeasureSpec(
            (childView.parent as View).width,
            View.MeasureSpec.EXACTLY
        )
        val wrapContentMeasureSpec: Int =
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        childView.measure(matchParentMeasureSpec, wrapContentMeasureSpec)

        val targetHeight: Int = baseView.measuredHeight + childView.measuredHeight
        val initialHeight: Int = baseView.measuredHeight
        val heightNeedToExpand: Int = childView.measuredHeight
        childView.visibility = View.VISIBLE
        val a: Animation = object : Animation() {
                override fun applyTransformation(
                    interpolatedTime: Float,
                    t: Transformation?
                ) {
                    baseView.layoutParams.height =
                        if (interpolatedTime == 1f) {
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        } else {
                            initialHeight + (heightNeedToExpand * interpolatedTime).toInt()
                        }
                    baseView.requestLayout()
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }
        // Expansion speed of 1dp/ms
        a.duration = (targetHeight / baseView.context.resources.displayMetrics.density).toLong()
        baseView.startAnimation(a)
    }

    fun collapse(baseView: View, childView: View) {
        val initialHeight: Int = baseView.measuredHeight
        val childHeight: Int = childView.measuredHeight
        val a: Animation =
            object : Animation() {
                override fun applyTransformation(
                    interpolatedTime: Float,
                    t: Transformation?
                ) {
                    if (interpolatedTime == 1f) {
                        childView.visibility = View.GONE
                    } else {
                        baseView.layoutParams.height =
                            initialHeight - (childHeight * interpolatedTime).toInt()
                        baseView.requestLayout()
                    }
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }
        // Collapse speed of 1dp/ms
        a.duration = (initialHeight / baseView.context.resources.displayMetrics.density).toLong()
        baseView.startAnimation(a)
    }
}