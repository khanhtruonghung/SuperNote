package com.truongkhanh.supernote.view.planning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseFragment
import com.truongkhanh.supernote.utils.CustomAnimation
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.THROTTLE_TIME
import com.truongkhanh.supernote.utils.disposedBy
import kotlinx.android.synthetic.main.fragment_planning.*
import java.util.concurrent.TimeUnit

class PlanningFragment : BaseFragment() {

    companion object {
        fun getInstance() = PlanningFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_planning, container, false)
    }

    private val bag = DisposeBag(this)
    private val animation = CustomAnimation()
    private var isCollapse = false

    override fun setUpView(view: View, savedInstanceState: Bundle?) {
        setUpViewInformation()
        setUpClickEvent()
    }

    private fun setUpViewInformation() {
        startTimePicker.setIs24HourView(true)
        endTimePicker.setIs24HourView(true)
    }

    private fun setUpClickEvent() {
        RxView.clicks(btnDropDown)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                isCollapse = if (isCollapse) {
                    animation.expand(optimizeBar, clOptimize)
                    false
                } else {
                    animation.collapse(optimizeBar, clOptimize)
                    true
                }
            }.disposedBy(bag)
    }
}