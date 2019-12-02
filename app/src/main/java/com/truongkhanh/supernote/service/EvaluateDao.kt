package com.truongkhanh.supernote.service

import androidx.lifecycle.LiveData
import androidx.room.*
import com.truongkhanh.supernote.model.Evaluate
import io.reactivex.Completable

@Dao
interface EvaluateDao {
    @Query("Select * from Evaluate")
    fun getAllEvaluate(): LiveData<MutableList<Evaluate>>

    @Insert
    fun createEvaluate(evaluate: Evaluate): Long

    @Update
    fun updateEvaluate(evaluate: Evaluate): Completable

    @Delete
    fun deleteEvaluate(evaluate: Evaluate): Completable
}