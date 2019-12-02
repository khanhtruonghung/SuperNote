package com.truongkhanh.supernote.view.createtodo

import android.content.Context
import androidx.lifecycle.*
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.*
import com.truongkhanh.supernote.repository.TodoRepository
import com.truongkhanh.supernote.repository.TodoTagListRepository
import com.truongkhanh.supernote.service.ApplicationDatabase
import com.truongkhanh.supernote.utils.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class CreateTodoViewModel(private val context: Context) : ViewModel() {

    val currentDate = MutableLiveData<MyCalendar>()
    val checkList = MutableLiveData<MutableList<CheckItem>>()
    val startCalendar = MutableLiveData<Calendar>()
    val endCalendar = MutableLiveData<Calendar>()
    val alert = MutableLiveData<Int>()
    val tagList = MutableLiveData<MutableList<TagType>>()
    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val priority = MutableLiveData<Int>().apply {
        postValue(5)
    }
    val isAllDay = MutableLiveData<Boolean>().apply {
        postValue(false)
    }

    private var todoTagListRepository: TodoTagListRepository
    private var todoRepository: TodoRepository
    private val bag = DisposeBag(context as LifecycleOwner)

    val notifyItemInsert: LiveData<Event<Int>> get() = _notifyItemInsert
    private val _notifyItemInsert = MutableLiveData<Event<Int>>()
    val notifyItemDelete: LiveData<Event<Int>> get() = _notifyItemDelete
    private val _notifyItemDelete = MutableLiveData<Event<Int>>()

    val notifyTagInsert: LiveData<Event<Int>> get() = _notifyTagInsert
    private val _notifyTagInsert = MutableLiveData<Event<Int>>()
    val notifyTagDelete: LiveData<Event<Int>> get() = _notifyTagDelete
    private val _notifyTagDelete = MutableLiveData<Event<Int>>()

    val navigateHomeActivity: LiveData<Event<Todo>> get() = _navigateHomeActivity
    private val _navigateHomeActivity = MutableLiveData<Event<Todo>>()

    val messageError: LiveData<Event<String>> get() = _messageError
    private val _messageError = MutableLiveData<Event<String>>()

    init {
        val dbInstance = ApplicationDatabase.getInstance(context)
        todoTagListRepository = TodoTagListRepository(dbInstance.todoTagListDao())
        todoRepository = TodoRepository(dbInstance.todoDao())
    }

    fun addItem(item: CheckItem) {
        checkList.value?.also {
            it.add(item)
            _notifyItemInsert.value = Event(it.size)
        }
    }

    fun removeItem(item: CheckItem) {
        checkList.value?.also {
            var position = 0
            it.forEachIndexed { index, checkItem ->
                if (item.id == checkItem.id)
                    position = index
            }
            if (position != 0) {
                it.removeAt(position)
                _notifyItemDelete.value = Event(position)
            }
        }
    }

    fun addTag(tagType: TagType) {
        tagList.value?.also {
            it.add(tagType)
            _notifyTagInsert.value = Event(it.size)
        }
    }

    fun removeTag(tagType: TagType) {
        tagList.value?.also {
            val position = it.indexOf(tagType)
            if (position != -1) {
                it.removeAt(position)
                _notifyTagDelete.value = Event(position)
            }
        }
    }

    fun saveTodo() {
        Todo(
            0,
            title.value,
            description.value,
            checkList.value?.convertToString(),
            priority.value,
            startCalendar.value?.timeInMillis!!,
            getDefaultDateFormat(startCalendar.value!!),
            endCalendar.value?.timeInMillis!!,
            getDefaultDateFormat(endCalendar.value!!),
            isAllDay.value
        ).also { todo ->
            insertTodo(todo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rowID ->
                    if (tagList.value.isNullOrEmpty()) {
                        _navigateHomeActivity.value = Event(todo)
                    } else {
                        getTodoID(todo, rowID)
                    }
                }, {
                    _messageError.value = Event(context.getString(R.string.lbl_insert_todo_failed))
                }).disposedBy(bag)
        }
    }

    private fun getTodoID(todo: Todo, rowid: Long) {
        todoRepository.getTodoID(rowid)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ todoID ->
                todoID?.let { it1 ->
                    insertTodoTagList(todo, it1)
                }
            }, {
                _messageError.value =
                    Event(context.getString(R.string.lbl_some_thing_went_wrong))
            }).disposedBy(bag)
    }

    private fun insertTodoTagList(todo: Todo, todoID: Int) {
        Single.fromCallable<LongArray> {
            tagList.value?.let {
                todoTagListRepository.insertAll(it.map { it1 ->
                    TodoTagList(todoID, it1.id)
                })
            }
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ listRowID ->
                listRowID?.let {
                    _navigateHomeActivity.value = Event(todo)
                }
            }, {
                _messageError.value =
                    Event(context.getString(R.string.lbl_insert_todo_tag_list_failed))
            }).disposedBy(bag)
    }

    private fun insertTodo(todo: Todo): Single<Long> {
        return todoRepository.insert2(todo)
    }

    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CreateTodoViewModel(context) as T
        }
    }
}