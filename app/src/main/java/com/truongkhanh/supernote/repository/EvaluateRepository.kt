package com.truongkhanh.supernote.repository

import androidx.lifecycle.LiveData
import com.truongkhanh.supernote.model.Evaluate
import com.truongkhanh.supernote.service.EvaluateDao
import io.reactivex.Completable

class EvaluateRepository(private val evaluateDao: EvaluateDao) {
    fun getAll(): LiveData<MutableList<Evaluate>> = evaluateDao.getAllEvaluate()

    fun insert(evaluate: Evaluate): Long = evaluateDao.createEvaluate(evaluate)
    fun delete(evaluate: Evaluate): Completable = evaluateDao.deleteEvaluate(evaluate)
    fun update(evaluate: Evaluate): Completable = evaluateDao.updateEvaluate(evaluate)
}