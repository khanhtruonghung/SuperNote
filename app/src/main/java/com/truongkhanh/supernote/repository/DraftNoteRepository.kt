package com.truongkhanh.supernote.repository

import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.service.DraftNoteDao
import io.reactivex.Completable
import io.reactivex.Single

class DraftNoteRepository(private val draftNoteDao: DraftNoteDao) {
    fun getAll(): Single<MutableList<DraftNote>> = draftNoteDao.getAllDraftNote()
    fun insert(draftNote: DraftNote): Completable = draftNoteDao.createDraftNote(draftNote)
    fun update(draftNote: DraftNote): Completable = draftNoteDao.updateDraftNote(draftNote)
    fun delete(draftNote: DraftNote): Completable = draftNoteDao.deleteDraftNote(draftNote)
}