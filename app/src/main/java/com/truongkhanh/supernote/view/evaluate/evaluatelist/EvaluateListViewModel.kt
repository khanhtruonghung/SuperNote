package com.truongkhanh.supernote.view.evaluate.evaluatelist

import android.content.Context
import androidx.lifecycle.*
import com.truongkhanh.supernote.model.Evaluate
import com.truongkhanh.supernote.model.enumclass.DAY
import com.truongkhanh.supernote.model.enumclass.MONTH
import com.truongkhanh.supernote.model.enumclass.WEEK
import com.truongkhanh.supernote.repository.EvaluateRepository
import com.truongkhanh.supernote.service.ApplicationDatabase
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.Event
import com.truongkhanh.supernote.utils.NULL_STRING
import com.truongkhanh.supernote.utils.disposedBy
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class EvaluateListViewModel(context: Context) : ViewModel() {
    private val bag = DisposeBag(context as LifecycleOwner)
    private val evaluateRepository: EvaluateRepository

    val showMessage: LiveData<Event<String>> get() = _messageError
    private val _messageError = MutableLiveData<Event<String>>()

    val navigateToCreateEvaluate: LiveData<Event<Evaluate>> get() = _navigateToCreateEvaluate
    private val _navigateToCreateEvaluate = MutableLiveData<Event<Evaluate>>()

    val evaluateList: LiveData<MutableList<Evaluate>>
    val evaluateType = MutableLiveData<Int>().apply {
        postValue(DAY)
    }

    init {
        val dbConnection = ApplicationDatabase.getInstance(context)
        evaluateRepository = EvaluateRepository(dbConnection.evaluateDao())
        evaluateList = evaluateRepository.getAll()
    }

    fun changeEvaluateType() {
        when(evaluateType.value) {
            DAY -> evaluateType.value = WEEK
            WEEK -> evaluateType.value = MONTH
            MONTH -> evaluateType.value = DAY
        }
    }

    fun getEvaluate() {
        when (evaluateType.value) {
            DAY -> {
                getDayEvaluate()
            }
            MONTH -> {
                getMonthEvaluate()
            }
            WEEK -> {
                getWeekEvaluate()
            }
        }
    }

    private fun getDayEvaluate() {
        val calendar = Calendar.getInstance(Locale.getDefault())
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        evaluateRepository.getDayEvaluate(day, month, year)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({evaluate ->
                evaluate?.let{
                    navigateToCreateEvaluate(it)
                }
            },{
                createEvaluate(DAY)
            }).disposedBy(bag)
    }

    private fun getWeekEvaluate() {
        val calendar = Calendar.getInstance(Locale.getDefault())
        val week = calendar.get(Calendar.WEEK_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        evaluateRepository.getWeekEvaluate(week, month, year)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({evaluate ->
                evaluate?.let{
                    navigateToCreateEvaluate(it)
                }
            },{
                createEvaluate(WEEK)
            }).disposedBy(bag)
    }

    private fun getMonthEvaluate() {
        val calendar = Calendar.getInstance(Locale.getDefault())
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        evaluateRepository.getMonthEvaluate(month, year)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({evaluate ->
                evaluate?.let{
                    navigateToCreateEvaluate(it)
                }
            },{
                createEvaluate(MONTH)
            }).disposedBy(bag)
    }

    private fun createEvaluate(type: Int) {
        Single.fromCallable<Long> {
            evaluateRepository.insert(getEvaluate(type))
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({rowid ->
                getEvaluateByRowid(rowid)
            },{
                it.message?.let{message ->
                    _messageError.value = Event(message)
                }
            }).disposedBy(bag)
    }

    private fun getEvaluateByRowid(rowid: Long) {
        evaluateRepository.getEvaluateByRowid(rowid)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                it?.let{evaluate ->
                    navigateToCreateEvaluate(evaluate)
                }
            },{
                it.message?.let{message ->
                    _messageError.value = Event(message)
                }
            }).disposedBy(bag)
    }

    private fun navigateToCreateEvaluate(evaluate: Evaluate) {
        _navigateToCreateEvaluate.value = Event(evaluate)
    }

    private fun getEvaluate(type: Int): Evaluate {
        val calendar = Calendar.getInstance(Locale.getDefault())
        return Evaluate(
            0,
            type,
            calendar.timeInMillis,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.WEEK_OF_MONTH),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.YEAR),
            NULL_STRING,
            NULL_STRING
        )
    }

    class Factory(private val context: Context): ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EvaluateListViewModel(
                context = context
            ) as T
        }
    }
}