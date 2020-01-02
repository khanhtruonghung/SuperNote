package com.truongkhanh.supernote.view.draftnote.list

import android.content.Context
import androidx.lifecycle.*
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.repository.DraftNoteRepository
import com.truongkhanh.supernote.service.ApplicationDatabase
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.Event
import com.truongkhanh.supernote.utils.disposedBy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DraftListViewModel(private val context: Context) : ViewModel() {

    private val bag = DisposeBag(context as LifecycleOwner)
    private val draftNoteRepository: DraftNoteRepository

    val listNote = MutableLiveData<MutableList<DraftNote>>()

    val showMessage: LiveData<Event<String>> get() = _showMessage
    private val _showMessage = MutableLiveData<Event<String>>()

    init {
        val dbConnection = ApplicationDatabase.getInstance(context)
        draftNoteRepository = DraftNoteRepository(dbConnection.draftNoteDao())
    }

    fun getListNote() {
        draftNoteRepository.getAll()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({data ->
                listNote.postValue(data)
            },{
                it.message?.let{message ->
                    _showMessage.value = Event(message)
                }
            }).disposedBy(bag)
    }

    fun delete(draftNote: DraftNote) {
        draftNoteRepository.delete(draftNote)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                getListNote()
            }.disposedBy(bag)
    }
    class Factory(private val context: Context): ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DraftListViewModel(context) as T
        }
    }
}