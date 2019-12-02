package com.truongkhanh.supernote.service

import androidx.lifecycle.LiveData
import androidx.room.*
import com.truongkhanh.supernote.model.EvaluateList
import io.reactivex.Completable

@Dao
interface EvaluateListDao {
    @Query("Select * from Evaluate_List where Evaluate_ID == :evaluateID")
    fun getEvaluateList(evaluateID: Int): LiveData<List<EvaluateList>>

    @Insert
    fun createEvaluateList(evaluateList: EvaluateList): Completable

    @Update
    fun updateEvaluateList(evaluateList: EvaluateList): Completable

    @Delete
    fun deleteEvaluateList(evaluateList: EvaluateList): Completable
}