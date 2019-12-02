package com.truongkhanh.supernote.repository

import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.model.TodoTagList
import com.truongkhanh.supernote.service.TodoTagListDao
import io.reactivex.Completable
import io.reactivex.Single

class TodoTagListRepository(private val todoTagListDao: TodoTagListDao) {
    fun getTagByTodoID(todoID: Int): Single<MutableList<TagType>>
        = todoTagListDao.getTagListByTodoID(todoID)

    fun insert(todoTagList: TodoTagList): Completable = todoTagListDao.createTagList(todoTagList)
    fun insertAll(todoTagList: List<TodoTagList>): LongArray = todoTagListDao.insertAll(todoTagList)

    fun update(todoTagList: TodoTagList): Completable = todoTagListDao.updateTagList(todoTagList)
    fun delete(todoTagList: TodoTagList): Completable = todoTagListDao.deleteTagList(todoTagList)
}