package com.truongkhanh.supernote.view.createtodo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseFragment
import com.truongkhanh.supernote.model.CheckItem
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.model.ScheduleItem
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.createtodo.adapter.CheckListAdapter
import com.truongkhanh.supernote.view.createtodo.adapter.ScheduleListAdapter
import com.truongkhanh.supernote.view.createtodo.adapter.TagListAdapter
import com.truongkhanh.supernote.view.dialog.bottomsheet.*
import kotlinx.android.synthetic.main.fragment_create_todo.*
import kotlinx.android.synthetic.main.layout_todo.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.jetbrains.anko.design.snackbar
import java.util.*
import java.util.concurrent.TimeUnit

class CreateTodoFragment : BaseFragment() {

    companion object {
        fun getInstance() = CreateTodoFragment()
    }

    private val bag = DisposeBag(this)

    private lateinit var checkListAdapter: CheckListAdapter
    private lateinit var tagListAdapter: TagListAdapter
    private lateinit var scheduleListAdapter: ScheduleListAdapter
    private lateinit var createTodoViewModel: CreateTodoViewModel
    private val itemCheckedListener: (CheckItem) -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_todo, container, false)
    }

    override fun setUpView(view: View, savedInstanceState: Bundle?) {
        prepareView()
        bindingViewModel()
        initRecyclerView()
        initListener()
        getDataFromBundle()
    }

    private fun prepareView() {
        btnToday.visibility = View.GONE
    }

    private fun getDataFromBundle() {
        arguments?.let { bundle ->
            bundle.getParcelable<MyCalendar>(MY_CALENDAR_BUNDLE)?.let {
                createTodoViewModel.currentDate.postValue(it)
            }
        }
    }

    private fun bindingViewModel() {
        createViewModel()
        createTodoViewModel.checkList.observe(this, Observer {
            checkListAdapter.submitList(it)
        })
        createTodoViewModel.tagList.observe(this, Observer {
            tagListAdapter.submitList(it)
        })
        createTodoViewModel.notifyItemInsert.observe(this, Observer {
            it.getContentIfNotHandled()?.let { position ->
                checkListAdapter.notifyItemInserted(position)
            }
        })
        createTodoViewModel.notifyItemDelete.observe(this, Observer {
            it.getContentIfNotHandled()?.let { position ->
                checkListAdapter.notifyItemRemoved(position)
            }
        })
        createTodoViewModel.notifyTagInsert.observe(this, Observer {
            it.getContentIfNotHandled()?.let { position ->
                tagListAdapter.notifyItemInserted(position)
            }
        })
        createTodoViewModel.notifyTagDelete.observe(this, Observer {
            it.getContentIfNotHandled()?.let { position ->
                tagListAdapter.notifyItemRemoved(position)
            }
        })
        createTodoViewModel.startCalendar.observe(this, Observer { startCalendar ->
            tvStartDate.text = getDateFormat(startCalendar)
            tvCurrentDate.text = getDateFormat(startCalendar)
//            createTodoViewModel.endCalendar.value?.let { end ->
//                if (startCalendar.timeInMillis > end.timeInMillis) {
//                    val calendar = startCalendar.clone() as Calendar
//                    calendar.add(Calendar.HOUR_OF_DAY, 1)
//                    createTodoViewModel.endCalendar.postValue(calendar)
//                }
//            }
            getScheduleItems()?.let {
                createTodoViewModel.scheduleItems.postValue(it)
            }
        })
        createTodoViewModel.endCalendar.observe(this, Observer { endCalendar ->
            tvEndDate.text = getDateFormat(endCalendar)
//            createTodoViewModel.startCalendar.value?.let { start ->
//                if (endCalendar.timeInMillis < start.timeInMillis) {
//                    val calendar = endCalendar.clone() as Calendar
//                    calendar.add(Calendar.HOUR_OF_DAY, -1)
//                    createTodoViewModel.startCalendar.postValue(calendar)
//                }
//            }
            getScheduleItems()?.let {
                createTodoViewModel.scheduleItems.postValue(it)
            }
        })
        createTodoViewModel.alert.observe(this, Observer {
            btnAlert.text = getAlertFormat(it, context!!)
        })
        createTodoViewModel.navigateHomeActivity.observe(this, Observer { event ->
            activity?.finish()
        })
        createTodoViewModel.currentDate.observe(this, Observer { currentDate ->
            setupDefaultTime(currentDate)
        })
        createTodoViewModel.messageError.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { message ->
                view?.snackbar(message)
            }
        })
        createTodoViewModel.scheduleItems.observe(this, Observer { scheduleItems ->
            scheduleListAdapter.submitList(scheduleItems)
        })
    }

    private fun getScheduleItems(): MutableList<ScheduleItem>? {
        createTodoViewModel.startCalendar.value?.let { start ->
            createTodoViewModel.endCalendar.value?.let { end ->
                val scheduleItems: MutableList<ScheduleItem> = mutableListOf()

                val newStart: Calendar = start.clone() as Calendar
                val newEnd: Calendar = end.clone() as Calendar

                var currentDay = newStart.get(Calendar.DAY_OF_MONTH)
                val endDay = newEnd.get(Calendar.DAY_OF_MONTH)

                scheduleItems.add(getScheduleItem(newStart, newEnd))
                while (currentDay != endDay) {
                    newStart.add(Calendar.DAY_OF_MONTH, 1)
                    currentDay = newStart.get(Calendar.DAY_OF_MONTH)
                    scheduleItems.add(getScheduleItem(newStart, newEnd))
                }
                return scheduleItems
            }
        }
        return null
    }

    private fun getScheduleItem(start: Calendar, end: Calendar): ScheduleItem {
        return ScheduleItem(
            start.timeInMillis,
            MyCalendar(
                start.get(Calendar.DAY_OF_MONTH),
                start.get(Calendar.MONTH) + 1,
                0,
                start.get(Calendar.MINUTE),
                start.get(Calendar.HOUR_OF_DAY)
            ),
            MyCalendar(
                end.get(Calendar.DAY_OF_MONTH),
                end.get(Calendar.MONTH) + 1,
                0,
                end.get(Calendar.MINUTE),
                end.get(Calendar.HOUR_OF_DAY)
            )
        )
    }

    private fun createViewModel() {
        val activity = activity ?: return
        createTodoViewModel = ViewModelProviders
            .of(activity, getCreateTodoViewModelFactory(activity))
            .get(CreateTodoViewModel::class.java)
    }

    private fun initListener() {
        clickListener()
        textChangeListener()
    }

    private fun initRecyclerView() {
        checkListRecyclerView()
        tagListRecyclerView()
        scheduleListRecyclerView()
    }

    private fun textChangeListener() {
        RxTextView.textChanges(etTitle)
            .skipInitialValue()
            .subscribe {
                createTodoViewModel.title.postValue(etTitle.text.toString())
            }.disposedBy(bag)
        RxTextView.textChanges(etDescription)
            .skipInitialValue()
            .subscribe {
                createTodoViewModel.description.postValue(etDescription.text.toString())
            }.disposedBy(bag)
    }

    private val endListener: (Calendar) -> Unit = {
        createTodoViewModel.endCalendar.value = it
    }

    private val startListener: (Calendar) -> Unit = {
        createTodoViewModel.startCalendar.value = it
    }

    private fun clickListener() {
        RxView.clicks(btnAddListItem)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                val id = createTodoViewModel.checkList.value?.size ?: 1
                createTodoViewModel.addItem(CheckItem(id, "", false))
            }.disposedBy(bag)
        RxView.clicks(tvStartDate)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                //                showDateTimePickerDialogFragment(
//                    DATE_TIME_PICKER_DIALOG_FRAGMENT_START_DATE_TAG,
//                    createTodoViewModel.startCalendar.value
//                ) {
//                    createTodoViewModel.startCalendar.postValue(it)
//                }
                showDatePickerDialogFragment(
                    DATE_PICKER_DIALOG_FRAGMENT_START_DATE_TAG,
                    createTodoViewModel.startCalendar.value,
                    startListener
                )
            }.disposedBy(bag)
        RxView.clicks(tvEndDate)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                //                showDateTimePickerDialogFragment(
//                    DATE_TIME_PICKER_DIALOG_FRAGMENT_END_DATE_TAG,
//                    createTodoViewModel.endCalendar.value
//                ) {
//                    createTodoViewModel.endCalendar.postValue(it)
//                }
                showDatePickerDialogFragment(
                    DATE_PICKER_DIALOG_FRAGMENT_DEADLINE_TAG,
                    createTodoViewModel.endCalendar.value,
                    endListener
                )
            }.disposedBy(bag)
        RxView.clicks(btnAlert)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                showAlertPickerDialogFragment {
                    createTodoViewModel.alert.postValue(it)
                }
            }.disposedBy(bag)
        RxView.clicks(btnSave)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                createTodoViewModel.saveTodo()
            }.disposedBy(bag)
        RxView.clicks(btnAddTag)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                showTagPickerDialog(TAG_PICKER_DIALOG_FRAGMENT_TAG) { newTagType ->
                    createTodoViewModel.addTag(newTagType)
                }
            }.disposedBy(bag)
        RxView.clicks(btnNavigation)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                activity?.finish()
            }.disposedBy(bag)
        RxView.clicks(btnCancel)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                activity?.finish()
            }.disposedBy(bag)
        RxView.clicks(cbIsAllDay)
            .subscribe {
                createTodoViewModel.isAllDay.postValue(cbIsAllDay.isChecked)
            }.disposedBy(bag)
    }

    private fun tagListRecyclerView() {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvTag.layoutManager = layoutManager
        tagListAdapter = TagListAdapter(null) { tagType ->
            createTodoViewModel.removeTag(tagType)
        }
        rvTag.adapter = tagListAdapter
        createTodoViewModel.tagList.postValue(mutableListOf())
    }

    private fun checkListRecyclerView() {
        val layoutManager = object : LinearLayoutManager(context, VERTICAL, false) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        rvCheckList.layoutManager = layoutManager
        checkListAdapter = CheckListAdapter(bag, itemCheckedListener) {
            createTodoViewModel.removeItem(it)
        }
        rvCheckList.adapter = checkListAdapter
        createTodoViewModel.checkList.postValue(mutableListOf())
    }

    private fun scheduleListRecyclerView() {
        val context = context ?: return
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvScheduleItems.layoutManager = layoutManager
        scheduleListAdapter = ScheduleListAdapter(context) {
            val position = it.first
            val data = it.second
            showSchedulePickerDialogFragment(data) { scheduleItem ->
                createTodoViewModel.scheduleItems.value?.also { scheduleItems ->
                    scheduleItems[position] = scheduleItem
                }
                scheduleListAdapter.notifyItemChanged(position)
            }
        }
        rvScheduleItems.adapter = scheduleListAdapter
    }

    private fun showSchedulePickerDialogFragment(
        scheduleItem: ScheduleItem,
        saveClickListener: (ScheduleItem) -> Unit
    ) {
        activity?.supportFragmentManager?.let {
            val schedulePickerDialogFragment = SchedulePickerDialogFragment(saveClickListener)
            val bundle = Bundle()
            bundle.putParcelable(SCHEDULE_BUNDLE, scheduleItem)
            schedulePickerDialogFragment.arguments = bundle
            schedulePickerDialogFragment.show(it, SCHEDULE_PICKER_DIALOG_FRAGMENT_TAG)
        }
    }

    private fun showAlertPickerDialogFragment(saveClickListener: (Int) -> Unit) {
        activity?.supportFragmentManager?.let {
            val alertPickerDialogFragment =
                AlertPickerDialogFragment.getInstance(saveClickListener)
            alertPickerDialogFragment.show(it, ALERT_TIME_PICKER_DIALOG_FRAGMENT_TAG)
        }
    }

    private fun showDateTimePickerDialogFragment(
        tag: String,
        calendar: Calendar?,
        saveClickListener: (Calendar) -> Unit
    ) {
        activity?.supportFragmentManager?.let { fragmentManager ->
            val dateTimePickerDialogFragment =
                DateTimePickerDialogFragment.getInstance(saveClickListener)
            calendar?.let {
                dateTimePickerDialogFragment.arguments = getMyCalendarBundle(it)
            }
            dateTimePickerDialogFragment.show(fragmentManager, tag)
        }
    }

    private fun showDatePickerDialogFragment(
        tag: String,
        calendar: Calendar?,
        saveClickListener: (Calendar) -> Unit
    ) {
        activity?.supportFragmentManager?.let { fragmentManager ->
            val datePickerDialogFragment = DatePickerDialogFragment.getInstance(saveClickListener)
            calendar?.let {
                datePickerDialogFragment.arguments = getMyCalendarBundle(it)
            }
            datePickerDialogFragment.show(fragmentManager, tag)
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

    private fun showTagPickerDialog(tag: String, saveClickListener: (TagType) -> Unit) {
        activity?.supportFragmentManager?.let { fragmentManager ->
            val tagPickerDialogFragment =
                TagPickerDialogFragment.getInstance(saveClickListener)
            tagPickerDialogFragment.show(fragmentManager, tag)
        }
    }

    private fun setupDefaultTime(currentDate: MyCalendar) {
        setStartCalendar(currentDate)
        setEndCalendar(currentDate)
    }

    private fun setStartCalendar(currentDate: MyCalendar) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentDate.year)
        calendar.set(Calendar.MONTH, currentDate.month)
        calendar.set(Calendar.DAY_OF_MONTH, currentDate.day)
        calendar.let {
            it.set(Calendar.HOUR_OF_DAY, 8)
            it.set(Calendar.MINUTE, 0)
            createTodoViewModel.startCalendar.postValue(it)
        }
        tvCurrentDate.text = getDateFormat(calendar)
    }

    private fun setEndCalendar(currentDate: MyCalendar) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentDate.year)
        calendar.set(Calendar.MONTH, currentDate.month)
        calendar.set(Calendar.DAY_OF_MONTH, currentDate.day)
        calendar.let {
            it.set(Calendar.HOUR_OF_DAY, 9)
            it.set(Calendar.MINUTE, 0)
            createTodoViewModel.endCalendar.postValue(it)
        }
    }
}