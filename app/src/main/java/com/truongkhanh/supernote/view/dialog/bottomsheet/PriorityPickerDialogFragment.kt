package com.truongkhanh.supernote.view.dialog.bottomsheet

import android.graphics.Color
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
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.PRIORITY_BUNDLE
import com.truongkhanh.supernote.utils.THROTTLE_TIME
import com.truongkhanh.supernote.utils.disposedBy
import kotlinx.android.synthetic.main.fragment_bottom_sheet_priority_picker.*
import java.util.concurrent.TimeUnit

class PriorityPickerDialogFragment(private val itemClickListener: (Int) -> Unit) :
    BottomSheetDialogFragment() {

    companion object {
        fun getInstance(itemClickListener: (Int) -> Unit)
                = PriorityPickerDialogFragment(itemClickListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_priority_picker, container, false)
    }

    private val bag = DisposeBag(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetDialog()
        initViewInformation()
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

    private fun initViewInformation() {
        arguments?.let{bundle ->
            setPriority(bundle.getInt(PRIORITY_BUNDLE))
        }
    }

    private fun setPriority(priority: Int) {
        when(priority) {
            HIGH_PRIORITY -> {
                cbHigh.isChecked = true
            }
            MEDIUM_PRIORITY -> {
                cbMedium.isChecked = true
            }
            LOW_PRIORITY -> {
                cbLow.isChecked = true
            }
        }
    }

    private fun initClickListener() {
        RxView.clicks(tvHigh)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                itemClickListener(HIGH_PRIORITY)
                dismiss()
            }.disposedBy(bag)
        RxView.clicks(tvMedium)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                itemClickListener(MEDIUM_PRIORITY)
                dismiss()
            }.disposedBy(bag)
        RxView.clicks(tvLow)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                itemClickListener(LOW_PRIORITY)
                dismiss()
            }.disposedBy(bag)
    }
}

const val HIGH_PRIORITY = 3
const val MEDIUM_PRIORITY = 2
const val LOW_PRIORITY = 1

const val HIGH_TEXT = "H"
const val MEDIUM_TEXT = "M"
const val LOW_TEXT = "L"
const val ERROR_TEXT = "E"