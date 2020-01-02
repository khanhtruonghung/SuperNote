package com.truongkhanh.supernote.view.draftnote.create

import android.content.Context
import androidx.lifecycle.*
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.repository.DraftNoteRepository
import com.truongkhanh.supernote.service.ApplicationDatabase
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.dialog.bottomsheet.MEDIUM_PRIORITY
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CreateNoteViewModel(context: Context) : ViewModel() {
    private val draftNoteRepository: DraftNoteRepository
    private val bag = DisposeBag(context as LifecycleOwner)

    val title = MutableLiveData<String>()
    val content = MutableLiveData<String>()
    val draftNote = MutableLiveData<DraftNote>()
    val priority = MutableLiveData<Int>().apply {
        postValue(MEDIUM_PRIORITY)
    }
    val estimateTotal = MutableLiveData<Int>().apply {
        postValue(DEFAULT_TOTAL_ESTIMATE)
    }
    val estimateDaily = MutableLiveData<Int>().apply {
        postValue(THIRTY_MINUTES)
    }
    val startDate = MutableLiveData<Long>()
    val deadline = MutableLiveData<Long>()

    val navigateBackEvent: LiveData<Event<Boolean>> get() = _navigateBackEvent
    private val _navigateBackEvent = MutableLiveData<Event<Boolean>>()

    init {
        val dbConnection = ApplicationDatabase.getInstance(context)
        draftNoteRepository = DraftNoteRepository(dbConnection.draftNoteDao())
    }

    fun insertNote() {
        val draftNote = DraftNote(
            id = 0,
            title = title.value,
            description = content.value,
            priority = priority.value!!,
            estimateDaily = estimateDaily.value!!,
            estimateTotal = estimateTotal.value!!,
            startDate = startDate.value!!,
            deadline = deadline.value!!
        )
        draftNoteRepository.insert(draftNote)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                _navigateBackEvent.value = Event(true)
            }.disposedBy(bag)
    }

    fun updateNote() {
        val newNote = DraftNote(
            id = draftNote.value?.id ?: 0,
            title = title.value,
            description = content.value,
            priority = priority.value!!,
            estimateDaily = estimateDaily.value!!,
            estimateTotal = estimateTotal.value!!,
            startDate = startDate.value!!,
            deadline = deadline.value!!
        )
        draftNoteRepository.update(newNote)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                _navigateBackEvent.value = Event(true)
            }.disposedBy(bag)
    }

    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CreateNoteViewModel(context) as T
        }
    }
}