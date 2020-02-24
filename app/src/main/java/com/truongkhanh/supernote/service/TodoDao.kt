package com.truongkhanh.supernote.service

import androidx.room.*
import com.truongkhanh.supernote.model.Todo
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface TodoDao {
    @Query("select * from Todo where End_Date between :firstDay and :lastDay order by Start_Date ASC")
    fun getTodoInMonth(firstDay: Long, lastDay: Long): Single<MutableList<Todo>>

    @Query("select * from Todo where End_Date between :start and :end order by Start_Date")
    fun getTodoByDay(start: Long, end: Long): Single<MutableList<Todo>>

    @Insert
    fun createTodo(todo: Todo): Long

    @Insert
    fun insertAll(listTodo: MutableList<Todo>): MutableList<Long>

    @Query("select * from Todo where rowid = :rowid")
    fun getTodoByRowid(rowid: Long): Todo

    @Update
    fun updateTodo(todo: Todo): Completable

    @Delete
    fun deleteTodo(todo: Todo): Completable
}