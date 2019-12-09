package com.truongkhanh.supernote.service

import androidx.lifecycle.LiveData
import androidx.room.*
import com.truongkhanh.supernote.model.Evaluate
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface EvaluateDao {
    @Query("Select * from Evaluate")
    fun getAllEvaluate(): LiveData<MutableList<Evaluate>>

    @Query("Select * from Evaluate where Evaluate_Week = :week and Evaluate_Month = :month and Evaluate_Year = :year")
    fun getWeekEvalute(week: Int, month: Int, year: Int): Single<Evaluate>

    @Query("Select * from Evaluate where Evaluate_Month = :month and Evaluate_Year = :year")
    fun getMonthEvaluate(month: Int, year: Int): Single<Evaluate>

    @Query("Select * from Evaluate where Evaluate_Day = :day and Evaluate_Month = :month and Evaluate_Year = :year")
    fun getDayEvaluate(day: Int, month: Int, year: Int): Single<Evaluate?>

    @Query("Select * from Evaluate where rowid = :rowid")
    fun getEvaluateByRowid(rowid: Long): Single<Evaluate>

    @Insert
    fun createEvaluate(evaluate: Evaluate): Long

    @Update
    fun updateEvaluate(evaluate: Evaluate): Completable

    @Delete
    fun deleteEvaluate(evaluate: Evaluate): Completable
}