package com.truongkhanh.supernote.service

import androidx.room.*
import com.truongkhanh.supernote.model.Todo
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TodoDao {
    @Query("select * from Todo where End_Date between :firstDay and :lastDay order by Is_All_Day DESC, Start_Date ASC")
    fun getTodoInMonth(firstDay: Long, lastDay: Long): Single<MutableList<Todo>>

    @Query("select * from Todo where End_Date between :start and :end order by Start_Date")
    fun getTodoByDay(start: Long, end: Long): Single<MutableList<Todo>>

    @Insert
    fun createTodo(todo: Todo): Long

    @Query("select id from Todo where rowid = :rowid")
    fun getTodoID(rowid: Long): Int

    @Update
    fun updateTodo(todo: Todo): Completable

    @Delete
    fun deleteTodo(todo: Todo): Completable
}