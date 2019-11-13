package com.truongkhanh.supernote.view.dialog.bottomsheet

import android.content.Context
import androidx.lifecycle.*
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.CheckItem
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.repository.TodoRepository
import com.truongkhanh.supernote.repository.TodoTagListRepository
import com.truongkhanh.supernote.service.ApplicationDatabase
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.Event
import com.truongkhanh.supernote.utils.convertToString
import com.truongkhanh.supernote.utils.disposedBy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DetailTodoDialogViewModel(private val context: Context) : ViewModel() {

    private val todoRepository: TodoRepository
    private val todoTagListRepository: TodoTagListRepository
    private val bag = DisposeBag(context as LifecycleOwner)
    val notifyEvent: LiveData<Event<String>> get() = _notifyEvent
    private val _notifyEvent: MutableLiveData<Event<String>> = MutableLiveData()

    val todo = MutableLiveData<Todo>()
    val checkList = MutableLiveData<MutableList<CheckItem>>()

    init {
        val dbConnection = ApplicationDatabase.getInstance(context)
        todoRepository = TodoRepository(dbConnection.todoDao())
        todoTagListRepository = TodoTagListRepository((dbConnection.todoTagListDao()))
    }

    fun updateTodo(item: CheckItem) {
        todo.value?.let {newTodo ->
            checkList.value?.also { checkList ->
                checkList.forEachIndexed { _, checkItem ->
                    if (item.id == checkItem.id) {
                        checkItem.isChecked = item.isChecked
                    }
                }
                newTodo.checkList = checkList.convertToString()
                todoRepository.update(newTodo)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe {
                        _notifyEvent.value = Event(context.getString(R.string.lbl_todo_updated))
                    }.disposedBy(bag)
            }
        }
    }

    class Factory(private val context: Context): ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DetailTodoDialogViewModel(context) as T
        }
    }
}