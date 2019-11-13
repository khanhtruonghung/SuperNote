package com.truongkhanh.supernote.view.dialog.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.THROTTLE_TIME
import com.truongkhanh.supernote.utils.disposedBy
import kotlinx.android.synthetic.main.fragment_bottom_sheet_alert_picker.*
import java.util.concurrent.TimeUnit

class AlertPickerDialogFragment(
    private val saveClickListener: (Int) -> Unit
) : BottomSheetDialogFragment() {

    companion object {
        fun getInstance(
            saveClickListener: (Int) -> Unit
        ) = AlertPickerDialogFragment(
            saveClickListener
        )

        const val NO_ALERT = 0
        const val ALERT_10_MINUTE = 1
        const val ALERT_30_MINUTE = 2
        const val ALERT_1_HOUR = 3
        const val ALERT_1_DAY = 4
        const val ALERT_2_DAY = 5
    }

    private val bag = DisposeBag(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet_alert_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        }
        initClickListener()
    }

    private fun initClickListener() {
        RxView.clicks(btnSave)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                saveClickListener(getAlertValue())
                dismiss()
            }.disposedBy(bag)
        RxView.clicks(btnCancel)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                dismiss()
            }.disposedBy(bag)
    }

    private fun getAlertValue(): Int {
        val radioID = rgbAlertPicker.checkedRadioButtonId
        val buttonSelected = rgbAlertPicker.findViewById<View>(radioID)
        return rgbAlertPicker.indexOfChild(buttonSelected)
    }
}