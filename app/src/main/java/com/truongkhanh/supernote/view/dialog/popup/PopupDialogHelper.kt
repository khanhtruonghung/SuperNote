package com.truongkhanh.supernote.view.dialog.popup

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.utils.dpToPx

class PopupDialogHelper {
    companion object {
        fun setUpColorPickerPopup(
            context: Context,
            baseView: View,
            itemClickListener: (String) -> Unit
        ) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.fragment_color_picker_dialog, null)

            val popupWindow = getPopupWindow(view)
            initClickListener(popupWindow, view, itemClickListener)

            if (popupWindow.isShowing) {
                popupWindow.dismiss()
            } else {
                popupWindow.showAsDropDown(baseView, 0, -dpToPx(context, 110F).toInt())
            }
        }

        private fun initClickListener(popupWindow: PopupWindow, view: View, itemClickListener: (String) -> Unit) {
            view.findViewById<View>(R.id.colorRed)?.also {
                it.setOnClickListener {
                    itemClickListener("#FF4444")
                    popupWindow.dismiss()
                }
            }
            view.findViewById<View>(R.id.colorBlue)?.also {
                it.setOnClickListener {
                    itemClickListener("#33B5E5")
                    popupWindow.dismiss()
                }
            }
            view.findViewById<View>(R.id.colorGreen)?.also {
                it.setOnClickListener {
                    itemClickListener("#99CC00")
                    popupWindow.dismiss()
                }
            }
            view.findViewById<View>(R.id.colorOrange)?.also {
                it.setOnClickListener {
                    itemClickListener("#FFBB33")
                    popupWindow.dismiss()
                }
            }
        }

        private fun getPopupWindow(view: View): PopupWindow {
            return PopupWindow(
                view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).also {popupWindow ->
                popupWindow.elevation = 20f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    popupWindow.overlapAnchor = true
                }
                popupWindow.isOutsideTouchable = true
            }
        }
    }
}