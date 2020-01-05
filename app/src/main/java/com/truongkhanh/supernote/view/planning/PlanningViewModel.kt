package com.truongkhanh.supernote.view.planning

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.repository.DraftNoteRepository
import com.truongkhanh.supernote.service.ApplicationDatabase
import com.truongkhanh.supernote.service.GeneticAlgorithm
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.Event
import com.truongkhanh.supernote.utils.disposedBy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class PlanningViewModel(context: Context) : ViewModel() {

    private var draftNoteRepository: DraftNoteRepository
    private val bag = DisposeBag(context as LifecycleOwner)

    val messageError: LiveData<Event<String>> get() = _messageError
    private val _messageError = MutableLiveData<Event<String>>()
    val enableProgressBar: LiveData<Event<Boolean>> get() = _enableProgressBar
    private val _enableProgressBar = MutableLiveData<Event<Boolean>>()

    val listDraftNote = MutableLiveData<MutableList<DraftNote>>()

    val startDate = MutableLiveData<Calendar>().apply {
        postValue(Calendar.getInstance(Locale.getDefault()))
    }
    val endDate = MutableLiveData<Calendar>().apply {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.add(Calendar.DAY_OF_MONTH, 3)
        postValue(calendar)
    }

    init {
        val dbConnection = ApplicationDatabase.getInstance(context)
        draftNoteRepository = DraftNoteRepository(dbConnection.draftNoteDao())
    }

    fun getDraftNote() {
        draftNoteRepository.getAll()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ listDraftNote ->
                listDraftNote?.let {
                    this.listDraftNote.postValue(it)
                }
            }, {
                it.message?.let { messageError ->
                    _messageError.value = Event(messageError)
                }
            }).disposedBy(bag)
    }

    fun startGA(startTime: MyCalendar, endTime: MyCalendar) {
        val geneticAlgorithm = GeneticAlgorithm()
        val draftNotes = listDraftNote.value ?: return
        val startCalendar = startDate.value ?: return
        val endCalendar = endDate.value ?: return

        _enableProgressBar.value = Event(true)

        geneticAlgorithm.startAlgorithm(
                draftNotes,
                startCalendar.timeInMillis,
                endCalendar.timeInMillis,
                startTime,
                endTime
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                Log.d("Debuggg", it.toString())
                _enableProgressBar.value = Event(false)
            },{
                it.message?.let{messageError ->
                    _messageError.value = Event(messageError)
                }
                _enableProgressBar.value = Event(false)
            }).disposedBy(bag)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PlanningViewModel(context) as T
        }
    }
}