package com.truongkhanh.supernote.service

import androidx.lifecycle.LiveData
import androidx.room.*
import com.truongkhanh.supernote.model.EvaluateTodo
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface EvaluateTodoDao {
    @Query("Select * from Evaluate_Todo")
    fun getEvaluateTodo(): LiveData<MutableList<EvaluateTodo>>

    @Insert
    fun createEvaluateTodo(evaluateTodo: EvaluateTodo): Long

    @Insert
    fun insertAll(list: MutableList<EvaluateTodo>): List<Long>

    @Update
    fun updateEvaluateTodo(evaluateTodo: EvaluateTodo): Completable

    @Delete
    fun deleteEvaluateTodo(evaluateTodo: EvaluateTodo): Completable

    @Query("select id from Evaluate_Todo where rowid = :rowid")
    fun getTodoID(rowid: Long): Int
}