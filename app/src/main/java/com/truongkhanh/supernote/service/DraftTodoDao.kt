package com.truongkhanh.supernote.service

import androidx.lifecycle.LiveData
import androidx.room.*
import com.truongkhanh.supernote.model.DraftTodo
import io.reactivex.Completable

@Dao
interface DraftTodoDao {
    @Query("Select * from Draft_Todo")
    fun getAllDraftTodo(): LiveData<List<DraftTodo>>

    @Insert
    fun createDraftTodo(draftTodo: DraftTodo): Completable

    @Update
    fun updateDraftTodo(draftTodo: DraftTodo): Completable

    @Delete
    fun deleteDraftTodo(draftTodo: DraftTodo): Completable
}