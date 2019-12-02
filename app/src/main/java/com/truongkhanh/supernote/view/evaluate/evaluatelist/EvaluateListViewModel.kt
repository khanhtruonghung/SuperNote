package com.truongkhanh.supernote.view.evaluate.evaluatelist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.truongkhanh.supernote.model.Evaluate
import com.truongkhanh.supernote.repository.EvaluateRepository
import com.truongkhanh.supernote.service.ApplicationDatabase

class EvaluateListViewModel(context: Context) : ViewModel() {

    private val evaluateRepository: EvaluateRepository
    var evaluateList: LiveData<MutableList<Evaluate>>

    init {
        val dbConnection = ApplicationDatabase.getInstance(context)
        evaluateRepository = EvaluateRepository(dbConnection.evaluateDao())
        evaluateList = evaluateRepository.getAll()
    }

    class Factory(private val context: Context): ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EvaluateListViewModel(
                context = context
            ) as T
        }
    }
}