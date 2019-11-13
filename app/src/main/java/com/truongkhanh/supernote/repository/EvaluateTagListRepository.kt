package com.truongkhanh.supernote.repository

import androidx.lifecycle.LiveData
import com.truongkhanh.supernote.model.EvaluateTagList
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.service.EvaluateTagListDao
import io.reactivex.Completable


class EvaluateTagListRepository(private val evaluateTagListDao: EvaluateTagListDao) {
    fun getTagList(evaluateTodoID: Int): LiveData<MutableList<TagType>> =
        evaluateTagListDao.getTagList(evaluateTodoID)

    fun insert(evaluateTagList: EvaluateTagList): Long = evaluateTagListDao.insert(evaluateTagList)

    fun insertAll(list: MutableList<EvaluateTagList>): List<Long> = evaluateTagListDao.insertAll(list)

    fun update(evaluateTagList: EvaluateTagList): Completable = evaluateTagListDao.update(evaluateTagList)

    fun delete(evaluateTagList: EvaluateTagList): Completable = evaluateTagListDao.delete(evaluateTagList)
}