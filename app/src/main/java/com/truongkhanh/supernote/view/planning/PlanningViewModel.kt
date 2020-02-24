package com.truongkhanh.supernote.view.planning

import android.content.Context
import androidx.lifecycle.*
import com.google.gson.Gson
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.model.Error
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.repository.DraftNoteRepository
import com.truongkhanh.supernote.repository.TodoRepository
import com.truongkhanh.supernote.service.ApplicationDatabase
import com.truongkhanh.supernote.service.GeneticAlgorithm
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.Event
import com.truongkhanh.supernote.utils.disposedBy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class PlanningViewModel(context: Context) : ViewModel() {

    private val draftNoteRepository: DraftNoteRepository
    private val todoRepository: TodoRepository
    private val bag = DisposeBag(context as LifecycleOwner)

    val messageError: LiveData<Event<String>> get() = _messageError
    private val _messageError = MutableLiveData<Event<String>>()
    val enableProgressBar: LiveData<Event<Boolean>> get() = _enableProgressBar
    private val _enableProgressBar = MutableLiveData<Event<Boolean>>()
    val navigateToHomeActivity: LiveData<Event<Boolean>> get() = _navigateToHomeActivity
    private val _navigateToHomeActivity = MutableLiveData<Event<Boolean>>()

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
        todoRepository = TodoRepository(dbConnection.todoDao())
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

    fun prepare(startTime: MyCalendar, endTime: MyCalendar) {
        val startCalendar = startDate.value ?: return
        val endCalendar = endDate.value ?: return

        todoRepository.getTodoByDay(startCalendar.timeInMillis, endCalendar.timeInMillis)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({data ->
                startGA(startTime, endTime, startCalendar.timeInMillis, endCalendar.timeInMillis, data)
            },{
                it.message?.let{errorJson ->
                    val error = Gson().fromJson<Error>(errorJson, Error::class.java)
                    _messageError.value = Event(error.errorMessage?:"")
                }
            }).disposedBy(bag)
    }

    private fun startGA(startTime: MyCalendar, endTime: MyCalendar, startDate: Long, endDate: Long, todoData: MutableList<Todo>?) {
        val geneticAlgorithm = GeneticAlgorithm()
        val draftNotes = listDraftNote.value ?: return
        _enableProgressBar.value = Event(true)
        geneticAlgorithm.startAlgorithm(
                draftNotes,
                startDate,
                endDate,
                startTime,
                endTime,
                todoData
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({listTodo ->
                listTodo?.forEach{ todo ->
                    todo.id = 0
                }
                insertListTodo(listTodo)
                _enableProgressBar.value = Event(false)
            },{
                it.message?.let{errorJson ->
                    val error = Gson().fromJson<Error>(errorJson, Error::class.java)
                    _messageError.value = Event(error.errorMessage?:"")
                }
                _enableProgressBar.value = Event(false)
            }).disposedBy(bag)
    }

    private fun insertListTodo(listTodo: MutableList<Todo>) {
        _enableProgressBar.value = Event(true)
        todoRepository.inSertAll(listTodo)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                _navigateToHomeActivity.value = Event(true)
                _enableProgressBar.value = Event(false)
            },{
                it.message?.let{messageError ->
                    _messageError.value = Event(messageError)
                }
                _enableProgressBar.value = Event(false)
            }).disposedBy(bag)
    }

    fun delete(draftNote: DraftNote) {
        draftNoteRepository.delete(draftNote)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                getDraftNote()
            }.disposedBy(bag)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PlanningViewModel(context) as T
        }
    }
}