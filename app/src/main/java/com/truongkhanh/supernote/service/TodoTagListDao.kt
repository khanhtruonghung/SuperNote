package com.truongkhanh.supernote.service

import androidx.room.*
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.model.TodoTagList
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TodoTagListDao {
    @Query("Select b.id, b.color, b.name from Todo_Tag_List as a join Tag_Type as b on a.Tag_Type_ID = b.id where a.Todo_ID = :todoID")
    fun getTagListByTodoID(todoID: Int): Single<MutableList<TagType>>

    @Insert
    fun createTagList(todoTagList: TodoTagList): Completable

    @Insert
    fun insertAll(todoTagList: List<TodoTagList>): LongArray

    @Update
    fun updateTagList(todoTagList: TodoTagList): Completable

    @Delete
    fun deleteTagList(todoTagList: TodoTagList): Completable
}