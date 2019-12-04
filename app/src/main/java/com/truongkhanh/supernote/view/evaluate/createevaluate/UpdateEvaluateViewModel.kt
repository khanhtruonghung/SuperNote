package com.truongkhanh.supernote.view.evaluate.createevaluate

import android.content.Context
import androidx.lifecycle.*
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.Evaluate
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.model.enumclass.DAY
import com.truongkhanh.supernote.model.enumclass.MONTH
import com.truongkhanh.supernote.model.enumclass.WEEK
import com.truongkhanh.supernote.repository.EvaluateRepository
import com.truongkhanh.supernote.repository.TodoRepository
import com.truongkhanh.supernote.repository.TodoTagListRepository
import com.truongkhanh.supernote.service.ApplicationDatabase
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.Event
import com.truongkhanh.supernote.utils.disposedBy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class UpdateEvaluateViewModel(private val context: Context) : ViewModel() {

    private val todoRepository: TodoRepository
    private val todoTagListRepository: TodoTagListRepository
    private val evaluateRepository: EvaluateRepository
    private val bag = DisposeBag(context as LifecycleOwner)

    val messageError: LiveData<Event<String>> get() = _messageError
    private var _messageError = MutableLiveData<Event<String>>()
    val notifyDataChanged: LiveData<Event<Todo>> get() = _notifyDataChanged
    private var _notifyDataChanged = MutableLiveData<Event<Todo>>()

    val detailTodoData = MutableLiveData<Pair<Todo, MutableList<TagType>?>>()
    val todoList = MutableLiveData<MutableList<Todo>>()
    val evaluate = MutableLiveData<Evaluate>()

    init {
        val dbConnection = ApplicationDatabase.getInstance(context)
        todoRepository = TodoRepository(dbConnection.todoDao())
        todoTagListRepository = TodoTagListRepository(dbConnection.todoTagListDao())
        evaluateRepository = EvaluateRepository(dbConnection.evaluateDao())
    }

    fun updateTodo(todo: Todo) {
        todoRepository.update(todo)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe ({
                _notifyDataChanged.value = Event(todo)
                _messageError.value = Event("Updated")
            },{
                _messageError.value = Event(context.getString(R.string.lbl_some_thing_went_wrong))
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

    fun getTodo(evaluate: Evaluate) {
        when (evaluate.evaluateType) {
            DAY -> {
                val startDate = getStartOfDay(evaluate)
                val endDate = getEndOfDay(evaluate)
                todoRepository.getTodoInMonth(startDate, endDate)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({data ->
                        todoList.postValue(data)
                    },{
                        _messageError.value = Event(context.getString(R.string.lbl_some_thing_went_wrong))
                    }).disposedBy(bag)
            }
            WEEK -> {
                val firstDayOfWeek = getDayOfWeek(evaluate, Calendar.MONDAY)
                val lastDayOfWeek = getDayOfWeek(evaluate, Calendar.SUNDAY)
                todoRepository.getTodoInMonth(firstDayOfWeek, lastDayOfWeek)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({data ->
                        todoList.postValue(data)
                    },{
                        _messageError.value = Event(context.getString(R.string.lbl_some_thing_went_wrong))
                    }).disposedBy(bag)
            }
            MONTH -> {
                val firstDayOfMonth = getFirstDayOfMonth(evaluate)
                val lastDayOfMonth = getLastDayOfMonth(evaluate)
                todoRepository.getTodoInMonth(firstDayOfMonth, lastDayOfMonth)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({data ->
                        todoList.postValue(data)
                    },{
                        _messageError.value = Event(context.getString(R.string.lbl_some_thing_went_wrong))
                    }).disposedBy(bag)
            }
        }
    }

    fun updateEvaluate() {
        val evaluate = evaluate.value ?: return
        evaluateRepository.update(evaluate)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                _messageError.value = Event(context.getString(R.string.lbl_update_complete))
            }.disposedBy(bag)
    }

    private fun getEndOfDay(evaluate: Evaluate): Long {
        val day = evaluate.day
        val month = evaluate.month
        val year = evaluate.year
        val endDate = Calendar.getInstance(Locale.getDefault())
        endDate.set(year, month, day, 23, 59)
        return endDate.timeInMillis
    }

    private fun getStartOfDay(evaluate: Evaluate): Long {
        val day = evaluate.day
        val month = evaluate.month
        val year = evaluate.year
        val startDate = Calendar.getInstance(Locale.getDefault())
        startDate.set(year, month, day, 0, 1)
        return startDate.timeInMillis
    }

    private fun getFirstDayOfMonth(evaluate: Evaluate): Long {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.set(evaluate.year, evaluate.month, 1)
        return calendar.timeInMillis
    }

    private fun getLastDayOfMonth(evaluate: Evaluate): Long {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.set(evaluate.year, evaluate.month, 1)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        return calendar.timeInMillis
    }

    private fun getDayOfWeek(evaluate: Evaluate, dayEnum: Int): Long {
        val week = evaluate.week
        val month = evaluate.month
        val year = evaluate.year
        val endDate = Calendar.getInstance(Locale.getDefault())
        endDate.set(Calendar.YEAR, year)
        endDate.set(Calendar.MONTH, month)
        endDate.set(Calendar.WEEK_OF_MONTH, week)
        endDate.set(Calendar.DAY_OF_WEEK, dayEnum)
        return endDate.timeInMillis
    }

    class Factory(private val context: Context): ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return UpdateEvaluateViewModel(context = context) as T
        }
    }
}