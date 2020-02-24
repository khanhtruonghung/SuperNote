package com.truongkhanh.supernote.view.dialog.bottomsheet

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.ScheduleItem
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.SCHEDULE_BUNDLE
import com.truongkhanh.supernote.utils.THROTTLE_TIME
import com.truongkhanh.supernote.utils.disposedBy
import kotlinx.android.synthetic.main.fragment_bottom_sheet_schedule_picker.*
import java.util.concurrent.TimeUnit

class SchedulePickerDialogFragment(private val saveClickListener: (ScheduleItem) -> Unit) :
    BottomSheetDialogFragment() {
    companion object {
        fun getInstance(saveClickListener: (ScheduleItem) -> Unit) =
            SchedulePickerDialogFragment(saveClickListener)
    }

    private val bag = DisposeBag(this)
    private var scheduleItem: ScheduleItem = ScheduleItem(0L, null, null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_schedule_picker, container, false)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetDialog()
        getDataFromBundle()
        initEventListener()
    }

    private fun getDataFromBundle() {
        arguments?.let { bundle ->
            bundle.getParcelable<ScheduleItem>(SCHEDULE_BUNDLE)?.let {
                scheduleItem = it
                startPicker.setIs24HourView(true)
                endPicker.setIs24HourView(true)
                if (Build.VERSION.SDK_INT >= 23) {
                    startPicker.hour = it.timeStart?.hour ?: 7
                    startPicker.minute = it.timeStart?.minute ?: 0
                    endPicker.hour = it.timeEnd?.hour ?: 7
                    endPicker.minute = it.timeEnd?.minute ?: 0
                } else {
                    startPicker.currentHour = it.timeStart?.hour ?: 7
                    startPicker.currentMinute = it.timeStart?.minute ?: 0
                    endPicker.currentHour = it.timeEnd?.hour ?: 7
                    endPicker.currentMinute = it.timeEnd?.minute ?: 0
                }
            }
        }
    }

    private fun initEventListener() {
        startPicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            scheduleItem.timeStart?.hour = hourOfDay
            scheduleItem.timeStart?.minute = minute
        }
        endPicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            scheduleItem.timeEnd?.hour = hourOfDay
            scheduleItem.timeEnd?.minute = minute
        }

        RxView.clicks(btnSave)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                saveClickListener(scheduleItem)
                dismiss()
            }.disposedBy(bag)
        RxView.clicks(btnCancel)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                dismiss()
            }.disposedBy(bag)
    }
}