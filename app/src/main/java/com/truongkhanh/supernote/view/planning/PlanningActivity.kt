package com.truongkhanh.supernote.view.planning

import android.os.Bundle
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseNoAppBarActivity

class PlanningActivity : BaseNoAppBarActivity() {

    private lateinit var planningFragment: PlanningFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            planningFragment = PlanningFragment.getInstance()
            if (intent != null) {
                planningFragment.arguments = intent.extras
            }
            setFragment(planningFragment)
        }
    }


    private fun setFragment(planningFragment: PlanningFragment) {
        replaceFragment(R.id.fragmentContainer, planningFragment)
    }
}