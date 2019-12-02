package com.truongkhanh.supernote.view.dialog.bottomsheet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.*

class DateTimeDialogViewModel() : ViewModel() {

    val calendar = MutableLiveData<Calendar>()

    class Factory(): ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DateTimeDialogViewModel() as T
        }
    }
}