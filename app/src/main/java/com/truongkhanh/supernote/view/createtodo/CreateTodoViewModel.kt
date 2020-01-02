package com.truongkhanh.supernote.view.createtodo

import android.content.Context
import androidx.lifecycle.*
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.*
import com.truongkhanh.supernote.repository.TodoRepository
import com.truongkhanh.supernote.repository.TodoTagListRepository
import com.truongkhanh.supernote.service.ApplicationDatabase
import com.truongkhanh.supernote.service.NotificationWorker
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.dialog.bottomsheet.AlertPickerDialogFragment
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class CreateTodoViewModel(private val context: Context) : ViewModel() {

    val currentDate = MutableLiveData<MyCalendar>()
    val checkList = MutableLiveData<MutableList<CheckItem>>()
    val startCalendar = MutableLiveData<Calendar>()
    val endCalendar = MutableLiveData<Calendar>()
    val alert = MutableLiveData<Int>().apply {
        postValue(AlertPickerDialogFragment.ALERT_10_MINUTE)
    }
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
            id = 0,
            title = title.value,
            description = description.value,
            checkList = checkList.value?.checkItemsToString(),
            priority = priority.value,
            startDate = startCalendar.value?.timeInMillis!!,
            endDate = endCalendar.value?.timeInMillis!!,
            isAllDay = isAllDay.value ?: false,
            alertType = alert.value ?: AlertPickerDialogFragment.NO_ALERT,
            notificationRequestID = NULL_STRING,
            schedule = NULL_STRING
        ).also { todo ->
            insertTodo(todo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rowID ->
                    getTodoByRowid(rowID)
                }, {
                    _messageError.value = Event(context.getString(R.string.lbl_insert_todo_failed))
                }).disposedBy(bag)
        }
    }

    private fun buildNotificationWorker(todo: Todo): String {
        return if (todo.alertType != AlertPickerDialogFragment.NO_ALERT) {
            val data = Data.Builder()
                .putInt(NOTIFICATION_ID_DATA_TAG, todo.id)
                .putString(NOTIFICATION_TITLE_DATA_TAG, todo.title)
                .putString(NOTIFICATION_DESCRIPTION_DATA_TAG, todo.description)
                .build()
            val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInputData(data)
                .setInitialDelay(calculateDelay(todo), TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueue(notificationRequest)
            notificationRequest.id.toString()
        } else {
            NULL_STRING
        }
    }

    private fun getTodoByRowid(rowid: Long) {
        todoRepository.getTodoByRowid(rowid)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ mTodo ->
                mTodo?.let { data ->
                    updateNotificationRequestID(data)
                }
            }, {
                _messageError.value =
                    Event(context.getString(R.string.lbl_some_thing_went_wrong))
            }).disposedBy(bag)
    }

    private fun updateNotificationRequestID(todo: Todo) {
        val requestID = buildNotificationWorker(todo)
        todo.notificationRequestID = requestID
        todoRepository.update(todo)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                val isInsertTodoTags = !tagList.value.isNullOrEmpty()
                if (isInsertTodoTags)
                    insertTodoTagList(todo, todo.id)
                else
                    _navigateHomeActivity.value = Event(todo)
            }.disposedBy(bag)

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

    private fun calculateDelay(todo: Todo): Long {
        val current = Calendar.getInstance(Locale.getDefault())
        current.set(Calendar.SECOND, 0)

        val start = current.clone() as Calendar
        start.timeInMillis = todo.startDate
        start.set(Calendar.SECOND, 0)

        when (todo.alertType) {
            AlertPickerDialogFragment.ALERT_10_MINUTE -> {
                start.add(Calendar.MINUTE, -10)
            }
            AlertPickerDialogFragment.ALERT_30_MINUTE -> {
                start.add(Calendar.MINUTE, -30)
            }
            AlertPickerDialogFragment.ALERT_1_HOUR -> {
                start.add(Calendar.HOUR, -1)
            }
            AlertPickerDialogFragment.ALERT_1_DAY -> {
                start.add(Calendar.DAY_OF_MONTH, -1)
            }
            AlertPickerDialogFragment.ALERT_2_DAY -> {
                start.add(Calendar.DAY_OF_MONTH, -2)
            }
        }

        val delay = (start.timeInMillis - current.timeInMillis)
        return if (delay < 0)
            0L
        else
            delay
    }


    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CreateTodoViewModel(context) as T
        }
    }
}