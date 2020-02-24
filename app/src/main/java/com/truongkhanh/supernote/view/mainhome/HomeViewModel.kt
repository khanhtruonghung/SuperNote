package com.truongkhanh.supernote.view.mainhome

import android.content.Context
import androidx.lifecycle.*
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.repository.DraftNoteRepository
import com.truongkhanh.supernote.repository.TodoRepository
import com.truongkhanh.supernote.repository.TodoTagListRepository
import com.truongkhanh.supernote.service.ApplicationDatabase
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.dialog.bottomsheet.MEDIUM_PRIORITY
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class HomeViewModel(private val context: Context) : ViewModel() {

    private val todoRepository: TodoRepository
    private val todoTagListRepository: TodoTagListRepository
    private val draftNoteRepository: DraftNoteRepository
    private val bag = DisposeBag(context as LifecycleOwner)

    val messageError: LiveData<Event<String>> get() = _messageError
    private var _messageError = MutableLiveData<Event<String>>()

    val notifyDataChanged: LiveData<Event<Todo>> get() = _notifyDataChanged
    private var _notifyDataChanged = MutableLiveData<Event<Todo>>()

    val insertDraftNoteComplete: LiveData<Event<String>> get() = _insertDraftNoteComplete
    private var _insertDraftNoteComplete = MutableLiveData<Event<String>>()

    val todoListInMonth = MutableLiveData<MutableList<Todo>>()
    val dateSelected = MutableLiveData<MyCalendar>()
    val detailTodoData = MutableLiveData<Pair<Todo, MutableList<TagType>?>>()

    //Draft Note attributes
    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val priority = MutableLiveData<Int>().apply {
        postValue(MEDIUM_PRIORITY)
    }
    val estimateTotal = MutableLiveData<Float>().apply {
        postValue(DEFAULT_TOTAL_ESTIMATE)
    }
    val estimateDaily = MutableLiveData<Int>().apply {
        postValue(THIRTY_MINUTES)
    }
    val startDate = MutableLiveData<Long>()
    val deadline = MutableLiveData<Long>()

    init {
        val dbInstance = ApplicationDatabase.getInstance(context)
        val todoDao = dbInstance.todoDao()
        todoRepository = TodoRepository(todoDao)
        val todoTagListDao = dbInstance.todoTagListDao()
        todoTagListRepository = TodoTagListRepository(todoTagListDao)
        val draftNoteDao = dbInstance.draftNoteDao()
        draftNoteRepository = DraftNoteRepository(draftNoteDao)
    }

    fun getTodoByMonthOfYear(calendar: MyCalendar) {
        val firstDay = getFirstDay(calendar)
        val lastDay = getLastDay(calendar)
        todoRepository.getTodoInMonth(firstDay, lastDay)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ data ->
                data?.let {
                    todoListInMonth.postValue(it)
                }
            }, {
                it.message?.let { error ->
                    _messageError.value = Event(error)
                }
            }).disposedBy(bag)
    }

    fun getTagList(todo: Todo) {
        val id = todo.id
        todoTagListRepository.getTagByTodoID(id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ tagList ->
                detailTodoData.postValue(Pair(todo, tagList))
            }, {
                _messageError.value = Event(context.getString(R.string.lbl_some_thing_went_wrong))
            }).disposedBy(bag)
    }

    fun updateTodo(todo: Todo) {
        todoRepository.update(todo)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                _notifyDataChanged.value = Event(todo)
                _messageError.value = Event("Updated")
            }.disposedBy(bag)
    }

    fun insertDraftNote() {
        val draftNote = DraftNote(
            id = 0,
            title = title.value,
            description = description.value,
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
                _insertDraftNoteComplete.value =
                    Event(context.getString(R.string.lbl_insert_draft_note_complete))
            }.disposedBy(bag)
    }

    fun removeTodo(newTodo: Todo) {
        var position = -1
        todoListInMonth.value?.forEachIndexed { index, todo ->
            if (todo.id == newTodo.id)
                position = index
        }
        if (position > -1)
            todoListInMonth.value?.removeAt(position)
    }

    private fun getFirstDay(myCalendar: MyCalendar): Long {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.set(myCalendar.year, myCalendar.month, 1)
        return calendar.timeInMillis
    }

    private fun getLastDay(myCalendar: MyCalendar): Long {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.set(myCalendar.year, myCalendar.month, 1)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        return calendar.timeInMillis
    }

    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HomeViewModel(context) as T
        }
    }
}