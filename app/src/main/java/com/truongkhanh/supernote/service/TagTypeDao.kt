package com.truongkhanh.supernote.service

import androidx.lifecycle.LiveData
import androidx.room.*
import com.truongkhanh.supernote.model.TagType
import io.reactivex.Completable

@Dao
interface TagTypeDao {
    @Query("Select * from Tag_Type")
    fun getTagType(): LiveData<MutableList<TagType>>

    @Insert
    fun createTagType(tagType: TagType): Completable

    @Update
    fun updateTagType(tagType: TagType): Completable

    @Delete
    fun deleteTagType(tagType: TagType): Completable
}