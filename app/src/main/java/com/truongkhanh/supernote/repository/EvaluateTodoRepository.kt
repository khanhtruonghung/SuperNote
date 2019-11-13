package com.truongkhanh.supernote.repository

import androidx.lifecycle.LiveData
import com.truongkhanh.supernote.model.EvaluateTodo
import com.truongkhanh.supernote.service.EvaluateTodoDao
import io.reactivex.Completable
import io.reactivex.Single

class EvaluateTodoRepository(private val evaluateTodoDao: EvaluateTodoDao) {
    fun getAll(): LiveData<MutableList<EvaluateTodo>> = evaluateTodoDao.getEvaluateTodo()

    fun insert(evaluateTodo: EvaluateTodo): Long = evaluateTodoDao.createEvaluateTodo(evaluateTodo)

    fun insertAll(list: MutableList<EvaluateTodo>): List<Long> = evaluateTodoDao.insertAll(list)

    fun update(evaluateTodo: EvaluateTodo): Completable = evaluateTodoDao.updateEvaluateTodo(evaluateTodo)

    fun delete(evaluateTodo: EvaluateTodo): Completable = evaluateTodoDao.deleteEvaluateTodo(evaluateTodo)

    fun getID(rowid: Long): Int = evaluateTodoDao.getTodoID(rowid)
}