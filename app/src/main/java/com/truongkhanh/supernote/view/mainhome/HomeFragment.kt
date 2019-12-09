package com.truongkhanh.supernote.view.mainhome

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.Calendar.Scheme
import com.haibin.calendarview.CalendarView
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.musicapplication.base.BaseFragment
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.dialog.bottomsheet.DetailTodoDialogFragment
import com.truongkhanh.supernote.view.mainhome.adapter.TodoAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_draft_note_navigation_view.*
import kotlinx.android.synthetic.main.layout_menu_navigation_view.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.jetbrains.anko.design.snackbar
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
                homeViewModel.dateSelected.postValue(getMyCalendar(it))
            }
        }

        override fun onCalendarOutOfRange(calendar: Calendar?) = Unit
    }
    private val checkBoxListener: (Todo) -> Unit = {
        homeViewModel.updateTodo(it)
    }
    private lateinit var listener: InteractionListener
    private val bag = DisposeBag(this)
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var todoAdapter: TodoAdapter

    interface InteractionListener {
        fun navigateToCreateTodo(calendar: MyCalendar?)
        fun navigateToEvaluateList()
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
        initViewInformation()
        initNavigationDrawer()
        bindingViewModel()
        initRecyclerView(view.context)
        initClickListener()
        setupCalendarView()
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
        RxView.clicks(btnBack)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                fragmentHomeDrawerLayout.closeDrawer(nvDraftNote)
            }.disposedBy(bag)
        RxView.clicks(btnSave)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                fragmentHomeDrawerLayout.closeDrawer(nvDraftNote)
            }.disposedBy(bag)
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
        homeViewModel.notifyDataChanged.observe(this, Observer {event ->
            event.getContentIfNotHandled()?.let{todo ->
                todoAdapter.notifyItem(todo)
            }
        })
    }

    private fun addScheme(listTodo: MutableList<Todo>) {
        val map: Map<String, Calendar> = HashMap()
        listTodo.forEach {
            val startCalendar = java.util.Calendar.getInstance(Locale.getDefault())
            startCalendar.timeInMillis = it.startDate
            var startDay = startCalendar.get(java.util.Calendar.DAY_OF_MONTH)
            val endCalendar = java.util.Calendar.getInstance(Locale.getDefault())
            endCalendar.timeInMillis = it.endDate
            val endDay = endCalendar.get(java.util.Calendar.DAY_OF_MONTH)

            while (startDay <= endDay) {
                val month = startCalendar.get(java.util.Calendar.MONTH)
                val year = startCalendar.get(java.util.Calendar.YEAR)
                map.plus(
                    Pair(
                        it.title.plus(startDay),
                        getSchemeCalendar(
                            year,
                            month,
                            startDay,
                            getColor("#33B5E5"),
                            it.title.toString()
                        )
                    )
                )
                startCalendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                startDay = startCalendar.get(java.util.Calendar.DAY_OF_MONTH)
            }
        }
        calendarView.setSchemeDate(map)
    }

    private fun getSchemeCalendar(
        year: Int,
        month: Int,
        day: Int,
        color: Int,
        text: String
    ): Calendar {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.schemeColor = color
        calendar.scheme = text
        calendar.addScheme(Scheme())
        calendar.addScheme(color, "假")
        calendar.addScheme(color, "节")
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
        RxView.clicks(btnEvaluate)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                listener.navigateToEvaluateList()
            }.disposedBy(bag)
    }

    private fun setupCalendarView() {
        calendarView.setOnCalendarSelectListener(calendarListener)
    }

    private fun softTodoListByDay(calendar: Calendar) {
        todoAdapter.filter.filter(calendar.day.toString())
    }

    private fun showDetailTodoDialog(todo: Todo, tagList: MutableList<TagType>?) {
        fragmentManager?.let {
            val detailTodoDialogFragment = DetailTodoDialogFragment.getInstance()
            val bundle = Bundle()
            bundle.putParcelable(TODO_BUNDLE, todo)
            todo.checkList?.convertFromString()?.let { checkList ->
                bundle.putParcelableArrayList(CHECK_LIST_BUNDLE, ArrayList(checkList))
            }
            tagList?.let { it1 ->
                bundle.putParcelableArrayList(TODO_TAG_LIST_BUNDLE, ArrayList(it1))
            }
            detailTodoDialogFragment.arguments = bundle
            detailTodoDialogFragment.show(it, DETAIL_TODO_DIALOG_FRAGMENT_TAG)
        }
    }

    private fun scrollCalendarViewToCurrentDay() {
        calendarView.scrollToCurrent()
    }

    private fun enableEmptyView(enable: Boolean) {
        rlEmptyView.visibility = getEnable(enable)
    }

    override fun notifyFiltered() {
        enableEmptyView(false)
    }
}
