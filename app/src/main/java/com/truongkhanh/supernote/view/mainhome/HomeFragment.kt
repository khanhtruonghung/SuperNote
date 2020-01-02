package com.truongkhanh.supernote.view.mainhome

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxAdapterView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseFragment
import com.truongkhanh.supernote.customcalendarview.CalendarMonthView
import com.truongkhanh.supernote.customcalendarview.CalendarWeekView
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.dialog.bottomsheet.*
import com.truongkhanh.supernote.view.mainhome.adapter.TodoAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_draft_note_navigation_view.*
import kotlinx.android.synthetic.main.layout_menu_navigation_view.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.jetbrains.anko.design.snackbar
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class HomeFragment : BaseFragment(), TodoAdapter.NotifyListener {

    companion object {
        fun getInstance() = HomeFragment()
    }

    private var calendarListener = object : CalendarView.OnCalendarSelectListener {
        override fun onCalendarSelect(calendar: Calendar?, isClick: Boolean) {
            calendar?.let {
                homeViewModel.dateSelected.value?.let { dateSelected ->
                    if ((it.month - 1) != dateSelected.month) {
                        enableEmptyView(true)
                        homeViewModel.getTodoByMonthOfYear(getMyCalendar(it))
                    } else {
                        softTodoListByDay(it)
                    }
                }
                view?.snackbar(it.hasScheme().toString())
                homeViewModel.dateSelected.postValue(getMyCalendar(it))
            }
        }

        override fun onCalendarOutOfRange(calendar: Calendar?) = Unit
    }
    private val checkBoxListener: (Todo) -> Unit = {
        cancelNotificationWorker(it)
        homeViewModel.updateTodo(it)
    }

    private lateinit var listener: InteractionListener
    private val bag = DisposeBag(this)
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var todoAdapter: TodoAdapter
    private var isCollapse: Boolean = false
    private val animation: CustomAnimation = CustomAnimation()

    interface InteractionListener {
        fun navigateToCreateTodo(calendar: MyCalendar?)
        fun navigateToEvaluateList()
        fun navigateToDraftList()
        fun navigateToPlanning()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is InteractionListener)
            listener = context
    }

    override fun onResume() {
        super.onResume()
        val currentDate = calendarView.selectedCalendar
        homeViewModel.getTodoByMonthOfYear(getMyCalendar(currentDate))
        homeViewModel.dateSelected.postValue(getMyCalendar(currentDate))
    }

    override fun setUpView(view: View, savedInstanceState: Bundle?) {
        setupCalendarView()
        initViewInformation()
        bindingViewModel()
        initNavigationDrawer()
        initRecyclerView(view.context)
        initClickListener()
    }

    private fun initViewInformation() {
        val context = context ?: return
        Glide.with(context)
            .load(context.getDrawable(R.drawable.ic_uit))
            .apply(RequestOptions.circleCropTransform())
            .into(ivIcon)
    }

    private fun initNavigationDrawer() {
        btnNavigation.setImageResource(R.drawable.ic_menu_black_24dp)
        fragmentHomeDrawerLayout.addDrawerListener(object : ActionBarDrawerToggle(activity, fragmentHomeDrawerLayout, R.string.lbl_open_drawer, R.string.lbl_close_drawer) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                view?.let{ forceCloseKeyboard(it) }
            }
        })
        initInformation()
        bindingView()
        RxView.clicks(btnBack)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                fragmentHomeDrawerLayout.closeDrawer(nvDraftNote)
            }.disposedBy(bag)
        RxView.clicks(btnSave)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                homeViewModel.insertDraftNote()
            }.disposedBy(bag)
        RxView.clicks(tvStartDate)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                showDatePickerDialogFragment(DATE_PICKER_DIALOG_FRAGMENT_START_DATE_TAG, java.util.Calendar.getInstance(Locale.getDefault())) {
                    tvStartDate.text = getDateFormat(it)
                    homeViewModel.startDate.postValue(it.timeInMillis)
                }
            }.disposedBy(bag)
        RxView.clicks(tvDeadline)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                showDatePickerDialogFragment(DATE_PICKER_DIALOG_FRAGMENT_DEADLINE_TAG, java.util.Calendar.getInstance(Locale.getDefault())) {
                    tvDeadline.text = getDateFormat(it)
                    homeViewModel.deadline.postValue(it.timeInMillis)
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
        RxView.clicks(btnPriority)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                val currentPriority = homeViewModel.priority.value ?: MEDIUM_PRIORITY
                showPriorityPickerDialogFragment(currentPriority) {newPriority ->
                    homeViewModel.priority.postValue(newPriority)
                }
            }.disposedBy(bag)
        RxView.clicks(btnEvaluate)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                listener.navigateToEvaluateList()
            }.disposedBy(bag)
        RxView.clicks(btnDraftList)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                listener.navigateToDraftList()
            }.disposedBy(bag)
        RxView.clicks(btnPlanning)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                listener.navigateToPlanning()
            }.disposedBy(bag)
    }

    private fun bindingView() {
        RxTextView.afterTextChangeEvents(etTitle)
            .skipInitialValue()
            .subscribe {
                homeViewModel.title.postValue(etTitle.text.toString())
            }.disposedBy(bag)
        RxTextView.afterTextChangeEvents(etContent)
            .skipInitialValue()
            .subscribe {
                homeViewModel.description.postValue(etContent.text.toString())
            }.disposedBy(bag)
        RxTextView.afterTextChangeEvents(etTotalEstimate)
            .skipInitialValue()
            .subscribe {
                homeViewModel.estimateTotal.postValue(etTotalEstimate.text.toString().toIntOrNull()?:0)
            }.disposedBy(bag)
        RxAdapterView.itemSelections(spinnerDailyEstimate)
            .skipInitialValue()
            .subscribe {
                val estimateDaily = getEstimateDailyMinutes()
                homeViewModel.estimateDaily.postValue(estimateDaily)
            }.disposedBy(bag)
    }

    private fun getEstimateDailyMinutes(): Int {
        return when (spinnerDailyEstimate.selectedItemPosition) {
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
    }

    private fun initInformation() {
        val calendar = java.util.Calendar.getInstance(Locale.getDefault())
        tvStartDate.text = getDateFormat(calendar)
        homeViewModel.startDate.postValue(calendar.timeInMillis)
        calendar.add(java.util.Calendar.DAY_OF_MONTH, DEFAULT_DEADLINE_DATE.toInt())
        tvDeadline.text = getDateFormat(calendar)
        homeViewModel.deadline.postValue(calendar.timeInMillis)

        etTotalEstimate.setText(DEFAULT_TOTAL_ESTIMATE.toString())
    }

    private fun initRecyclerView(context: Context) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvCurrentDayTodo.layoutManager = layoutManager
        todoAdapter = TodoAdapter(context, this, calendarView.curDay, checkBoxListener) {
            homeViewModel.getTagList(it)
        }
        rvCurrentDayTodo.adapter = todoAdapter
    }

    private fun bindingViewModel() {
        val activity = activity ?: return
        homeViewModel = ViewModelProviders
            .of(activity, getHomeViewModelFactory(activity))
            .get(HomeViewModel::class.java)

        homeViewModel.dateSelected.observe(this, Observer {
            tvCurrentDate.text = getDateFormat(getCalendarFromMyCalendar(it))
        })
        homeViewModel.todoListInMonth.observe(this, Observer {
            addScheme(it)
            todoAdapter.setItems(it)
            todoAdapter.filter.filter(calendarView.selectedCalendar.day.toString())
        })
        homeViewModel.messageError.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { message ->
                view?.snackbar(message)
            }
        })
        homeViewModel.detailTodoData.observe(this, Observer { detailTodoData ->
            val todo = detailTodoData.first
            val tagList = detailTodoData.second
            showDetailTodoDialog(todo, tagList)
        })
        homeViewModel.notifyDataChanged.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { todo ->
                todoAdapter.notifyItem(todo)
            }
        })
        homeViewModel.insertDraftNoteComplete.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                view?.snackbar(it)
            }
            clearDraftNoteView()
        })
        homeViewModel.priority.observe(this, Observer {priority ->
            tvPriority.text = getPriority(priority)
        })
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

    private fun clearDraftNoteView() {
        etTitle.setText(NULL_STRING)
        etContent.setText(NULL_STRING)
    }

    private fun addScheme(listTodo: MutableList<Todo>) {
        if (!listTodo.isNullOrEmpty()) {
            val map: MutableMap<String, Calendar> = mutableMapOf()
            listTodo.forEach {
                val startCalendar = java.util.Calendar.getInstance(Locale.getDefault())
                startCalendar.timeInMillis = it.startDate
                var startDay = startCalendar.get(java.util.Calendar.DAY_OF_MONTH)

                val endCalendar = java.util.Calendar.getInstance(Locale.getDefault())
                endCalendar.timeInMillis = it.endDate
                val endDay = endCalendar.get(java.util.Calendar.DAY_OF_MONTH)

                while (startDay <= endDay) {
                    val year: Int = startCalendar.get(java.util.Calendar.YEAR)
                    val month: Int = startCalendar.get(java.util.Calendar.MONTH).plus(1)
                    val schemeCalendar = getSchemeCalendar(
                        year,
                        month,
                        startDay,
                        getColor("#33B5E5"),
                        it.title.toString()
                    )
                    map[schemeCalendar.toString()] = schemeCalendar
                    startCalendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                    startDay = startCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                }
            }
            calendarView.setSchemeDate(map)
        }
    }

    private fun getSchemeCalendar(
        year: Int,
        month: Int,
        day: Int,
        color: Int,
        text: String
    ): Calendar {
        val calendar: Calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.schemeColor = color
        calendar.scheme = text
        return calendar
    }

    private fun initClickListener() {
        RxView.clicks(fbCreateTodo)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                listener.navigateToCreateTodo(homeViewModel.dateSelected.value)
            }.disposedBy(bag)
        RxView.clicks(btnNavigation)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                fragmentHomeDrawerLayout.openDrawer(nvMenu)
            }.disposedBy(bag)
        RxView.clicks(btnToday)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                scrollCalendarViewToCurrentDay()
            }.disposedBy(bag)
    }

    private fun setupCalendarView() {
        calendarView.setMonthView(CalendarMonthView::class.java)
        calendarView.setWeekView(CalendarWeekView::class.java)
        calendarView.setOnCalendarSelectListener(calendarListener)
    }

    private fun softTodoListByDay(calendar: Calendar) {
        todoAdapter.filter.filter(calendar.day.toString())
    }

    private fun showDetailTodoDialog(todo: Todo, tagList: MutableList<TagType>?) {
        fragmentManager?.let {
            val detailTodoDialogFragment = DetailTodoDialogFragment.getInstance { todo ->
                removeTodo(todo)
            }
            val bundle = Bundle()
            bundle.putParcelable(TODO_BUNDLE, todo)
            todo.checkList?.checkItemsFromString()?.let { checkList ->
                bundle.putParcelableArrayList(CHECK_LIST_BUNDLE, ArrayList(checkList))
            }
            tagList?.let { it1 ->
                bundle.putParcelableArrayList(TODO_TAG_LIST_BUNDLE, ArrayList(it1))
            }
            detailTodoDialogFragment.arguments = bundle
            detailTodoDialogFragment.show(it, DETAIL_TODO_DIALOG_FRAGMENT_TAG)
        }
    }

    private fun removeTodo(todo: Todo) {
        homeViewModel.removeTodo(todo)
        todoAdapter.removeTodo(todo)
    }

    private fun scrollCalendarViewToCurrentDay() {
        calendarView.scrollToCurrent()
    }

    private fun enableEmptyView(enable: Boolean) {
        rlEmptyView.visibility = getEnable(enable)
    }

    private fun cancelNotificationWorker(it: Todo) {
        val context = context ?: return
        val requestString = it.notificationRequestID
        if (!requestString.isNullOrBlank()) {
            UUID.fromString(requestString)?.let { uuid ->
                WorkManager.getInstance(context)
                    .cancelWorkById(uuid)
            }
        }
    }

    private fun showDatePickerDialogFragment(
        tag: String,
        calendar: java.util.Calendar?,
        saveClickListener: (java.util.Calendar) -> Unit
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

    private fun getMyCalendarBundle(calendar: java.util.Calendar): Bundle {
        val bundle = Bundle()
        val myCalendar = MyCalendar(
            calendar.get(java.util.Calendar.DAY_OF_MONTH),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MINUTE),
            calendar.get(java.util.Calendar.HOUR_OF_DAY)
        )
        bundle.putParcelable(MY_CALENDAR_BUNDLE, myCalendar)
        return bundle
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

    override fun notifyFiltered() {
        enableEmptyView(false)
    }
}
