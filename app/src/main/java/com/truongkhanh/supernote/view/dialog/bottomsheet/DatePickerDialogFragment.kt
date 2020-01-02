package com.truongkhanh.supernote.view.dialog.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.MY_CALENDAR_BUNDLE
import com.truongkhanh.supernote.utils.disposedBy
import com.truongkhanh.supernote.utils.getDateTimeDialogViewModelFactory
import kotlinx.android.synthetic.main.fragment_bottom_sheet_date_picker.*
import java.util.*

class DatePickerDialogFragment(
    private val saveClickListener: (Calendar) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var dateTimeDialogViewModel: DateTimeDialogViewModel

    companion object {
        fun getInstance(
            saveClickListener: (Calendar) -> Unit
        ) = DatePickerDialogFragment(
            saveClickListener
        )
    }

    private val bag = DisposeBag(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_date_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetDialog()
        bindingViewModel()
        getDataFromBundle()
        initClickListener()
    }

    private fun setupBottomSheetDialog() {
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            val coordinatorLayout = bottomSheet?.parent as CoordinatorLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.peekHeight = bottomSheet.height
            bottomSheet.setBackgroundColor(Color.TRANSPARENT)
            coordinatorLayout.parent.requestLayout()
        }
    }

    private fun getDataFromBundle() {
        arguments?.let { bundle ->
            bundle.getParcelable<MyCalendar>(MY_CALENDAR_BUNDLE)?.let {
                val calendar: Calendar = Calendar.getInstance(Locale.getDefault())
                calendar.set(it.year, it.month, it.day)
                calendar.set(Calendar.HOUR_OF_DAY, it.hour)
                calendar.set(Calendar.MINUTE, it.minute)
                setPickerDate(calendar)
                dateTimeDialogViewModel.calendar.postValue(calendar)
            }
        }
    }

    private fun setPickerDate(calendar: Calendar) {
        calendarPicker.date = calendar.timeInMillis
    }

    private fun bindingViewModel() {
        val activity = activity ?: return
        dateTimeDialogViewModel = ViewModelProviders
            .of(activity, getDateTimeDialogViewModelFactory())
            .get(DateTimeDialogViewModel::class.java)
    }

    private fun initClickListener() {
        calendarPicker.setOnDateChangeListener { _: CalendarView, year: Int, month: Int, day: Int ->
            dateTimeDialogViewModel.calendar.value?.let {
                val calendar = it
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                dateTimeDialogViewModel.calendar.postValue(calendar)
            }
        }
        RxView.clicks(btnCancel)
            .subscribe {
                dismiss()
            }.disposedBy(bag)
        RxView.clicks(btnSave)
            .subscribe {
                saveClickListener(dateTimeDialogViewModel.calendar.value ?: Calendar.getInstance())
                dismiss()
            }.disposedBy(bag)
    }
}