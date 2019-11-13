package com.truongkhanh.supernote.view.evaluate.evaluatelist

import android.os.Bundle
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseNoAppBarActivity

class EvaluateListActivity : BaseNoAppBarActivity() {

    private lateinit var evaluateListFragment: EvaluateListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            evaluateListFragment =
                EvaluateListFragment.getInstance()
            if (intent != null)
                evaluateListFragment.arguments = intent.extras
            setFragment(evaluateListFragment)
        }
    }

    private fun setFragment(evaluateListFragment: EvaluateListFragment) {
        replaceFragment(R.id.fragmentContainer, evaluateListFragment)
    }
}