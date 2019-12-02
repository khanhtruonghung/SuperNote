package com.truongkhanh.supernote.repository

import androidx.lifecycle.LiveData
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.service.TagTypeDao

class TagTypeRepository (private val tagTypeDao: TagTypeDao) {
    val listTagType: LiveData<MutableList<TagType>> = tagTypeDao.getTagType()
    fun insert(tagType: TagType) = tagTypeDao.createTagType(tagType)
    fun update(tagType: TagType) = tagTypeDao.updateTagType(tagType)
    fun delete(tagType: TagType) = tagTypeDao.deleteTagType(tagType)
}