package com.truongkhanh.supernote.view.mainhome

import android.content.Context
import androidx.lifecycle.*
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.repository.TodoRepository
import com.truongkhanh.supernote.repository.TodoTagListRepository
import com.truongkhanh.supernote.service.ApplicationDatabase
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.Event
import com.truongkhanh.supernote.utils.disposedBy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class HomeViewModel(private val context: Context) : ViewModel() {

    private val todoRepository: TodoRepository
    private val todoTagListRepository: TodoTagListRepository
    private val bag = DisposeBag(context as LifecycleOwner)

    val messageError: LiveData<Event<String>> get() = _messageError
    private var _messageError = MutableLiveData<Event<String>>()
    val notifyDataChanged: LiveData<Event<Todo>> get() = _notifyDataChanged
    private var _notifyDataChanged = MutableLiveData<Event<Todo>>()

    val todoListInMonth = MutableLiveData<MutableList<Todo>>()
    val dateSelected = MutableLiveData<MyCalendar>()
    val detailTodoData = MutableLiveData<Pair<Todo, MutableList<TagType>?>>()

    init {
        val dbInstance = ApplicationDatabase.getInstance(context)
        val todoDao = dbInstance.todoDao()
        todoRepository = TodoRepository(todoDao)
        val todoTagListDao = dbInstance.todoTagListDao()
        todoTagListRepository = TodoTagListRepository(todoTagListDao)
    }

    fun getTodoByMonthOfYear(calendar: MyCalendar) {
        val firstDay = getFirstDay(calendar)
        val lastDay = getLastDay(calendar)
        val calendar1 = Calendar.getInstance(Locale.getDefault())
        calendar1.timeInMillis = firstDay
        val calendar2 = Calendar.getInstance(Locale.getDefault())
        calendar2.timeInMillis = lastDay
        todoRepository.getTodoInMonth(firstDay, lastDay)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({data ->
                data?.let{
                    todoListInMonth.postValue(it)
                }
            },{
                it.message?.let{error ->
                    _messageError.value = Event(error)
                }
            }).disposedBy(bag)
    }

    fun getTagList(todo: Todo) {
        val id = todo.id
        todoTagListRepository.getTagByTodoID(id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({tagList ->
                detailTodoData.postValue(Pair(todo, tagList))
            },{
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

    class Factory(private val context: Context): ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HomeViewModel(context) as T
        }
    }
}