package com.truongkhanh.supernote.repository

import androidx.lifecycle.LiveData
import com.truongkhanh.supernote.model.Evaluate
import com.truongkhanh.supernote.service.EvaluateDao
import io.reactivex.Completable
import io.reactivex.Single

class EvaluateRepository(private val evaluateDao: EvaluateDao) {
    fun getAll(): LiveData<MutableList<Evaluate>> = evaluateDao.getAllEvaluate()

    fun getDayEvaluate(day: Int, month: Int, year: Int): Single<Evaluate?> =
        evaluateDao.getDayEvaluate(day, month, year)

    fun getWeekEvaluate(week: Int, month: Int, year: Int): Single<Evaluate> =
        evaluateDao.getWeekEvalute(week, month, year)

    fun getMonthEvaluate(month: Int, year: Int): Single<Evaluate> =
        evaluateDao.getMonthEvaluate(month, year)

    fun getEvaluateByRowid(rowid: Long): Single<Evaluate> = evaluateDao.getEvaluateByRowid(rowid)

    fun insert(evaluate: Evaluate): Long = evaluateDao.createEvaluate(evaluate)
    fun delete(evaluate: Evaluate): Completable = evaluateDao.deleteEvaluate(evaluate)
    fun update(evaluate: Evaluate): Completable = evaluateDao.updateEvaluate(evaluate)
}