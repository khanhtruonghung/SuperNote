package com.truongkhanh.supernote.repository

import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.service.TodoDao
import io.reactivex.Completable
import io.reactivex.Single

class TodoRepository(private val todoDao: TodoDao) {
    fun getTodoInMonth(firstDay: Long, lastDay: Long): Single<MutableList<Todo>>
        = todoDao.getTodoInMonth(firstDay, lastDay)

    fun getTodoByDay(start: Long, end: Long): Single<MutableList<Todo>>
        = todoDao.getTodoByDay(start, end)

    fun insert(todo: Todo): Long = todoDao.createTodo(todo)

    fun insert2(todo: Todo): Single<Long> {
        return Single.fromCallable<Long> {
            todoDao.createTodo(todo)
        }
    }

    fun getTodoByRowid(rowid: Long): Single<Todo> {
        return Single.fromCallable<Todo> {
            todoDao.getTodoByRowid(rowid)
        }
    }

    fun update(todo: Todo): Completable = todoDao.updateTodo(todo)
    fun delete(todo: Todo): Completable = todoDao.deleteTodo(todo)
}