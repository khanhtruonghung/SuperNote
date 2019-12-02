package com.truongkhanh.supernote.service

import androidx.lifecycle.LiveData
import androidx.room.*
import com.truongkhanh.supernote.model.EvaluateTagList
import com.truongkhanh.supernote.model.TagType
import io.reactivex.Completable

@Dao
interface EvaluateTagListDao {
    @Query("Select b.id, b.color, b.name from Evaluate_Tag_List as a join Tag_Type as b on a.Tag_Type_ID = b.id where a.Evaluate_Todo_ID = :evaluateTodoID")
    fun getTagList(evaluateTodoID: Int): LiveData<MutableList<TagType>>

    @Insert
    fun insert(evaluateTagList: EvaluateTagList): Long

    @Insert
    fun insertAll(list: MutableList<EvaluateTagList>): List<Long>

    @Update
    fun update(evaluateTagList: EvaluateTagList): Completable

    @Delete
    fun delete(evaluateTagList: EvaluateTagList): Completable
}