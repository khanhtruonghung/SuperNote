package com.truongkhanh.supernote.view.planning

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseFragment
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.dialog.bottomsheet.DatePickerDialogFragment
import com.truongkhanh.supernote.view.draftnote.list.adapter.DraftListAdapter
import com.truongkhanh.supernote.view.mainhome.HomeActivity
import kotlinx.android.synthetic.main.fragment_planning.*
import kotlinx.android.synthetic.main.layout_toolbar_light.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.intentFor
import java.util.*
import java.util.concurrent.TimeUnit

class PlanningFragment : BaseFragment() {

    companion object {
        fun getInstance() = PlanningFragment()
    }

    interface NavigationListener {
        fun navigateToCreateNote(draftNote: DraftNote?)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_planning, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NavigationListener)
            listener = context
    }

    private lateinit var planningViewModel: PlanningViewModel
    private lateinit var listener: NavigationListener
    private val bag = DisposeBag(this)
    private val animation = CustomAnimation()
    private var isCollapse = false

    private lateinit var adapter: DraftListAdapter

    override fun setUpView(view: View, savedInstanceState: Bundle?) {
        setUpViewInformation()
        bindingViewModel()
        setUpClickEvent()
        setUpRecyclerView()
    }

    private var longClickListener: (Pair<View, DraftNote>) -> Unit = {pair ->
        showMenu(pair)
    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvDraftNote.layoutManager = layoutManager
        adapter = DraftListAdapter(longClickListener) {draftNote ->
            listener.navigateToCreateNote(draftNote)
        }
        rvDraftNote.adapter = adapter
        planningViewModel.getDraftNote()
    }

    private fun bindingViewModel() {
        val activity = activity ?: return
        planningViewModel = ViewModelProviders
            .of(activity, getPlanningViewModelFactory(activity))
            .get(PlanningViewModel::class.java)

        planningViewModel.listDraftNote.observe(this, Observer {listDraftNote ->
            adapter.submitList(listDraftNote)
        })
        planningViewModel.messageError.observe(this, Observer {
            it.getContentIfNotHandled()?.let{message ->
                view?.snackbar(message)
            }
        })
        planningViewModel.enableProgressBar.observe(this, Observer {
            it.getContentIfNotHandled()?.let{enable ->
                progressBar.visibility = getEnable(enable)
            }
        })
        planningViewModel.navigateToHomeActivity.observe(this, Observer {_ ->
            context?.startActivity(context?.intentFor<HomeActivity>())
        })
    }

    private fun setUpViewInformation() {
        tvCurrentDate.text = context?.getString(R.string.lbl_planning_draft_note)
        btnToday.setImageResource(R.drawable.ic_check_black_24dp)

        tvStartDay.text = getDateFormat(Calendar.getInstance(Locale.getDefault()))
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.add(Calendar.DAY_OF_MONTH, 3)
        tvEndDay.text = getDateFormat(calendar)

        startTimePicker.setIs24HourView(true)
        endTimePicker.setIs24HourView(true)
        if (Build.VERSION.SDK_INT >= 23) {
            startTimePicker.hour = 7
            startTimePicker.minute = 0
        } else {
            startTimePicker.currentHour = 7
            startTimePicker.currentMinute = 0
        }
        if (Build.VERSION.SDK_INT >= 23) {
            endTimePicker.hour = 18
            endTimePicker.minute = 0
        } else {
            endTimePicker.currentHour = 18
            endTimePicker.currentMinute = 0
        }
    }

    private fun setUpClickEvent() {
        RxView.clicks(btnDropDown)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                isCollapse = if (isCollapse) {
                    animation.expand(optimizeBar, clOptimize)
                    btnDropDown.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp)
                    false
                } else {
                    animation.collapse(optimizeBar, clOptimize)
                    btnDropDown.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)
                    true
                }
            }.disposedBy(bag)
        RxView.clicks(btnNavigation)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                activity?.finish()
            }.disposedBy(bag)
        RxView.clicks(btnToday)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                planningViewModel.prepare(getStartTime(), getEndTime())
            }.disposedBy(bag)
        RxView.clicks(tvStartDay)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                showDatePickerDialogFragment(DATE_PICKER_DIALOG_FRAGMENT_START_DATE_TAG, planningViewModel.startDate.value) {
                    tvStartDay.text = getDateFormat(it)
                    planningViewModel.startDate.postValue(it)
                }
            }.disposedBy(bag)
        RxView.clicks(tvEndDay)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                showDatePickerDialogFragment(DATE_PICKER_DIALOG_FRAGMENT_DEADLINE_TAG, planningViewModel.endDate.value) {
                    tvEndDay.text = getDateFormat(it)
                    planningViewModel.endDate.postValue(it)
                }
            }.disposedBy(bag)
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

    private fun getStartTime(): MyCalendar {
        val myCalendar = MyCalendar(0,0,0,0,0)
        if (Build.VERSION.SDK_INT >= 23) {
            myCalendar.hour = startTimePicker.hour
            myCalendar.minute = startTimePicker.minute
        } else {
            myCalendar.hour = startTimePicker.currentHour
            myCalendar.minute = startTimePicker.currentMinute
        }
        return myCalendar
    }

    private fun getEndTime(): MyCalendar {
        val myCalendar = MyCalendar(0,0,0,0,0)
        if (Build.VERSION.SDK_INT >= 23) {
            myCalendar.hour = endTimePicker.hour
            myCalendar.minute = endTimePicker.minute
        } else {
            myCalendar.hour = endTimePicker.currentHour
            myCalendar.minute = endTimePicker.currentMinute
        }
        return myCalendar
    }

    private fun showMenu(pair: Pair<View, DraftNote>) {
        val context = context ?: return
        val popup = PopupMenu(context, pair.first, Gravity.END)
        popup.menuInflater.inflate(R.menu.menu_delete_note, popup.menu)
        popup.setOnMenuItemClickListener {
            planningViewModel.delete(pair.second)
            true
        }
        popup.show()
    }
}