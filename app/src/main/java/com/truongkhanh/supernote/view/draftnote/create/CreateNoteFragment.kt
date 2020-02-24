package com.truongkhanh.supernote.view.draftnote.create

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxAdapterView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseFragment
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.dialog.bottomsheet.*
import kotlinx.android.synthetic.main.layout_draft_note_navigation_view.*
import java.util.*
import java.util.concurrent.TimeUnit

class CreateNoteFragment : BaseFragment() {
    companion object {
        fun getInstance() = CreateNoteFragment()
    }

    private val bag = DisposeBag(this)
    private lateinit var createNoteViewModel: CreateNoteViewModel
    private var isNewNote: Boolean = false
    private var isCollapse: Boolean = false
    private val animation: CustomAnimation = CustomAnimation()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_draft_note_navigation_view, container, false)
    }

    override fun setUpView(view: View, savedInstanceState: Bundle?) {
        bindingViewModel()
        getDataFromBundle()
        initClickEvent()
        initTextChangeEvent()
    }

    private fun getDataFromBundle() {
        arguments?.getParcelable<DraftNote>(DRAFT_NOTE_BUNDLE)?.let { draftNote ->
            createNoteViewModel.draftNote.postValue(draftNote)
            createNoteViewModel.title.postValue(draftNote.title)
            createNoteViewModel.content.postValue(draftNote.description)
            createNoteViewModel.priority.postValue(draftNote.priority)
            createNoteViewModel.estimateDaily.postValue(draftNote.estimateDaily)
            createNoteViewModel.estimateTotal.postValue(draftNote.estimateTotal)
            createNoteViewModel.startDate.postValue(draftNote.startDate)
            createNoteViewModel.deadline.postValue(draftNote.deadline)
            setView(draftNote)
        } ?: setIsNewNote()
    }

    private fun setView(draftNote: DraftNote) {
        etTitle.setText(draftNote.title ?: NULL_STRING)
        etContent.setText(draftNote.description ?: NULL_STRING)
        etTotalEstimate.setText(draftNote.estimateTotal.toString())
    }

    private fun setIsNewNote() {
        isNewNote = true
        createNoteViewModel.priority.postValue(MEDIUM_PRIORITY)
        createNoteViewModel.estimateDaily.postValue(THIRTY_MINUTES)
        createNoteViewModel.estimateTotal.postValue(3F)
        val calendar = Calendar.getInstance(Locale.getDefault())
        createNoteViewModel.startDate.value = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 9)
        createNoteViewModel.deadline.value = calendar.timeInMillis
    }

    private fun bindingViewModel() {
        val activity = activity ?: return
        createNoteViewModel = ViewModelProviders
            .of(activity, getCreateNoteViewModelFactory(activity))
            .get(CreateNoteViewModel::class.java)

        createNoteViewModel.navigateBackEvent.observe(this, Observer { _ ->
            this.activity?.finish()
        })
        createNoteViewModel.priority.observe(this, Observer {
            tvPriority.text = getPriority(it)
        })
        createNoteViewModel.estimateDaily.observe(this, Observer {
            spinnerDailyEstimate.setSelection(getEstimateDailyIndex(it))
        })
        createNoteViewModel.startDate.observe(this, Observer {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.timeInMillis = it
            tvStartDate.text = getDateFormat(calendar)
        })
        createNoteViewModel.deadline.observe(this, Observer {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.timeInMillis = it
            tvDeadline.text = getDateFormat(calendar)
        })
    }

    private fun getEstimateDailyIndex(estimateDailyTime: Int): Int {
        return when (estimateDailyTime) {
            THIRTY_MINUTES -> 0
            ONE_HOUR -> 1
            ONE_AND_A_HALF_HOURS -> 2
            TWO_HOURS -> 3
            TWO_AND_A_HALF_HOURS -> 4
            THREE_HOURS -> 5
            else -> {
                Log.d("Debuggg", "Cant get spinner daily estimate")
                0
            }
        }
    }

    private fun initClickEvent() {
        RxView.clicks(btnBack)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                activity?.finish()
            }.disposedBy(bag)
        RxView.clicks(btnSave)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                if (isNewNote) {
                    createNoteViewModel.insertNote()
                } else {
                    createNoteViewModel.updateNote()
                }
            }.disposedBy(bag)
        RxView.clicks(btnPriority)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                val currentPriority = createNoteViewModel.priority.value ?: MEDIUM_PRIORITY
                showPriorityPickerDialogFragment(currentPriority) {newPriority ->
                    createNoteViewModel.priority.postValue(newPriority)
                }
            }.disposedBy(bag)
        RxView.clicks(tvStartDate)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                showDatePickerDialogFragment(DATE_PICKER_DIALOG_FRAGMENT_START_DATE_TAG, java.util.Calendar.getInstance(
                    Locale.getDefault())) {
                    tvStartDate.text = getDateFormat(it)
                    createNoteViewModel.startDate.postValue(it.timeInMillis)
                }
            }.disposedBy(bag)
        RxView.clicks(tvDeadline)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                showDatePickerDialogFragment(DATE_PICKER_DIALOG_FRAGMENT_DEADLINE_TAG, java.util.Calendar.getInstance(
                    Locale.getDefault())) {
                    tvDeadline.text = getDateFormat(it)
                    createNoteViewModel.deadline.postValue(it.timeInMillis)
                }
            }.disposedBy(bag)
        RxView.clicks(btnDrop)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                isCollapse = if (isCollapse) {
                    animation.expand(rlPlanningInformation, rlContent)
                    btnDrop.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp)
                    false
                } else {
                    animation.collapse(rlPlanningInformation, rlContent)
                    btnDrop.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)
                    true
                }
            }.disposedBy(bag)
    }

    private fun initTextChangeEvent() {
        RxTextView.textChanges(etTitle)
            .skipInitialValue()
            .subscribe {
                createNoteViewModel.title.postValue(etTitle.text.toString())
            }.disposedBy(bag)
        RxTextView.textChanges(etContent)
            .skipInitialValue()
            .subscribe {
                createNoteViewModel.content.postValue(etContent.text.toString())
            }.disposedBy(bag)
        RxTextView.afterTextChangeEvents(etTotalEstimate)
            .skipInitialValue()
            .subscribe {
                createNoteViewModel.estimateTotal.postValue(etTotalEstimate.text.toString().toFloatOrNull()?:0F)
            }.disposedBy(bag)
        RxAdapterView.itemSelections(spinnerDailyEstimate)
            .skipInitialValue()
            .subscribe {
                val estimateDaily = when (spinnerDailyEstimate.selectedItemPosition) {
                    0 -> THIRTY_MINUTES
                    1 -> ONE_HOUR
                    2 -> ONE_AND_A_HALF_HOURS
                    3 -> TWO_HOURS
                    4 -> TWO_AND_A_HALF_HOURS
                    5 -> THREE_HOURS
                    else -> {
                        Log.d("Debuggg", "Cant get spinner daily estimate")
                        0
                    }
                }
                createNoteViewModel.estimateDaily.postValue(estimateDaily)
            }.disposedBy(bag)
    }

    private fun showPriorityPickerDialogFragment(
        currentPriority: Int?,
        itemClickListener: (Int) -> Unit
    ) {
        activity?.supportFragmentManager?.let { fragmentManager ->
            val priorityPickerDialogFragment = PriorityPickerDialogFragment.getInstance(itemClickListener)
            currentPriority?.let {
                val bundle = Bundle()
                bundle.putInt(PRIORITY_BUNDLE, currentPriority)
                priorityPickerDialogFragment.arguments = bundle
            }
            priorityPickerDialogFragment.show(fragmentManager, PRIORITY_PICKER_DIALOG_FRAGMENT_TAG)
        }
    }

    private fun showDatePickerDialogFragment(
        tag: String,
        calendar: Calendar?,
        saveClickListener: (Calendar) -> Unit
    ) {
        activity?.supportFragmentManager?.let { fragmentManager ->
            val dateTimePickerDialogFragment =
                DatePickerDialogFragment.getInstance(saveClickListener)
            calendar?.let {
                dateTimePickerDialogFragment.arguments = getMyCalendarBundle(it)
            }
            dateTimePickerDialogFragment.show(fragmentManager, tag)
        }
    }

    private fun getMyCalendarBundle(calendar: Calendar): Bundle {
        val bundle = Bundle()
        val myCalendar = MyCalendar(
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.HOUR_OF_DAY)
        )
        bundle.putParcelable(MY_CALENDAR_BUNDLE, myCalendar)
        return bundle
    }

    private fun getPriority(priority: Int): String {
        return when(priority) {
            HIGH_PRIORITY -> {
                HIGH_TEXT
            }
            MEDIUM_PRIORITY -> {
                MEDIUM_TEXT
            }
            LOW_PRIORITY -> {
                LOW_TEXT
            }
            else -> {
                Log.d("Debuggg", "Priority error")
                ERROR_TEXT
            }
        }
    }
}