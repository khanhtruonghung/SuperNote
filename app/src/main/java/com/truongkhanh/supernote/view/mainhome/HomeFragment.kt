package com.truongkhanh.supernote.view.mainhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.truongkhanh.musicapplication.base.BaseFragment
import com.truongkhanh.supernote.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : BaseFragment() {

    companion object {
        fun getInstance() = HomeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun setUpView(view: View, savedInstanceState: Bundle?) {
        fbCreateTodo.setOnClickListener {

        }
    }
}