package com.truongkhanh.supernote.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun forceCloseKeyboard(activeView: View) {
    val inputMethodManager =
        activeView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(activeView.windowToken, 0)
}