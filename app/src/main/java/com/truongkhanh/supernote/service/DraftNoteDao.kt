package com.truongkhanh.supernote.service

import androidx.lifecycle.LiveData
import androidx.room.*
import com.truongkhanh.supernote.model.DraftNote
import io.reactivex.Completable

@Dao
interface DraftNoteDao {
    @Query("Select * from Draft_Note")
    fun getAllDraftNote(): LiveData<List<DraftNote>>

    @Insert
    fun createDraftNote(draftNote: DraftNote): Completable

    @Update
    fun updateDraftNote(draftNote: DraftNote): Completable

    @Delete
    fun deleteDraftNote(draftNote: DraftNote): Completable
}